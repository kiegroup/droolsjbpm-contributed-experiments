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

package org.jbpm.prediction.service.seldon.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Seldon's prediction request to be serialized.
 */
@JsonRootName(value = "data")
public class PredictionRequest implements Serializable {

    private final List<List<Double>> features;

    public PredictionRequest() {
        this(new ArrayList<>());
    }

    /**
     * Construct the request using a predefined 2D list of features.
     *
     * @param features A 2D list of numerical features
     */
    public PredictionRequest(List<List<Double>> features) {
        this.features = features;
    }

    /**
     * Get the numerical features in this request.
     *
     * @return A 2D list of numerical features
     */
    @JsonProperty("ndarray")
    public List<List<Double>> getFeatures() {
        return features;
    }

    /**
     * Add a list of features to the prediction request.
     *
     * @param features A 1D list of numerical features
     */
    public void addFeatures(List<Double> features) {
        this.features.add(features);
    }

    /**
     * Serialise the request into a JSON {@link String} payload using a specified 2D list of features.
     *
     * @param features A 2D list of numerical features for the request
     * @return A JSON String representation for the Seldon request payload
     * @throws JsonProcessingException
     */
    public static String build(List<List<Double>> features) throws JsonProcessingException {
        final PredictionRequest req = new PredictionRequest(features);
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        return mapper.writeValueAsString(req);
    }

}
