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
 * Seldon's prediction response tensor data deserialization result.
 */
public class PredictionTensorData {

    @JsonProperty(value = "shape", required = true)
    private final int[] shape = new int[2];
    @JsonProperty(value = "values", required = true)
    private final List<Double> values = new ArrayList<>();

    /**
     * Return the tensor's shape.
     *
     * @return An array containing the tensor's shape
     */
    public int[] getShape() {
        return shape;
    }

    /**
     * Return a the tensor's data.
     *
     * @return A {@link List<Double>} with the tensor's data
     */
    public List<Double> getValues() {
        return values;
    }
}
