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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class EqualProbabilityTest extends RandomForestPredictionServiceProcessTest {

    /**
     * Insert an equal number of true and false samples, making
     * sure the total number of samples is larger than the dataset
     * size threshold. Since the dataset size
     * threshold will have been met and the probability of true
     * and false will be nearly equal, we expect confidence to be
     * lower than 0.55 (55%).
     */
    @Test
    public void testEqualProbabilityRandomForestPredictionService() {
        System.setProperty("org.jbpm.task.prediction.service.confidence_threshold", "1.0");
        Map<String, Object> outputs = new HashMap<>();

        for (int i = 0; i < 100; i++) {
            startAndReturnTaskOutputData("test item", "john", 5, false);
            outputs = startAndReturnTaskOutputData("test item", "john", 5, true);
        }

        final double confidence = (double) outputs.get("confidence");
        assertTrue(confidence < 0.6 && confidence > 0.4);
    }


}
