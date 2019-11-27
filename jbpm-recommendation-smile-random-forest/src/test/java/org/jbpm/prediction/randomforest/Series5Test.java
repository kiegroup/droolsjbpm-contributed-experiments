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
import java.util.Random;

public class Series5Test extends RandomForestPredictionServiceProcessTest {


    @Test
    public void testConfidenceWithRandomOutcomes() {
        Map<String, Object> outputs;

        for (int i = 0; i < 100; i++) {
            startAndReturnTaskOutputData("test item", "john", i % 5, !(new Random().nextDouble() < 0.9));
            startAndReturnTaskOutputData("test item", "mary", i % 5, new Random().nextDouble() < 0.9);
            startAndReturnTaskOutputData("test item", "mary", i % 3, new Random().nextDouble() < 0.9);
        }
        startAndReturnTaskOutputData("test item2", "krisv", 10, true);
        startAndReturnTaskOutputData("test item2", "krisv", 11, false);
        startAndReturnTaskOutputData("test item2", "krisv", 11, false);
        startAndReturnTaskOutputData("test item2", "krisv", 10, true);
        startAndReturnTaskOutputData("test item2", "krisv", 10, false);
        startAndReturnTaskOutputData("test item", "john", 5, false);
        outputs = startAndReturnTaskOutputData("test item2", "krisv", 10, true);
        assertBetween((double) outputs.get("confidence"), 0.4, 0.6);
    }


}
