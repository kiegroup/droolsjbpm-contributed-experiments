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

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kiegroup.zenithr.drools.model.InputFact;
import org.kiegroup.zenithr.drools.model.OutputFact;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RulesGenerationTest {

    @Test
    public void testSysOpsExample() throws Exception {
        KieSession session = createSession("sysops-example.drl");
        JsonObject deployment = Json.createObjectBuilder()
            .add("spec",
                 Json.createObjectBuilder()
                     .add("replicas", 3)
                     .build())
            .add("metadata", Json.createObjectBuilder()
                .add("name", "example")
                .build())
            .build();

        InputFact deploymentFact = InputFact.withName("deployment1").withValue(deployment.toString());
        OutputFact output = new OutputFact();

        session.insert(deploymentFact);
        session.insert(output);

        session.fireAllRules();
        session.dispose();

        assertEquals(2, output.get().size());
        output.get().contains("{\"replicas\": \"2\"}");
        output.get().contains("{\"result\": \"example\"}");
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
