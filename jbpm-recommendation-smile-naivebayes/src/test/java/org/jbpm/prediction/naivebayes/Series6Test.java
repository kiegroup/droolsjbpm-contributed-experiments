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

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Series6Test extends NaiveBayesRecommendationServiceProcessTest {
    @Test
    public void testAutoCompletion() {
        System.setProperty("org.jbpm.task.prediction.service.confidence_threshold", "0.85");
        Map<String, Object> outputs = new HashMap<>();
        startAndReturnTaskOutputData("item6", "mary", 5, false);
        for (int i = 0; i < 200; i++) {
                startAndReturnTaskOutputData("item6", "mary", 5, false);
            for (int j = 0 ; j < 6 ; j++) {
                outputs = startAndReturnTaskOutputData("item6", "mary", 5, true);
            }
        }
        assertTrue(outputs.isEmpty());
    }

}
