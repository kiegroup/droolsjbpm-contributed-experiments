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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.kiegroup.zenithr.drools.json.DeclaredTypeMapDeserializer;
import org.kiegroup.zenithr.drools.json.InputMapDeserializer;
import org.kiegroup.zenithr.drools.json.RuleListDeserializer;
import org.kiegroup.zenithr.drools.model.exceptions.InvalidSpecException;

public class Spec {

    private static final String DEFAULT_SPEC_NAME = "Zenithr";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TYPES = "types";
    public static final String FIELD_INPUTS = "inputs";
    public static final String FIELD_RULES = "rules";
    public static final String FIELD_OUTPUT = "output";
    
    @JsonProperty
    private String name;
    @JsonProperty
    @JsonDeserialize(using = DeclaredTypeMapDeserializer.class)
    private Map<String, DeclaredType> types = new HashMap<>();
    @JsonProperty
    @JsonDeserialize(using = InputMapDeserializer.class)
    private Map<String, Input> inputs = new HashMap<>();
    @JsonProperty
    @JsonDeserialize(using = RuleListDeserializer.class)
    private List<Rule> rules = new ArrayList<>();
    @JsonProperty
    private String output;

    public Spec() {
        this.name = DEFAULT_SPEC_NAME;
    }
    
    public String getName() {
        return name;
    }

    public Spec setName(String name) {
        this.name = name;
        return this;
    }

    public Map<String, DeclaredType> getTypes() {
        return types;
    }

    public Map<String, Input> getInputs() {
        return inputs;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public Spec setRules(List<Rule> rules) {
        this.rules = rules;
        return this;
    }
    
    public String getOutput() {
        return output;
    }
    
    public void setOutput(String output) {
        this.output = output;
    }

    public void validateType(String type) {
        if (FieldType.fromString(type) == null && this.types.get(type) == null) {
            throw new InvalidSpecException("Invalid type: " + type);
        }
    }   

    public boolean isValidType(String type) {
        if (FieldType.fromString(type) != null) {
            return true;
        }
        return types.containsKey(type);
    }

    public boolean mustParse(String input, String field) {
        String type = inputs.get(input).getType();
        FieldType fieldType = FieldType.fromString(type);
        if (fieldType == null && !types.containsKey(type)) {
            throw new IllegalArgumentException("Declared type: " + type + " does not have field: " + field);
        }
        if(fieldType != null) {
            return mustParse(fieldType);
        }
        return mustParse(types.get(type).getDefinition().get(field));
    }

    private boolean mustParse(FieldType type) {
        if(type==null) {
            throw new IllegalArgumentException("Null type cannot be parsed");
        }
        switch(type) {
            case STRING:
                return true;
            default:
                return false;
        }
    }

}
