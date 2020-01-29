/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.prediction.service.seldon;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jbpm.prediction.service.seldon.payload.PredictionRequest;
import org.jbpm.prediction.service.seldon.payload.PredictionResponse;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.prediction.PredictionOutcome;
import org.kie.internal.task.api.prediction.PredictionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class AbstractSeldonPredictionService implements PredictionService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSeldonPredictionService.class);
    protected final ResteasyClient client = new ResteasyClientBuilder().build();
    protected final ResteasyWebTarget predict;
    private double confidenceThreshold = 1.0;

    private static final String SELDON_URL_KEY = "org.jbpm.task.prediction.service.seldon.url";
    private static final String CONFIDENCE_THRESHOLD_KEY = "org.jbpm.task.prediction.service.seldon.confidence_threshold";


    public AbstractSeldonPredictionService() {
        // Seldon connection configuration
        Configurations configs = new Configurations();
        CompositeConfiguration compositeConfiguration = new CompositeConfiguration();

        Configuration javaProperties = new PropertiesConfiguration();
        Configuration systemProperties = new SystemConfiguration();

        compositeConfiguration.addConfiguration(javaProperties);
        compositeConfiguration.addConfiguration(systemProperties);
        try {
            Configuration config = configs.properties(new File("seldon.properties"));
            compositeConfiguration.addConfiguration(config);
        } catch (ConfigurationException e) {
            logger.debug("Could not find the file 'seldon.properties'. Trying other configuration sources.");
        }

        final String SELDON_URL = compositeConfiguration.getString(SELDON_URL_KEY);

        if (SELDON_URL == null) {
            final String errorMessage = "No Seldon endpoint URL specified";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        logger.debug("Using Seldon endpoint " + SELDON_URL);

        predict = client.target(SELDON_URL).path("predict");

        // set confidence threshold from configuration
        final String CONFIDENCE_THRESHOLD = compositeConfiguration.getString(CONFIDENCE_THRESHOLD_KEY);

        if (CONFIDENCE_THRESHOLD != null) {
            try {
                this.confidenceThreshold = Double.parseDouble(CONFIDENCE_THRESHOLD);
                logger.info("Setting confidence threshold to " + this.confidenceThreshold);
            } catch (NumberFormatException e) {
                logger.error("Invalid confidence threshold in org.jbpm.task.prediction.service.seldon.confidence_threshold");
            }
        } else {
            logger.info("Using default confidence threshold of 1.0");
        }
    }

    @Override
    public abstract String getIdentifier();

    /**
     * Returns a model prediction given the input data.
     *
     * A {@link PredictionRequest} is sent to the Seldon server containing the features built using the concrete
     * implementation of {@link PredictionRequest#build(List)}. The response is deserialized by
     * {@link PredictionResponse#parse(String)} and passed to {@link #parsePredictFeatures(PredictionResponse)}
     * in order to build the {@link PredictionOutcome}.
     *
     * @param task Human task data
     * @param map A map containing the input attribute names as keys and the attribute values as values.
     * @return A {@link PredictionOutcome} containing the model's prediction for the input data.
     */
    @Override
    public PredictionOutcome predict(Task task, Map<String, Object> map) {
        final List<List<Double>> features = buildPredictFeatures(task, map);
        try {
            final String json = PredictionRequest.build(features);
            final String stringResponse = predict.request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON_TYPE), String.class);
            final PredictionResponse response = PredictionResponse.parse(stringResponse);
            final Map<String, Object> parsedResponse = parsePredictFeatures(response);
            return new PredictionOutcome((Double) parsedResponse.get("confidence"), this.confidenceThreshold, parsedResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new PredictionOutcome();
    }

    /**
     * Model training is not supported in this service with Seldon. This is a NO-OP.
     */
    @Override
    public void train(Task task, Map<String, Object> map, Map<String, Object> map1) {
        // Training not supported
    }

    /**
     * Construct a list of numerical features which can be passed to Seldon with the provided
     * task and input data.
     * It returns a two-dimensional numerical list where each list element corresponds
     * to a single prediction request's features.
     * This is domain specific and must be implemented in the concrete service.
     *
     * @param task Human task data
     * @param map A map containing the input attribute names as keys and the attribute values as values.
     * @return A two-dimensional list of numerical features.
     */
    public abstract List<List<Double>> buildPredictFeatures(Task task, Map<String, Object> map);

    /**
     * Builds the {@link PredictionOutcome} data from the deserialized Seldon response.
     * This is domain specific and must be implemented in the concrete service.
     *
     * @param response Deserialized Seldon's response
     * @return A {@link Map} as the {@link PredictionOutcome} data
     */
    public abstract Map<String, Object> parsePredictFeatures(PredictionResponse response);

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }
}
