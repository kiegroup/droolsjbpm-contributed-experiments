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
        JsonObject deployment = FileUtils.readObject("deployment-replicas1.json");
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
        JsonObject deployment = FileUtils.readObject("deployment-replicas3.json");
        JsonObject inputs = Json.createObjectBuilder().add("deployment1", deployment).build();
        Collection<Output> result = ruleService.process(inputs);
        assertNotNull(result);
        assertEquals(2, result.size());
        Output firstOutput = Output.withName("deployment2").withPath("spec").withValue("{\"replicas\":\"2\"}");
        hasOutput(firstOutput, result);
        Output secondOutput = Output.withName("deployment3").withPath("metadata.labels").withValue("{\"example\":\"default-broker-filter\"}");
        hasOutput(secondOutput, result);
    }

    @Test
    public void testMultipleInputs_1Replica() {
        RuleService ruleService = getRuleService("multiple-inputs.json");
        JsonObject deployment = FileUtils.readObject("deployment-replicas1.json");
        JsonObject replicaSet = FileUtils.readObject("replicaset-replicas1.json");

        JsonObject inputs = Json.createObjectBuilder().add("deployment1", deployment).add("replicaSet1", replicaSet).build();
        Collection<Output> result = ruleService.process(inputs);

        assertNotNull(result);
        assertEquals(2, result.size());
        Output firstOutput = Output.withName("depConfig1").withPath("spec").withValue("{\"replicas\":\"1\"}");
        hasOutput(firstOutput, result);
        Output secondOutput = Output.withName("replicaSet1").withPath("spec").withValue("{\"replicas\":\"1\"}");
        hasOutput(secondOutput, result);
    }

    @Test
    public void testMultipleInputs_deployment3() {
        RuleService ruleService = getRuleService("multiple-inputs.json");
        JsonObject deployment = FileUtils.readObject("deployment-replicas3.json");
        JsonObject replicaSet = FileUtils.readObject("replicaset-replicas1.json");

        JsonObject inputs = Json.createObjectBuilder().add("deployment1", deployment).add("replicaSet1", replicaSet).build();
        Collection<Output> result = ruleService.process(inputs);

        assertNotNull(result);
        assertEquals(3, result.size());
        Output firstOutput = Output.withName("depConfig1").withPath("spec").withValue("{\"replicas\":\"2\"}");
        hasOutput(firstOutput, result);
        Output secondOutput = Output.withName("replicaSet1").withPath("metadata.labels").withValue("{\"example\":\"default-broker-filter\"}");
        hasOutput(secondOutput, result);
        Output thirdOutput = Output.withName("replicaSet1").withPath("spec").withValue("{\"replicas\":\"2\"}");
        hasOutput(thirdOutput, result);
    }

    @Test
    public void testMultipleInputs_replicaSet3() {
        RuleService ruleService = getRuleService("multiple-inputs.json");
        JsonObject deployment = FileUtils.readObject("deployment-replicas1.json");
        JsonObject replicaSet = FileUtils.readObject("replicaset-replicas3.json");

        JsonObject inputs = Json.createObjectBuilder().add("deployment1", deployment).add("replicaSet1", replicaSet).build();
        Collection<Output> result = ruleService.process(inputs);

        assertNotNull(result);
        assertEquals(3, result.size());
        Output firstOutput = Output.withName("depConfig1").withPath("spec").withValue("{\"replicas\":\"1\"}");
        hasOutput(firstOutput, result);
        Output secondOutput = Output.withName("replicaSet1").withPath("spec").withValue("{\"replicas\":\"1\"}");
        hasOutput(secondOutput, result);
        Output thirdOutput = Output.withName("route1").withPath("metadata.labels").withValue("{\"example\":\"default-broker-filter\"}");
        hasOutput(thirdOutput, result);
    }

    @Test
    public void testMultipleInputs_3replicas() {
        RuleService ruleService = getRuleService("multiple-inputs.json");
        JsonObject deployment = FileUtils.readObject("deployment-replicas3.json");
        JsonObject replicaSet = FileUtils.readObject("replicaset-replicas3.json");

        JsonObject inputs = Json.createObjectBuilder().add("deployment1", deployment).add("replicaSet1", replicaSet).build();
        Collection<Output> result = ruleService.process(inputs);

        assertNotNull(result);
        assertEquals(4, result.size());
        Output thirdOutput = Output.withName("route1").withPath("metadata.labels").withValue("{\"example\":\"default-broker-filter\"}");
        hasOutput(thirdOutput, result);
    }

    private void hasOutput(Output expected, Collection<Output> outputs) {
        assertTrue(outputs.stream().filter(o -> expected.getName().equals(o.getName())).anyMatch(o ->
            expected.getName().equals(o.getName())
                && expected.getPath().equals(o.getPath())
                && expected.getValue().equals(o.getValue())
        ), "Missing expected output: " + expected);
    }

    private RuleService getRuleService(String file) {
        SessionFactory sessionFactory = new TestSessionFactory(file);
        sessionFactory.initialize();

        return new TestRuleServiceImpl(sessionFactory);
    }
}
