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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.kiegroup.zenithr.drools.json.DefinitionMapDeserializer;

public class DeclaredType {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_FIELDS = "fields";
    public static final String FIELD_TYPE = "type";
    
    @JsonProperty
    private final String name;
    @JsonProperty
    @JsonDeserialize(using = DefinitionMapDeserializer.class)
    private final Map<String, FieldType> fields;

    @JsonCreator
    public DeclaredType(@JsonProperty(FIELD_NAME) String name, @JsonProperty(FIELD_FIELDS) Map<String, FieldType> fields) {
        this.name = name;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public Map<String, FieldType> getDefinition() {
        return fields;
    }

}
