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

import org.jbpm.prediction.service.seldon.payload.PredictionMetadata;
import org.jbpm.prediction.service.seldon.payload.PredictionResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FeatureDeserializerTest {

    @Test
    public void testSingleFeatureDeserialization() throws IOException {

        final String JSON = TestUtils.readJSONAsString("responses/ndarray.json");

        final PredictionResponse response = PredictionResponse.parse(JSON);

        assertEquals(response.getData().getArray().size(), 1);
        assertEquals(response.getData().getNames().size(), 2);
        List<Double> firstOutcome = response.getData().getArray().get(0);
        assertEquals(firstOutcome.size(), 2);
        assertEquals(firstOutcome.get(0), 0.71, 0.0);
        assertEquals(firstOutcome.get(1), 0.29, 0.0);
    }

    @Test
    public void testTagsDeserialization() throws IOException {
        final String JSON = TestUtils.readJSONAsString("responses/metadata.json");

        final PredictionResponse response = PredictionResponse.parse(JSON);

        final PredictionMetadata metadata = response.getMetadata();

        assertEquals(metadata.getTags().keySet().size(), 2);
        assertEquals(metadata.getTags().get("version"), 1.0);
        assertEquals(metadata.getTags().get("namespace"), "localhost");
    }
}