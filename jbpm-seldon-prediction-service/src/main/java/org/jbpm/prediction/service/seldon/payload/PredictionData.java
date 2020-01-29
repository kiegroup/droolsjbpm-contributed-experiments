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

import java.util.ArrayList;
import java.util.List;

/**
 * Seldon's prediction data response deserialization result.
 */
public class PredictionData {

    @JsonProperty("names")
    private final List<String> names = new ArrayList<>();

    @JsonProperty(value = "ndarray")
    private List<List<Double>> array;
    @JsonProperty(value = "tftensor")
    private List<Byte> tftensor;
    @JsonProperty(value = "tensor")
    private PredictionTensorData tensorData;

    /**
     * If available, returns a TFTensor response as a byte {@link List}, otherwise return null.
     *
     * @return Byte {@link List} corresponding to the packed TFTensor
     */
    public List<Byte> getTftensor() {
        return tftensor;
    }

    /**
     * Returns the features names.
     *
     * @return List of feature names.
     */
    public List<String> getNames() {
        return names;
    }

    /**
     * If the response is of type NDArray, return the two-dimensional NDArray as a {@link List} of {@link List<Double>},
     * otherwise null.
     *
     * @return A two-dimensional list of {@link Double}
     */
    public List<List<Double>> getArray() {
        return array;
    }

    /**
     * If the response is of type Tensor, return a {@link PredictionTensorData} object, otherwise null.
     *
     * @return Tensor data object
     */
    public PredictionTensorData getTensorData() {
        return tensorData;
    }
}
