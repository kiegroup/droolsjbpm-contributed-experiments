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

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kiegroup.zenithr.drools.model.DeclaredType;
import org.kiegroup.zenithr.drools.model.FieldType;
import org.kiegroup.zenithr.drools.model.Input;
import org.kiegroup.zenithr.drools.model.Spec;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConverterUtilsTest {

    @Test
    public void testGetOutputFieldTypeString() {
        Spec spec = buildSpec();
        spec.setOutput("string");
        ConverterUtils converter = new ConverterUtils(spec);
        assertEquals("output.setString(\"F\");", converter.parseOutput("F"));
        assertEquals("output.setString(String.format(\"Full name: %s %s\", user.getFirstName(), user.getLastName()));", converter.parseOutput("Full name: $user.firstName $user.lastName"));
    }

    @Test
    public void testGetOutputFieldTypeInteger() {
        Spec spec = buildSpec();
        spec.setOutput("integer");
        ConverterUtils converter = new ConverterUtils(spec);
        assertEquals("output.setInteger(34);", converter.parseOutput("34"));
        assertEquals("output.setInteger(user.getAge() * 2);", converter.parseOutput("$user.age * 2"));
    }
    
    @Test
    public void testGetOutputFieldTypeNumber() {
        Spec spec = buildSpec();
        spec.setOutput("number");
        ConverterUtils converter = new ConverterUtils(spec);
        assertEquals("output.setNumber((math.getNumber() + physics.getNumber() + biology.getNumber()) / 3);", converter.parseOutput("($math + $physics + $biology) / 3"));
    }
    
    @Test
    public void testGetOutputDeclaredType() {
        Spec spec = buildSpec();
        spec.setOutput("Results");
        ConverterUtils converter = new ConverterUtils(spec);
        assertEquals("output.setAverage((math.getNumber() + physics.getNumber() + biology.getNumber()) / 3);", converter.parseOutput("average", "($math + $physics + $biology) / 3"));
        assertEquals("output.setMax(math.getNumber());", converter.parseOutput("max", "$math"));
        assertEquals("output.setSubject(\"Math\");", converter.parseOutput("subject", "Math"));
    }
    
    @Test
    public void testGetter() {
        ConverterUtils converter = new ConverterUtils(buildSpec());
        assertEquals("name.getString()", converter.getGetter("name"));
        assertEquals("user.getAge()", converter.getGetter("user.age"));
        assertEquals("grade.getNumber()", converter.getGetter("grade"));
    }
    
    private Spec buildSpec() {
        Spec spec = new Spec();
        spec.getInputs().putAll(buildInputs());
        spec.getTypes().putAll(buildTypes());
        return spec;
    }

    private Map<String, DeclaredType> buildTypes() {
        Map<String, DeclaredType> types = new HashMap<>();
        Map<String, FieldType> resultsDefinition = new HashMap<>();
        resultsDefinition.put("average", FieldType.NUMBER);
        resultsDefinition.put("max", FieldType.NUMBER);
        resultsDefinition.put("subject", FieldType.STRING);
        types.put("Results", new DeclaredType("Results", resultsDefinition));
        
        Map<String, FieldType> errorMessageDefinition = new HashMap<>();
        errorMessageDefinition.put("message", FieldType.STRING);
        errorMessageDefinition.put("code", FieldType.INTEGER);
        types.put("Error", new DeclaredType("Error", errorMessageDefinition));
        
        Map<String, FieldType> personDefinition = new HashMap<>();
        personDefinition.put("firstName", FieldType.STRING);
        personDefinition.put("lastName", FieldType.STRING);
        personDefinition.put("age", FieldType.INTEGER);
        types.put("Person", new DeclaredType("Person", personDefinition));
        
        return types;
    }
    
    private Map<String, Input> buildInputs() {
        Map<String, Input> inputs = new HashMap<>();
        inputs.put("math", new Input("math", "number"));
        inputs.put("biology", new Input("biology", "number"));
        inputs.put("physics", new Input("physics", "number"));
        inputs.put("grade", new Input("grade", "number"));
        inputs.put("name", new Input("name", "string"));
        inputs.put("humble", new Input("humble", "boolean"));
        inputs.put("requiredAge", new Input("requiredAge", "integer"));
        inputs.put("user", new Input("user", "Person"));
        return inputs;
    }

}
