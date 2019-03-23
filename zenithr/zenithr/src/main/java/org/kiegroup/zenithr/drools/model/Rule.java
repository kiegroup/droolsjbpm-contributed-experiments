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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.kiegroup.zenithr.drools.json.ThenDeserializer;

public class Rule {

    public static final String RULE_PREFIX = "rule_";
    public static final String FIELD_WHEN = "when";
    public static final String FIELD_THEN = "then";
    
    private String name;
    @JsonProperty
    private final List<String> when;
    @JsonProperty
    @JsonDeserialize(using = ThenDeserializer.class)
    private final Then then;

    @JsonCreator
    public Rule(@JsonProperty(FIELD_WHEN) List<String> when, @JsonProperty(FIELD_THEN) Then then) {
        this.when = when;
        this.then = then;
    }

    public String getName() {
        return name;
    }

    public List<String> getWhen() {
        return when;
    }

    public Then getThen() {
        return then;
    }

    public Rule setId(int i) {
        if(this.name != null) {
            throw new IllegalStateException("The ID must only be set once");
        }
        this.name = RULE_PREFIX + i;
        return this;
    }

}
