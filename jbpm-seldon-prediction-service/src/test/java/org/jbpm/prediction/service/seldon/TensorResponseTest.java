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

import org.junit.Test;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;

public class TensorResponseTest extends SeldonTestSuite {

    @Test
    public void testTensorResponse() {
        final String endpoint = System.getProperty("org.jbpm.task.prediction.service.seldon.endpoint", "predict");
        stubFor(post(urlEqualTo("/" + endpoint))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.readJSONAsString("responses/tensor.json"))));

        startAndReturnTaskOutputData("test item", "john", 5, false);
        Map<String, Object> outputs = startAndReturnTaskOutputData("test item", "john", 5, true);

        final double confidence = (double) outputs.get("confidence");
        assertEquals(0.79, confidence, 1e-10);
    }

}