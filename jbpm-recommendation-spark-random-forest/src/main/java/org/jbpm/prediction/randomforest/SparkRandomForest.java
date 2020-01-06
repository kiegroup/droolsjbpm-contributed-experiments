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
import org.apache.spark.ml.feature.OneHotEncoderEstimator;
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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparkRandomForest implements PredictionService {

    public static final String IDENTIFIER = "SparkRandomForest";
    private static final Logger logger = LoggerFactory.getLogger(SparkRandomForest.class);
    private static final int numTrees = 10;

    private SparkSession session;

    private final StructType trainingSchema;
    private final StructType predictionSchema;
    private org.apache.spark.sql.Dataset<Row> df;
    private PipelineModel pm = null;
    private final Pipeline pipeline;
    private double confidenceThreshold = 1.0;
    private final List<Row> miniBatch = new ArrayList<>();

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
                        DataTypes.createStructField("item", DataTypes.StringType, false),
                        DataTypes.createStructField("label", DataTypes.IntegerType, false),
                });

        predictionSchema = DataTypes.createStructType(
                new StructField[] {
                        DataTypes.createStructField("user", DataTypes.StringType, false),
                        DataTypes.createStructField("level", DataTypes.IntegerType, false),
                        DataTypes.createStructField("item", DataTypes.StringType, false)
                });

        // Create an empty dataframe to store incoming data
        df = session.createDataFrame(jsc.emptyRDD(), trainingSchema).toDF();

        final StringIndexer userIndexer = new StringIndexer()
                .setInputCol("user")
                .setOutputCol("userIndex")
                .setHandleInvalid("keep");
        final OneHotEncoderEstimator userEncoder = new OneHotEncoderEstimator()
                .setInputCols(new String[]{"userIndex"})
                .setOutputCols(new String[]{"userVec"});
        final StringIndexer itemIndexer = new StringIndexer()
                .setInputCol("item")
                .setOutputCol("itemIndex")
                .setHandleInvalid("keep");
        final OneHotEncoderEstimator itemEncoder = new OneHotEncoderEstimator()
                .setInputCols(new String[]{"itemIndex"})
                .setOutputCols(new String[]{"itemVec"});

        final VectorAssembler assembler = new VectorAssembler()
                .setInputCols(new String[]{"userVec", "level", "itemVec"})
                .setOutputCol("features");
        final RandomForestClassifier randomForest = new RandomForestClassifier().setNumTrees(this.numTrees)
                .setMaxBins(32)
                .setMaxMemoryInMB(128)
                .setMaxDepth(3);

        // Pipeline stages consist of: indexing users / items -> encoding features -> assembling vector -> random forest
        final PipelineStage[] stages = new PipelineStage[]{
                userIndexer, userEncoder, itemIndexer, itemEncoder, assembler, randomForest
        };

        pipeline = new Pipeline().setStages(stages);
    }

    /**
     * Add provided data to an existing JavaRDD {@link org.apache.spark.api.java.JavaRDD}.
     *
     * @param data    A map containing the input attribute names as keys and the attribute values as values.
     * @param outcome The value of the outcome (output data).
     */
    public void addData(Map<String, Object> data, Object outcome) {
        // convert input data to a Row
        final Row row = RowFactory.create(
                data.get("ActorId").toString(),
                data.get("level"),
                data.get("item"),
                (Boolean.TRUE.equals((Boolean) outcome)) ? 1 : 0
        );

        miniBatch.add(row);

        if (miniBatch.size() > 20) {
            // Append the row to the existing dataframe
            df = df.union(session.createDataFrame(miniBatch, trainingSchema));
            miniBatch.clear();
        }
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

        if (pm == null) {
            return new PredictionOutcome();
        }

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
        final List<Row> rows = ImmutableList.of(
                RowFactory.create(
                        inputData.get("ActorId").toString(),
                        inputData.get("level"),
                        inputData.get("item")
                ));
        final Dataset<Row> pdf = session.createDataFrame(rows, predictionSchema);
        final List<Row> result = pm.transform(pdf).toJavaRDD().collect();

        // Prediction must return at least one result
        if (result.isEmpty()) {
            logger.error("Apache Spark returned no predictions");
            return new PredictionOutcome();
        } else {
            final Row r = result.get(0);
            // prediction's row 10th element contains the outcome probabilities
            org.apache.spark.ml.linalg.DenseVector m = (org.apache.spark.ml.linalg.DenseVector) r.get(9);
            // determine the highest probability
            double[] outcomeProbabilities = m.toArray();
            double maxProbability;
            // The outcome probabilities will be as many as unique outcomes seen so far
            // We have either one or two unique outcomes in this example
            if (outcomeProbabilities.length == 1) {
                maxProbability = outcomeProbabilities[0];
            } else {
                maxProbability = Math.max(outcomeProbabilities[0], outcomeProbabilities[1]);
            }
            // prediction's row 11th element contains the outcome category (0 = false, 1 = true)
            final Boolean approved = (Double) r.get(10) == 1.0;
            logger.debug("predicting '{}' with confidence {}%", approved, maxProbability * 100.0);
            outcomes.put("approved", approved);
            outcomes.put("confidence", maxProbability);
            return new PredictionOutcome(maxProbability, this.confidenceThreshold, outcomes);
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

        try {
            pm = pipeline.fit(df);
        } catch (IllegalArgumentException e) {
            // Fitting will be skipped until we have the necessary amount of data
            logger.debug("Apache Spark does not have enough unique data for training");
        }
    }

    @PreDestroy
    public void closeSparkSession() {
        session.close();
    }
}
