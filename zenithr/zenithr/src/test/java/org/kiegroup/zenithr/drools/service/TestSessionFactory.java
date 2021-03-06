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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestSessionFactory extends SessionFactory {
    
    public TestSessionFactory(String specFileName) throws URISyntaxException, IOException {
        this.jsonSpec = readRulesDefinition(specFileName);
    }
    
    private static String readRulesDefinition(String fileName) throws URISyntaxException, IOException {
        Path path = Paths.get(TestSessionFactory.class.getClassLoader().getResource("definitions/" + fileName).toURI());
        Stream<String> lines = Files.lines(path);
        String definition = lines.collect(Collectors.joining("\n"));
        lines.close();
        return definition;
    }
}
