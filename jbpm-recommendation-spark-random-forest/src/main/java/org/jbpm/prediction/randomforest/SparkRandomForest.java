/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.prediction.randomforest;

import com.google.common.collect.ImmutableList;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.feature.OneHotEncoder;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.prediction.PredictionOutcome;
import org.kie.internal.task.api.prediction.PredictionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparkRandomForest implements PredictionService {

    public static final String IDENTIFIER = "SparkRandomForest";
    private static final Logger logger = LoggerFactory.getLogger(SparkRandomForest.class);

    private final SparkSession session;

    private final StructType trainingSchema;
    private final StructType predictionSchema;
    private org.apache.spark.sql.Dataset<Row> df;
    private int count = 0;
    private PipelineModel pm = null;
    private final Pipeline pipeline;
    private double confidenceThreshold = 1.0;
    private final int numTrees = 10;
    private final int MINIMUM_OBSERVATIONS = 2;


    public SparkRandomForest() {

        final String sparkMaster = System.getProperty("org.jbpm.prediction.randomforest.spark.master", "local[4]");

        logger.info("Starting Apache Spark at {}", sparkMaster);

        session = SparkSession.builder()
                .master(sparkMaster)
                .appName("Random Forest example")
                .getOrCreate();

        // Get the Java specific Spark context
        final JavaSparkContext jsc = new JavaSparkContext(session.sparkContext());

        // Dataframe schema
        trainingSchema = DataTypes.createStructType(
                new StructField[] {
                        DataTypes.createStructField("user", DataTypes.StringType, false),
                        DataTypes.createStructField("level", DataTypes.IntegerType, false),
                        DataTypes.createStructField("label", DataTypes.IntegerType, false),
                });

        predictionSchema = DataTypes.createStructType(
                new StructField[] {
                        DataTypes.createStructField("user", DataTypes.StringType, false),
                        DataTypes.createStructField("level", DataTypes.IntegerType, false)
                });

        // Create an empty dataframe to store incoming data
        df = session.createDataFrame(jsc.emptyRDD(), trainingSchema).toDF();

        final StringIndexer userIndexer = new StringIndexer().setInputCol("user").setOutputCol("userIndex");
        final OneHotEncoder userEncoder = new OneHotEncoder().setInputCol("userIndex").setOutputCol("userVec");

        final VectorAssembler assembler = new VectorAssembler().setInputCols(new String[]{"userVec", "level"}).setOutputCol("features");
        final RandomForestClassifier randomForest = new RandomForestClassifier().setNumTrees(this.numTrees)
                .setMaxBins(32)
                .setMaxMemoryInMB(128)
                .setMaxDepth(3);

        // Pipeline stages consist of: indexing users -> encoding features -> assembling vector -> random forest
        final PipelineStage[] stages = new PipelineStage[]{
                userIndexer, userEncoder, assembler, randomForest
        };

        pipeline = new Pipeline().setStages(stages);
    }


    /**
     * Add the data provided as a map to a Smile {@link smile.data.Dataset}.
     *
     * @param data    A map containing the input attribute names as keys and the attribute values as values.
     * @param outcome The value of the outcome (output data).
     */
    public void addData(Map<String, Object> data, Object outcome) {
        // convert input data to a Row
        final List<Row> rows = ImmutableList.of(
                RowFactory.create(
                        data.get("ActorId").toString(),
                        data.get("level"),
                        ((Boolean) outcome) ? 1 : 0
                ));
        // Append the row to the existing dataframe
        df = df.union(session.createDataFrame(rows, trainingSchema));
    }

    /**
     * Returns the service's identifier
     *
     * @return The service identifier
     */
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * Returns a model prediction given the input data
     *
     * @param task      Human task data
     * @param inputData A map containing the input attribute names as keys and the attribute values as values.
     * @return A {@link PredictionOutcome} containing the model's prediction for the input data.
     */
    @Override
    public PredictionOutcome predict(Task task, Map<String, Object> inputData) {
        Map<String, Object> outcomes = new HashMap<>();

        if (pm != null) {

            // Allow confidence threshold to be overriden by a Java property
            final String confidenceThreshold = System.getProperty("org.jbpm.task.prediction.service.confidence_threshold");

            // Check if the confidence threshold was set at runtime, otherwise keep using as defined in output.properties
            if (confidenceThreshold!=null) {
                try {
                    this.confidenceThreshold = Double.parseDouble(confidenceThreshold);
                } catch (NumberFormatException e) {
                    logger.error("Invalid confidence threshold set in org.jbpm.task.prediction.service.confidence_threshold");
                }
            }


            final List<Row> rows = ImmutableList.of(RowFactory.create(inputData.get("ActorId").toString(), inputData.get("level")));
            final Dataset<Row> pdf = session.createDataFrame(rows, predictionSchema);
            final List<Row> result = pm.transform(pdf).toJavaRDD().collect();

            // Prediction must return at least one result
            if (result.isEmpty()) {
                logger.error("Apache Spark returned no predictions");
                return new PredictionOutcome();
            } else {
                final Row r = result.get(0);
                // prediction's row 6th element contains the outcome probabilities
                org.apache.spark.ml.linalg.DenseVector m = (org.apache.spark.ml.linalg.DenseVector) r.get(6);
                // determine the highest probability
                double[] outcomeProbabilities = m.toArray();
                double maxProbability = Math.max(outcomeProbabilities[0], outcomeProbabilities[1]);
                // prediction's row 7th element contains the outcome category (0 = false, 1 = true)
                final Boolean approved = (Double) r.get(7) == 1.0;
                logger.debug("predicting '{}' with confidence {}%", approved, maxProbability * 100.0);
                outcomes.put("approved", approved);
                outcomes.put("confidence", maxProbability);
                // TODO: Get the conf. threshold from a system property and test autocompletion
                return new PredictionOutcome(maxProbability, this.confidenceThreshold, outcomes);

            }
        } else {
            return new PredictionOutcome();
        }
    }

    /**
     * Train the random forest model using data from the human task.
     *
     * @param task       Human task data
     * @param inputData  A map containing the input attribute names as keys and the attribute values as values.
     * @param outputData A map containing the output attribute names as keys and the attribute values as values.
     */
    @Override
    public void train(Task task, Map<String, Object> inputData, Map<String, Object> outputData) {
        addData(inputData, outputData.get("approved"));

        // Wait until we have the minimum number of observations
        if (count > MINIMUM_OBSERVATIONS) {
            pm = pipeline.fit(df);
        }

        count++;
    }
}
