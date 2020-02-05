/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kiegroup.zenithr.drools.rest;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;

import io.quarkus.test.junit.QuarkusTest;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.kiegroup.zenithr.drools.FileUtils;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class RuleEndpointTest {

    @Test
    public void testRequest() {
        JsonObject deployment = FileUtils.readObject("deployment-replicas1.json");
        JsonObject json = Json.createObjectBuilder().add("deployment1", deployment).build();
        JsonArray expected = Json.createArrayBuilder().add(
            Json.createObjectBuilder()
                .add("name", "deployment2")
                .add("path", "spec")
                .add("value", Json.createObjectBuilder()
                    .add("replicas", 1)
                    .build())
                .build())
            .add(
                Json.createObjectBuilder()
                    .add("name", "deployment3")
                    .add("path", "spec")
                    .add("value", Json.createObjectBuilder()
                        .add("replicas", 0)
                        .build())
                    .build())
            .build();

        given()
            .when()
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(json.toString())
            .post()
            .then()
            .statusCode(200)
            .body(CoreMatchers.is(expected.toString()));
    }
}
