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
import java.io.StringWriter;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.drools.compiler.kie.builder.impl.KieRepositoryImpl;
import org.drools.compiler.kie.builder.impl.KieServicesImpl;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kiegroup.zenithr.drools.model.InputFact;
import org.kiegroup.zenithr.drools.model.Output;
import org.kiegroup.zenithr.drools.model.OutputFact;
import org.kiegroup.zenithr.drools.model.Rule;
import org.kiegroup.zenithr.drools.model.Spec;
import org.kiegroup.zenithr.drools.model.exceptions.InvalidSpecException;
import org.kiegroup.zenithr.drools.regex.ConverterUtils;
import org.kiegroup.zenithr.drools.service.impl.ZenithrSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SessionFactory {

    private static final Logger logger = LoggerFactory.getLogger(SessionFactory.class);

    public static final String DATE_PATTERN = "yyyy-mm-dd";
    public static final String DEFAULT_PACKAGE = "org.kiegroup.zenithr.drools";
    private static final String RULE_TEMPLATE = "rule \"%s\" when%s\nthen%s\nend\n";

    private static final Class<?>[] IMPORTS = {Output.class, OutputFact.class, InputFact.class};

    @ConfigProperty(name = "rules.definition")
    String jsonSpec;

    private KieContainer kieContainer;
    private Spec spec;

    @PostConstruct
    public void initialize() {
        this.spec = loadSpec();

        System.setProperty("drools.dateformat", DATE_PATTERN);
        KieServices ks = new KieServicesImpl();
        KieFileSystem kfs = ks.newKieFileSystem();

        kfs.write("src/main/resources/org/kiegroup/zenithr/drools/rule.drl", parseRules());

        KieBuilder kb = ks.newKieBuilder(kfs);

        if (kb.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
        }
        this.kieContainer = ks.newKieContainer(new KieRepositoryImpl().getDefaultReleaseId());
    }

    public Collection<Output> process(JsonObject inputs) {
        return new ZenithrSession(kieContainer, inputs).process();
    }

    private Spec loadSpec() {
        logger.debug("Loaded rules spec: {}", jsonSpec);
        Spec spec;
        try {
            ObjectMapper mapper = new ObjectMapper();
            spec = mapper.readValue(jsonSpec, Spec.class);
        } catch (IOException e) {
            throw new InvalidSpecException("Unable to deserialize Spec", e);
        }
        validateSpec(spec);
        return spec;
    }

    private void validateSpec(Spec spec) {
        if (spec.getInputs().isEmpty() || spec.getRules().isEmpty()) {
            throw new InvalidSpecException("Missing one or more required values in the spec: [inputs, rules]");
        }
    }

    private String parseRules() {
        StringWriter drl = new StringWriter();
        drl.append(String.format("package %s \n\n", DEFAULT_PACKAGE));
        drl.append(getImports());
        spec.getRules().forEach(r -> drl.append(parseRule(r)).append('\n'));
        logger.debug("Generated rules file: {}", drl);
        return drl.toString();
    }

    private String parseRule(Rule rule) {
        StringBuilder when = new StringBuilder();
        this.spec.getInputs()
            .stream()
            .map(i -> ConverterUtils.getInputFacts(rule, i))
            .forEach(r -> when.append("\n\t").append(r));
        when.append("\n\toutputs: OutputFact()");

        StringBuilder then = new StringBuilder();
        rule.getThen()
            .stream()
            .map(this::parseThen)
            .forEach(t -> then.append("\n\t").append(t));
        return String.format(RULE_TEMPLATE, rule.getName(), when.toString(), then.toString());
    }

    private String parseThen(Output output) {
        StringBuilder o = new StringBuilder("outputs.add(Output");
        if (output.getName() != null && !output.getName().trim().isEmpty()) {
            o.append(".withName(\"").append(output.getName()).append("\")");
        }
        if (output.getPath() != null && !output.getPath().trim().isEmpty()) {
            o.append(".withPath(\"").append(output.getPath()).append("\")");
        }
        if (output.getValue() != null && !output.getValue().trim().isEmpty()) {
            o.append(".withValue(")
                .append(ConverterUtils.replaceVariables(output.getValue()))
                .append(")");
        }
        return o.append(");").toString();
    }

    private String getImports() {
        StringWriter imports = new StringWriter();
        for (Class<?> c : IMPORTS) {
            imports.append("import ").append(c.getName()).append("\n");
        }
        return imports.append("\n").toString();
    }
}
