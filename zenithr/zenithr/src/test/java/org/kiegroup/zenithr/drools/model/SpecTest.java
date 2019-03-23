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

package org.kiegroup.zenithr.drools.model;

import java.io.IOException;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SpecTest {
    
    @Test
    public void testParseEmpty() throws IOException {
        JsonObject json = Json.createObjectBuilder()
                              .build();
        System.out.println(json);
        Spec spec = parseSpec(json);
        assertNotNull(spec);
        assertTrue(spec.getInputs().isEmpty());
        assertEquals("Zenithr", spec.getName());
    }
    
   

    @Test
    public void testOutputComplexType() throws IOException {
        JsonObject json = Json.createObjectBuilder()
                              .add(Spec.FIELD_INPUTS, Json.createArrayBuilder())
                              .add(Spec.FIELD_TYPES, buildTypes())
                              .add(Spec.FIELD_RULES, Json.createArrayBuilder())
                              .add(Spec.FIELD_OUTPUT, "Person")
                              .build();
        Spec spec = parseSpec(json);
        assertEquals("Person", spec.getOutput());
    }

    @Test
    public void testParseDeclare() throws IOException {
        JsonObject json = Json.createObjectBuilder()
                              .add(Spec.FIELD_INPUTS, Json.createArrayBuilder())
                      //        .add(Spec.FIELD_RULES, Json.createArrayBuilder())
                              .add(Spec.FIELD_TYPES, buildTypes())
                              .add(Spec.FIELD_OUTPUT, FieldType.STRING.getValue())
                              .build();
        Spec spec = parseSpec(json);
        assertNotNull(spec);
        assertFalse(spec.getTypes().isEmpty());
        assertEquals(FieldType.STRING, spec.getTypes().get("Person").getDefinition().get("name"));
        assertEquals(FieldType.INTEGER, spec.getTypes().get("Person").getDefinition().get("age"));
    }

    @Test
    public void testParseInputs() throws IOException {
        JsonObject json = Json.createObjectBuilder()
                              .add(Spec.FIELD_INPUTS, buildInputs())
                              .add(Spec.FIELD_OUTPUT, FieldType.STRING.getValue())
                              .build();
        Spec spec = parseSpec(json);
        assertNotNull(spec);
        assertEquals("Person", spec.getInputs().get("employee").getType());
        assertEquals("Person", spec.getInputs().get("manager").getType());
        assertEquals(FieldType.BOOLEAN.getValue(), spec.getInputs().get("nice").getType());
        assertTrue(spec.getTypes().isEmpty());
        assertTrue(spec.getRules().isEmpty());
    }

    @Test
    public void testParseRules() throws IOException {
        JsonArray rules = buildRules();
        JsonObject json = Json.createObjectBuilder()
                              .add(Spec.FIELD_INPUTS, buildInputs())
                              .add(Spec.FIELD_TYPES, buildTypes())
                              .add(Spec.FIELD_RULES, rules)
                              .add(Spec.FIELD_OUTPUT, FieldType.STRING.getValue())
                              .build();
        Spec spec = parseSpec(json);
        assertNotNull(spec);
        assertEquals(3, spec.getRules().size());
        // Rule 0
        assertEquals(2, spec.getRules().get(0).getWhen().size());
        assertEquals("manager.name != Joe", spec.getRules().get(0).getWhen().get(0));
        assertEquals("nice == false", spec.getRules().get(0).getWhen().get(1));
        assertEquals("$manager.name is not nice", ((Map<String, String>)spec.getRules().get(0).getThen().getMultipleOutput()).get("message"));
        // Rule 1
        assertEquals(2, spec.getRules().get(1).getWhen().size());
        assertEquals("manager.name != Joe", spec.getRules().get(1).getWhen().get(0));
        assertEquals("nice == true", spec.getRules().get(1).getWhen().get(1));
        assertEquals("$manager.name is nice", ((Map<String, String>)spec.getRules().get(1).getThen().getMultipleOutput()).get("message"));
        // Rule 2
        assertEquals(1, spec.getRules().get(2).getWhen().size());
        assertEquals("manager.name == Joe", spec.getRules().get(2).getWhen().get(0));
        assertEquals("$manager.name is always nice", ((Map<String, String>)spec.getRules().get(2).getThen().getMultipleOutput()).get("message"));
    }

    private JsonArray buildInputs() {
        return Json.createArrayBuilder()
                   .add(Json.createObjectBuilder().add("name", "manager").add("type", "Person"))
                   .add(Json.createObjectBuilder().add("name", "employee").add("type", "Person"))
                   .add(Json.createObjectBuilder().add("name", "nice").add("type", "boolean"))
                   .build();
    }

    private JsonArray buildTypes() {
        JsonArrayBuilder fields = Json.createArrayBuilder()
                                      .add(Json.createObjectBuilder().add("name", "name").add("type", "string"))
                                      .add(Json.createObjectBuilder().add("name", "age").add("type", "integer"));
        return Json.createArrayBuilder().add(Json.createObjectBuilder().add("name", "Person").add("fields", fields)).build();
    }

    private JsonArray buildRules() {
        return Json.createArrayBuilder()
                   .add(Json.createObjectBuilder()
                            .add("when", Json.createArrayBuilder()
                                             .add("manager.name != Joe")
                                             .add("nice == false"))
                            .add("then", Json.createObjectBuilder()
                                             .add("output", Json.createObjectBuilder()
                                                                .add("message", "$manager.name is not nice"))))
                   .add(Json.createObjectBuilder()
                            .add("when", Json.createArrayBuilder()
                                             .add("manager.name != Joe")
                                             .add("nice == true"))
                            .add("then", Json.createObjectBuilder()
                                             .add("output", Json.createObjectBuilder()
                                                                .add("message", "$manager.name is nice"))))
                   .add(Json.createObjectBuilder()
                            .add("when", Json.createArrayBuilder()
                                             .add("manager.name == Joe"))
                            .add("then", Json.createObjectBuilder()
                                             .add("output", Json.createObjectBuilder()
                                                                .add("message", "$manager.name is always nice"))))
                   .build();
    }

    private Spec parseSpec(JsonObject json) throws IOException {
        return new ObjectMapper().readValue(json.toString(), Spec.class);
    }
}
