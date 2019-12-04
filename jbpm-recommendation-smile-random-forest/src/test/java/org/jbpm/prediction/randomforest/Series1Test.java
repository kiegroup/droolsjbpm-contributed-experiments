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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Series1Test extends RandomForestPredictionServiceProcessTest {

    /**
     * This test shows how after testing with a mixed set (60x false, 17x true) how prediction (and probability)
     * evolve when you then keep sending as "false" value to the outcome.
     */
    @Test
    public void testChangingConfidenceWithInputDataDrift() {
        System.setProperty("org.jbpm.task.prediction.service.confidence_threshold", "1.0");
        Map<String, Object> outputs = new HashMap<>();

        for (int i = 0; i < 17; i++) {
            outputs = startAndReturnTaskOutputData("test item", "john", 5, true);
        }
        for (int i = 0; i < 60; i++) {
            outputs = startAndReturnTaskOutputData("test item", "john", 5, false);
        }
        assertTrue((double) outputs.get("confidence") > 0.5);
        assertEquals("false", outputs.get("approved"));

    }

}
