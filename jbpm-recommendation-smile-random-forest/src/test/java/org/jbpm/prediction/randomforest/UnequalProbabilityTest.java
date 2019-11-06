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

public class UnequalProbabilityTest extends RandomForestPredictionServiceProcessTest {

    /**
     * Insert a disproportionate partitioning of true and false samples
     * of a sample set larger than the dataset size threshold. In this
     * case true will have higher probability and as such we expect
     * confidence to be high.
     */
    @Test
    public void testUnequalProbabilityRandomForestPredictionService() {
        Map<String, Object> outputs;

        outputs = startAndReturnTaskOutputData("test item", "john", 5, true);
        for (int i = 0; i < 10; i++) {
            outputs = startAndReturnTaskOutputData("test item", "john", 5, false);
        }
        for (int i = 0; i < 90; i++) {
            outputs = startAndReturnTaskOutputData("test item", "john", 5, true);
        }

        assertEquals("true", outputs.get("approved"));
    }


}
