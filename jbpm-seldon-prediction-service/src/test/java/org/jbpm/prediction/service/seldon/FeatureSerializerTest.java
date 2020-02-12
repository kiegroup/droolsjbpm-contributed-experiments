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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jbpm.prediction.service.seldon.payload.PredictionRequest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class FeatureSerializerTest {

    private static List<Double> buildFeatures(double... features) {
        return Arrays.stream(features).boxed().collect(Collectors.toList());
    }

    @Test
    public void testSingleFeatureSerialization() throws JsonProcessingException {
        final List<List<Double>> features = new ArrayList<>();
        features.add(buildFeatures(1.0, 2500.0));

        final String request = PredictionRequest.build(features);
        assertEquals("{\"data\":{\"ndarray\":[[1.0,2500.0]]}}", request);
    }

    @Test
    public void testMultipleFeatureSerialization() throws JsonProcessingException {
        final List<List<Double>> features = new ArrayList<>();

        features.add(buildFeatures(1.0, 2500.0));
        features.add(buildFeatures(0.0, 1944.2));

        String result = PredictionRequest.build(features);
        assertEquals("{\"data\":{\"ndarray\":[[1.0,2500.0],[0.0,1944.2]]}}", result);
    }

}