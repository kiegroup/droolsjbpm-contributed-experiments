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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kiegroup.zenithr.drools.model.Fact;
import org.kiegroup.zenithr.drools.model.OutputFact;

public class RulesGenerationTest {

    @Test
    public void testGradeLettersDRL() throws Exception {
        KieSession session = createSession("grade-letters.drl");
        Fact input = new Fact("grade");
        Fact output = new OutputFact();
        input.setNumber(Double.valueOf(91));
        session.insert(input);
        session.insert(output);
        session.fireAllRules();
        session.dispose();
        Assert.assertEquals("A", output.getString());
    }
    
    private KieSession createSession(String rulesFile) throws Exception {
        System.setProperty("drools.dateformat", SessionFactory.DATE_PATTERN);
        KieServices ks = KieServices.Factory.get();
        KieRepository kr = ks.getRepository();
        KieFileSystem kfs = ks.newKieFileSystem();

        Path path = Paths.get(this.getClass().getClassLoader().getResource("rules/" + rulesFile).toURI());
        try (Stream<String> lines = Files.lines(path)) {
            String definition = lines.collect(Collectors.joining("\n"));
            kfs.write("src/main/resources/org/kiegroup/zenithr/drools/rule.drl", definition);
        }
        KieBuilder kb = ks.newKieBuilder(kfs);

        kb.buildAll();
        if (kb.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
        }
        KieContainer kieContainer = ks.newKieContainer(kr.getDefaultReleaseId());
        
        return kieContainer.newKieSession();
    }
}
