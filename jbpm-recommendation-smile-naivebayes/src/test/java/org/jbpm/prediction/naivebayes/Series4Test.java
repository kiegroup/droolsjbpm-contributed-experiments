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

package org.jbpm.prediction.naivebayes;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Series4Test extends NaiveBayesRecommendationServiceProcessTest {
    /**
     * This test shows how after passing min count of 2 input with 1 irrelevant param switching between 5 possible
     * values, accuracy of completely new input is extremely high.
     */
    @Test
    public void test4() {
        Map<String, Object> outputs;

        for (int i = 0; i < 50; i++) {
            startAndReturnTaskOutputData("test item", "john", i % 5, false);
            startAndReturnTaskOutputData("test item", "mary", i % 5, true);
        }
        startAndReturnTaskOutputData("test item2", "krisv", 10, true);
        startAndReturnTaskOutputData("test item2", "krisv", 11, false);
        startAndReturnTaskOutputData("test item2", "krisv", 11, false);
        startAndReturnTaskOutputData("test item2", "krisv", 10, true);
        startAndReturnTaskOutputData("test item2", "krisv", 10, false);
        startAndReturnTaskOutputData("test item", "john", 5, false);
        outputs = startAndReturnTaskOutputData("test item2", "krisv", 10, true);

        assertTrue((double) outputs.get("confidence") > 0.7);
        assertEquals("true", outputs.get("approved"));
    }

}
