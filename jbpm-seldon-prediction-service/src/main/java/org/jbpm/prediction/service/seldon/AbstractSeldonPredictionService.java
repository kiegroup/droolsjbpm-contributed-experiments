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
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jbpm.prediction.service.seldon.payload.AuthorizationTokenFilter;
import org.jbpm.prediction.service.seldon.payload.PredictionRequest;
import org.jbpm.prediction.service.seldon.payload.PredictionResponse;
import org.jbpm.prediction.service.seldon.payload.TokenResponse;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.prediction.PredictionOutcome;
import org.kie.internal.task.api.prediction.PredictionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSeldonPredictionService implements PredictionService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSeldonPredictionService.class);
    private static final String SELDON_URL_KEY = "org.jbpm.task.prediction.service.seldon.url";
    private static final String SELDON_AUTH_CLIENT_ID = "org.jbpm.task.prediction.service.seldon.auth.client_id";
    private static final String SELDON_AUTH_CLIENT_SECRET = "org.jbpm.task.prediction.service.seldon.auth.client_secret";
    private static final String CONFIDENCE_THRESHOLD_KEY = "org.jbpm.task.prediction.service.seldon.confidence_threshold";
    private static final String SELDON_TIMEOUT_KEY = "org.jbpm.task.prediction.service.seldon.timeout";
    private static final String SELDON_CONNECTION_POOL_SIZE_KEY = "org.jbpm.task.prediction.service.seldon.connection_pool_size";
    protected final ResteasyClient client;
    protected final ResteasyWebTarget predict;
    private double confidenceThreshold = 1.0;

    public AbstractSeldonPredictionService() {
        // Seldon connection configuration
        Configurations configs = new Configurations();
        CompositeConfiguration compositeConfiguration = new CompositeConfiguration();

        Configuration javaProperties = new PropertiesConfiguration();
        Configuration systemProperties = new SystemConfiguration();

        compositeConfiguration.addConfiguration(javaProperties);
        compositeConfiguration.addConfiguration(systemProperties);

        readConfigurationProperties("/seldon.properties", configs, compositeConfiguration);
        readConfigurationProperties("/authorization.properties", configs, compositeConfiguration);

        final String SELDON_URL = compositeConfiguration.getString(SELDON_URL_KEY);

        if (SELDON_URL == null) {
            final String errorMessage = "No Seldon endpoint URL specified";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        logger.debug("Using Seldon endpoint " + SELDON_URL);

        ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder();

        final String seldonTimeoutStr = compositeConfiguration.getString(SELDON_TIMEOUT_KEY);

        if (seldonTimeoutStr != null) {
            try {
                final long seldonTimeout = Long.parseLong(seldonTimeoutStr);
                clientBuilder = clientBuilder.connectionCheckoutTimeout(seldonTimeout, TimeUnit.MILLISECONDS);
                logger.info("Seldon connection timeout set to " + seldonTimeout + " milliseconds");
            } catch (NumberFormatException e) {
                logger.error("Invalid Seldon connection timeout");
            }
        }

        final String seldonConnectioPoolSizeStr = compositeConfiguration.getString(SELDON_CONNECTION_POOL_SIZE_KEY);

        if (seldonConnectioPoolSizeStr != null) {
            try {
                final int seldonConnectioPoolSize = Integer.parseInt(seldonConnectioPoolSizeStr);
                clientBuilder = clientBuilder.connectionPoolSize(seldonConnectioPoolSize);
                logger.info("Seldon connection pool size set to " + seldonConnectioPoolSize);
            } catch (NumberFormatException e) {
                logger.error("Invalid Seldon connection pool size");
            }
        }

        client = clientBuilder.build();

        // Get the OAuth client id and secret from the configuration
        final String seldonOAuthClientID = compositeConfiguration.getString(SELDON_AUTH_CLIENT_ID);
        final String seldonOAuthClientSecret = compositeConfiguration.getString(SELDON_AUTH_CLIENT_SECRET);

        if (seldonOAuthClientID != null && seldonOAuthClientSecret != null) {
            logger.info("Using authenticated Seldon requests");
                // If both OAuth client id and secret exist, request an authorization token from the auth endpoint
                final TokenResponse tokenResponse = requestAuthorizationToken(client,
                        SELDON_URL,
                        seldonOAuthClientID,
                        seldonOAuthClientSecret);
                if (tokenResponse != null) {
                    // If the authorization was successful, register the token for all subsequent
                    // HTTP requests
                    logger.info("Client authorized by Seldon at {}/oauth/token", SELDON_URL);
                    client.register(new AuthorizationTokenFilter(tokenResponse.getAccess_token()));
                } else {
                    logger.warn("No valid token response. Skipping authentication");
                }

        } else {
            logger.debug("Using Seldon without authentication");
        }

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

    private void readConfigurationProperties(String filename, Configurations configurations, CompositeConfiguration composite) {
        try {
            Configuration properties = configurations.properties(new File(filename));
            composite.addConfiguration(properties);

        } catch (ConfigurationException e) {
            logger.debug("Could not find the file '" + filename + "'. Trying other configuration sources.");
        }
    }

    /**
     * Request an authorization token from the server, by providing the OAuth key and secret
     * @param client HTTP {@link ResteasyClient} object
     * @param seldonURL Seldon server location
     * @param oauthClientID OAuth client id
     * @param oauthClientSecret OAuth client secret
     * @return An authorization token as a {@link TokenResponse}
     */
    private TokenResponse requestAuthorizationToken(ResteasyClient client,
                                                    String seldonURL,
                                                    String oauthClientID,
                                                    String oauthClientSecret) {

        final Form form = new Form().param("grant_type", "client_credentials");
        final ResteasyWebTarget target = client.target(seldonURL).path("oauth").path("token");

        target.register(new BasicAuthentication(oauthClientID, oauthClientSecret));
        return target.request().post(Entity.form(form), TokenResponse.class);
    }

    /**
     * Returns a model prediction given the input data.
     * <p>
     * A {@link PredictionRequest} is sent to the Seldon server containing the features built using the concrete
     * implementation of {@link PredictionRequest#build(List)}. The response is deserialized by
     * {@link PredictionResponse#parse(String)} and passed to {@link #parsePredictFeatures(PredictionResponse)}
     * in order to build the {@link PredictionOutcome}.
     *
     * @param task Human task data
     * @param map  A map containing the input attribute names as keys and the attribute values as values.
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
        } catch (Exception e) {
            logger.error(e.getMessage());
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
     * @param map  A map containing the input attribute names as keys and the attribute values as values.
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
