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

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RepeatedRandomForestTest extends RandomForestPredictionServiceProcessTest {
    /**
     * For this test insert a quantity of true training samples
     * to verify the random forest class/process is functional.
     * Expect confidence > 90.0 and "approved" to be true.
     */
    @Test
    public void testRepeatedRandomForestPredictionService() {
        System.setProperty("org.jbpm.task.prediction.service.confidence_threshold", "1.0");
        Map<String, Object> outputs;
        outputs = startAndReturnTaskOutputData("test item", "john", 5, false);
        for (int i = 0; i < 40; i++) {
            outputs = startAndReturnTaskOutputData("test item", "john", 5, true);
        }
        assertEquals("true", outputs.get("approved"));
    }


}
