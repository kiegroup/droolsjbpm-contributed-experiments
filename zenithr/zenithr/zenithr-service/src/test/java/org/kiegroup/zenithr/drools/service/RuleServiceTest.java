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

package org.kiegroup.zenithr.drools.service;

import java.io.StringReader;
import java.util.Collection;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.kiegroup.zenithr.drools.FileUtils;
import org.kiegroup.zenithr.drools.model.Output;
import org.kiegroup.zenithr.drools.service.impl.TestRuleServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RuleServiceTest {

    @Test
    public void testSysOpsShouldScaleDown() {
        RuleService ruleService = getRuleService("sysops-example.json");
        String object = FileUtils.readObject("deployment-replicas1.json");
        JsonObject deployment = Json.createReader(new StringReader(object)).readObject();
        JsonObject inputs = Json.createObjectBuilder().add("deployment1", deployment).build();
        Collection<Output> result = ruleService.process(inputs);
        assertNotNull(result);
        assertEquals(2, result.size());
        Output firstOutput = Output.withName("deployment2").withPath("spec").withValue("{\"replicas\":1}");
        hasOutput(firstOutput, result);
        Output secondOutput = Output.withName("deployment3").withPath("spec").withValue("{\"replicas\":0}");
        hasOutput(secondOutput, result);
    }

    @Test
    public void testSysOpsShouldScaleUp() {
        RuleService ruleService = getRuleService("sysops-example.json");
        String object = FileUtils.readObject("deployment-replicas3.json");
        JsonObject deployment = Json.createReader(new StringReader(object)).readObject();
        JsonObject inputs = Json.createObjectBuilder().add("deployment1", deployment).build();
        Collection<Output> result = ruleService.process(inputs);
        assertNotNull(result);
        assertEquals(2, result.size());
        Output firstOutput = Output.withName("deployment2").withPath("spec").withValue("{\"replicas\":\"2\"}");
        hasOutput(firstOutput, result);
        Output secondOutput = Output.withName("deployment3").withPath("metadata.labels").withValue("{\"example\":\"default-broker-filter\"}");
        hasOutput(secondOutput, result);
    }

    private void hasOutput(Output expected, Collection<Output> outputs) {
        Optional<Output> actual = outputs.stream().filter(o -> expected.getName().equals(o.getName())).findFirst();
        assertTrue(actual.isPresent(), "Missing expected output: " + expected.getName());
        assertEquals(expected.getName(), actual.get().getName(), expected.getName());
        assertEquals(expected.getPath(), actual.get().getPath(), expected.getName());
        assertEquals(expected.getValue(), actual.get().getValue(), expected.getName());
    }

    private RuleService getRuleService(String file) {
        SessionFactory sessionFactory = new TestSessionFactory(file);
        sessionFactory.initialize();

        return new TestRuleServiceImpl(sessionFactory);
    }
}
