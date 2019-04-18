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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kiegroup.zenithr.drools.model.Fact;
import org.kiegroup.zenithr.drools.model.exceptions.InvalidSpecException;
import org.kiegroup.zenithr.drools.service.impl.RuleServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RuleServiceTest {

    @Test
    public void testGradeLetters() throws Exception {
        RuleService ruleService = getRuleService("grade-letters.json");

        assertEquals("A", grade(ruleService, 92.5));
        assertEquals("B", grade(ruleService, 84.5));
        assertEquals("C", grade(ruleService, 70d));
        assertEquals("D", grade(ruleService, 69.99));
        assertEquals("F", grade(ruleService, 45d));
    }

    @Test
    public void testGradeLettersNull() throws Exception {
        RuleService ruleService = getRuleService("grade-letters.json");
        Map<String, String> parameters = new HashMap<>();
        parameters.put("grade", null);
        assertNull(((Fact) ruleService.process(parameters)).getString());
    }

    private String grade(RuleService ruleService, double value) throws Exception {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("grade", String.valueOf(value));
        return ((Fact) ruleService.process(parameters)).getString();
    }

    @Test
    public void testAverageGrade() throws Exception {
        RuleService ruleService = getRuleService("average-grade.json");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("math", String.valueOf(96d));
        parameters.put("physics", String.valueOf(95d));
        parameters.put("biology", String.valueOf(91d));
        Object output = ruleService.process(parameters);
        assertNotNull(output);
        assertEquals(Double.valueOf(94d), output.getClass().getMethod("getAverage").invoke(output));
    }

    @Test
    public void testComplexObjectsRule0() throws Exception {
        RuleService ruleService = getRuleService("complex-objects.json");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("person", "{\"name\":\"Kermit\",\"age\":16}");
        String result = ((Fact) ruleService.process(parameters)).getString();
        assertEquals("Kermit can't drive", result);
    }

    @Test
    public void testComplexObjectsRule1() throws Exception {
        RuleService ruleService = getRuleService("complex-objects.json");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("person", "{\"name\":\"Joe\",\"age\":18}");
        String result = ((Fact) ruleService.process(parameters)).getString();
        assertEquals("I don't know you but you can drive", result);
    }

    @Test
    public void testInvalidSpecEmpty() throws Exception {
        Assertions.assertThrows(InvalidSpecException.class, () -> {
        getRuleService("invalid-empty.json");
        });
    }

    @Test
    public void testParseMissingInput() throws IOException {
        Assertions.assertThrows(InvalidSpecException.class, () -> {
            getRuleService("invalid-missing-input.json");
        });
    }

    @Test
    public void testParseMissingRules() throws IOException {
        Assertions.assertThrows(InvalidSpecException.class, () -> {
            getRuleService("invalid-missing-rules.json");
        });
    }

    @Test
    public void testParseMissingOutput() throws IOException {
        Assertions.assertThrows(InvalidSpecException.class, () -> {
            getRuleService("invalid-missing-output.json");
        });
    }

    @Test
    public void testInvalidOutput() throws IOException {
        Assertions.assertThrows(InvalidSpecException.class, () -> {
            getRuleService("invalid-unknown-output.json");
        });
    }

    @Test
    public void testParseInputsUnknownType() throws IOException {
        Assertions.assertThrows(InvalidSpecException.class, () -> {
            getRuleService("invalid-unknown-input.json");
        });
    }

    private RuleService getRuleService(String file) {
        return new RuleServiceImpl(TestSessionFactory.getInstance(file));
    }

}
