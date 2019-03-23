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

import org.junit.Assert;
import org.junit.Test;
import org.kiegroup.zenithr.drools.model.Fact;
import org.kiegroup.zenithr.drools.model.exceptions.InvalidSpecException;
import org.kiegroup.zenithr.drools.service.impl.RuleServiceImpl;

public class RuleServiceTest {

    @Test
    public void testGradeLetters() throws Exception {
        RuleService ruleService = getRuleService("grade-letters.json");

        Assert.assertEquals("A", grade(ruleService, 92.5));
        Assert.assertEquals("B", grade(ruleService, 84.5));
        Assert.assertEquals("C", grade(ruleService, 70d));
        Assert.assertEquals("D", grade(ruleService, 69.99));
        Assert.assertEquals("F", grade(ruleService, 45d));
    }

    @Test
    public void testGradeLettersNull() throws Exception {
        RuleService ruleService = getRuleService("grade-letters.json");
        Map<String, String> parameters = new HashMap<>();
        parameters.put("grade", null);
        Assert.assertNull(((Fact) ruleService.process(parameters)).getString());
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
        Assert.assertNotNull(output);
        Assert.assertEquals(Double.valueOf(94d), output.getClass().getMethod("getAverage").invoke(output));
    }

    @Test
    public void testComplexObjectsRule0() throws Exception {
        RuleService ruleService = getRuleService("complex-objects.json");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("person", "{\"name\":\"Kermit\",\"age\":16}");
        String result = ((Fact) ruleService.process(parameters)).getString();
        Assert.assertEquals("Kermit can't drive", result);
    }

    @Test
    public void testComplexObjectsRule1() throws Exception {
        RuleService ruleService = getRuleService("complex-objects.json");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("person", "{\"name\":\"Joe\",\"age\":18}");
        String result = ((Fact) ruleService.process(parameters)).getString();
        Assert.assertEquals("I don't know you but you can drive", result);
    }

    @Test(expected = InvalidSpecException.class)
    public void testInvalidSpecEmpty() throws Exception {
        getRuleService("invalid-empty.json");
    }

    @Test(expected = InvalidSpecException.class)
    public void testParseMissingInput() throws IOException {
        getRuleService("invalid-missing-input.json");
    }

    @Test(expected = InvalidSpecException.class)
    public void testParseMissingRules() throws IOException {
        getRuleService("invalid-missing-rules.json");
    }

    @Test(expected = InvalidSpecException.class)
    public void testParseMissingOutput() throws IOException {
        getRuleService("invalid-missing-output.json");
    }

    @Test(expected = InvalidSpecException.class)
    public void testInvalidOutput() throws IOException {
        getRuleService("invalid-unknown-output.json");
    }

    @Test(expected = InvalidSpecException.class)
    public void testParseInputsUnknownType() throws IOException {
        getRuleService("invalid-unknown-input.json");
    }

    private RuleService getRuleService(String file) {
        return new RuleServiceImpl(TestSessionFactory.getInstance(file));
    }

}
