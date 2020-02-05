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

import javax.json.Json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Output {

    private String name;
    private String path;
    private String value;

    @JsonCreator
    private Output(
        @JsonProperty("name") String name,
        @JsonProperty("path") String path,
        @JsonProperty("value") Map<String, Object> value
    ) {
        this.name = name;
        this.path = path;
        this.value = Json.createObjectBuilder(value).build().toString();
    }

    private Output(String name, String path, String value) {
        this.name = name;
        this.path = path;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getValue() {
        return value;
    }

    public static Output withName(String name) {
        return new Output(name, null, (String) null);
    }

    public Output withPath(String path) {
        return new Output(name, path, value);
    }

    public Output withValue(String value) {
        return new Output(name, path, value);
    }

    @Override
    public String toString() {
        return "Output{" +
            "name='" + name + '\'' +
            ", path='" + path + '\'' +
            ", value='" + value + '\'' +
            '}';
    }
}
