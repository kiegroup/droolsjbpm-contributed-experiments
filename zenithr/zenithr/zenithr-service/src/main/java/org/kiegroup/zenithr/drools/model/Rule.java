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
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Rule {

    public static final String RULE_PREFIX = "rule_";
    private static int index = 0;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String name;
    private final Collection<String> when;
    private final Collection<Output> then;

    @JsonCreator
    private Rule(@JsonProperty("when") Collection<String> when, @JsonProperty("then") Collection<Output> then) {
        this.name = RULE_PREFIX + index++;
        this.when = when;
        this.then = then;
    }

    public Rule() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public String getName() {
        return name;
    }

    public Collection<String> getWhen() {
        return when;
    }

    public Collection<Output> getThen() {
        return then;
    }


    public Rule addWhen(String when) {
        this.when.add(when);
        return this;
    }

    public Rule addThen(Output output) {
        this.then.add(output);
        return this;
    }
}
