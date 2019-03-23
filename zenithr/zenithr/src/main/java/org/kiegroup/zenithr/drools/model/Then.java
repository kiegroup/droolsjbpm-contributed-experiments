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

public class Then {

    public static final String FIELD_OUTPUT = "output";
    private final String singleOutput;
    private final Map<String, String> multipleOutput;

    public Then(String output) {
        this.singleOutput = output;
        this.multipleOutput = null;
    }

    public Then(Map<String, String> output) {
        this.singleOutput = null;
        this.multipleOutput = output;
    }

    public String getSingleOutput() {
        return singleOutput;
    }

    public Map<String, String> getMultipleOutput() {
        return multipleOutput;
    }

    public boolean isMultiple() {
        return multipleOutput != null;
    }

}
