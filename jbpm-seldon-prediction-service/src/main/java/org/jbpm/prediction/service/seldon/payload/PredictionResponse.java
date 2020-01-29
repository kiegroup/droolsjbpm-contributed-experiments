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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Seldon's prediction response deserialization result.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PredictionResponse {

    @JsonProperty(value = "data", required = true)
    private final PredictionData data = new PredictionData();
    @JsonProperty(value = "meta")
    private final PredictionMetadata metadata = new PredictionMetadata();

    public PredictionResponse() {

    }

    /**
     * Returns the data part of Seldon's response as a {@link PredictionData} object.
     * @return Deserialized prediction data response object
     */
    public PredictionData getData() {
        return data;
    }

    /**
     * Returns the metadata part of Seldon's response as a {@link PredictionMetadata} object.
     * @return Deserialized prediction metadata response object
     */
    public PredictionMetadata getMetadata() {
        return metadata;
    }

    /**
     * Builds a {@link PredictionResponse} object from a JSON response {@link String}.
     * @param response Seldon's JSON response as a {@link String}
     * @return Deserialized response as a {@link PredictionResponse}
     * @throws IOException
     */
    public static PredictionResponse parse(String response) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response, PredictionResponse.class);
    }


}
