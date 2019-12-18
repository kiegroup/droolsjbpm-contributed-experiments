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

package org.kiegroup.zenithr.drools.regex;

import org.junit.jupiter.api.Test;
import org.kiegroup.zenithr.drools.model.Output;
import org.kiegroup.zenithr.drools.model.Rule;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConverterUtilsTest {

    @Test
    public void testGetInputFact() {
        Rule rule0 = new Rule()
            .addWhen("$deployment1.spec.template.spec.replicas >= 3")
            .addWhen("\"test\" == $deployment1.metadata.name")
            .addThen(
                Output.withName("deployment2")
                    .withPath("spec.template.spec")
                    .withValue("{\"replicas\": \"($deployment1.spec.template.spec.replicas / 2) + 1\"}"));

        assertEquals("deployment1: InputFact( getName() == \"deployment1\" && deployment1.greaterThanEquals(\"spec.template.spec.replicas\", 3) && deployment1.equalsTo(\"metadata.name\", \"test\"))",
                     ConverterUtils.getInputFacts(rule0, "deployment1"));
    }

    @Test
    public void testGetOutput() {
        String value = "{\"replicas\": \"$deployment1.spec.replicas / 2 + 1\"}";
        String expected = "\"{\\\"replicas\\\": \\\"\" + (deployment1.getNumber(\"spec.replicas\") / 2 + 1) + \"\\\"}\"";
        assertEquals(expected, ConverterUtils.replaceVariables(value));

        value = "{\"example\": \"$deployment1.metadata.name\"}";
        expected = "\"{\\\"example\\\": \\\"\" + deployment1.getString(\"metadata.name\") + \"\\\"}\"";
        assertEquals(expected, ConverterUtils.replaceVariables(value));
    }
}
