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

package org.jbpm.prediction.service.seldon.examples;

import org.jbpm.prediction.service.seldon.AbstractSeldonPredictionService;
import org.jbpm.prediction.service.seldon.payload.PredictionResponse;
import org.kie.api.task.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Example concrete implementation of a Seldon prediction service.
 */
public class ExampleSeldonPredictionService extends AbstractSeldonPredictionService {

    public static final String IDENTIFIER = "SeldonPredictionService";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * Build a domain specific list of numerical features based on the input data.
     *
     * @param task Human task data
     * @param map A map containing the input attribute names as keys and the attribute values as values.
     * @return A 2D {@link List} of numerical features
     */
    @Override
    public List<List<Double>> buildPredictFeatures(Task task, Map<String, Object> map) {
        List<List<Double>> result = new ArrayList<>();
        List<Double> single = new ArrayList<>();

        single.add(map.get("ActorId").equals("john") ? 0.0 : 1.0);
        single.add(Double.valueOf((Integer) map.get("level")));
        result.add(single);
        return result;
    }

    /**
     * Transfom the deserialised Seldon's response ({@link PredictionResponse}) into a
     * {@link org.kie.internal.task.api.prediction.PredictionOutcome} data map.
     * @param response Deserialized Seldon's response
     * @return A map containing data for {@link org.kie.internal.task.api.prediction.PredictionOutcome}
     */
    @Override
    public Map<String, Object> parsePredictFeatures(PredictionResponse response) {
        Map<String, Object> result = new HashMap<>();
        List<Double> features = new ArrayList<>();

        if (response.getData().getArray()!=null) {

            features = response.getData().getArray().get(0);

        } else if (response.getData().getTensorData()!=null) {

            features = response.getData().getTensorData().getValues();

        }

        double o1 = features.get(0);
        double o2 = features.get(1);

        if (o1 > o2) {
            result.put("outcome", true);
            result.put("confidence", o1);
        } else {
            result.put("outcome", false);
            result.put("confidence", o2);
        }

        return result;
    }
}
