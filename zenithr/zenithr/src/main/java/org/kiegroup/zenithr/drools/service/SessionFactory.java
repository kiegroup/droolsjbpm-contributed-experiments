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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kiegroup.zenithr.drools.model.Fact;
import org.kiegroup.zenithr.drools.model.FieldType;
import org.kiegroup.zenithr.drools.model.Input;
import org.kiegroup.zenithr.drools.model.OutputFact;
import org.kiegroup.zenithr.drools.model.Rule;
import org.kiegroup.zenithr.drools.model.Spec;
import org.kiegroup.zenithr.drools.model.Then;
import org.kiegroup.zenithr.drools.model.exceptions.InvalidSpecException;
import org.kiegroup.zenithr.drools.regex.ConverterUtils;
import org.kiegroup.zenithr.drools.service.impl.ZenithrSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SessionFactory {

    private static final Logger logger = LoggerFactory.getLogger(SessionFactory.class);

    public static final String DATE_PATTERN = "yyyy-mm-dd";
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat(SessionFactory.DATE_PATTERN);
    public static final String DEFAULT_PACKAGE = "org.kiegroup.zenithr.drools";

    private static final String RULE_TEMPLATE = "rule \"%s\" when%s\nthen%s\nend\n";
    private static final String FACT_TEMPLATE = "\n\t%s: Fact(id == \"%s\" && %s)";
    private static final String DECLARED_FACT_TEMPLATE = "\n\t%s: %s(%s)";
    private static final Pattern SIMPLE_TYPE_CONDITION_PATTERN = Pattern.compile("^(\\w+)\\s*([=<>\\!]+)\\s(.+)$");
    private static final Pattern COMPOSED_TYPE_CONDITION_PATTERN = Pattern.compile("^(\\w+)\\.(\\w+)\\s*([=<>\\!]+)\\s*(.+)$");

    @ConfigProperty(name = "rules.definition")
    String jsonSpec;
    
    private KieContainer kieContainer;
    private Spec spec;
    private ConverterUtils converter;
    private String serviceName;

    @PostConstruct
    public void initialize() {
        this.spec = loadSpec();
        this.converter = new ConverterUtils(spec);
        this.serviceName = spec.getName();

        System.setProperty("drools.dateformat", DATE_PATTERN);
        KieServices ks = KieServices.Factory.get();
        KieRepository kr = ks.getRepository();
        KieFileSystem kfs = ks.newKieFileSystem();

        kfs.write("src/main/resources/org/kiegroup/zenithr/drools/rule.drl", getRules());

        KieBuilder kb = ks.newKieBuilder(kfs);

        kb.buildAll(); // kieModule is automatically deployed to KieRepository if successfully built.
        if (kb.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
        }
        this.kieContainer = ks.newKieContainer(kr.getDefaultReleaseId());
    }

    public Session getSession() {
        try {
            return new ZenithrSession(kieContainer, getOutputInstance());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to create the output instance", e);
        }
    }

    private Spec loadSpec() {
        logger.debug("Loaded rules spec: {}", jsonSpec);
        Spec spec = null;
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
        if(spec.getOutput() == null || spec.getInputs().isEmpty() || spec.getRules().isEmpty()) {
            throw new InvalidSpecException("Missing one or more required values in the spec: [output, inputs, rules]");
        }
        spec.getInputs().forEach((k, input) -> spec.validateType(input.getType()));
        spec.validateType(spec.getOutput());
        //TODO: Validate k8s types
    }

    private String getRules() {
        StringWriter drl = new StringWriter();
        drl.append(String.format("package %s \n\n", DEFAULT_PACKAGE));
        drl.append(getImports());
        drl.append(getDeclares());
        List<Rule> rules = spec.getRules();
        drl.append(getRule(rules)).append('\n');
        logger.debug("Generated rules file: {}", drl);
        return drl.toString();
    }

    private String getImports() {
        StringWriter imports = new StringWriter();
        imports.append("import ").append(Fact.class.getName()).append("\n");
        return imports.append("\n").toString();
    }

    private String getDeclares() {
        StringWriter declares = new StringWriter();
        spec.getTypes().values().forEach(type -> {
            declares.append("declare ").append(type.getName());
            type.getDefinition().forEach((name, fieldType) -> {
                declares.append("\n\t").append(name).append(" : ").append(fieldType.getTypeClass().getName());
            });
            declares.append("\nend\n");
        });
        return declares.toString();
    }

    private String getRule(List<Rule> rules) {
        StringWriter rulesDef = new StringWriter();
        for (int index = 0; index < rules.size(); index++) {
            Rule rule = rules.get(index);
            StringBuilder when = new StringBuilder();
            Map<String, Set<String>> conditions = new HashMap<>();
            for (String whenSpec : rule.getWhen()) {
                if (!addSimpleCondition(conditions, whenSpec)) {
                    addComposedCondition(conditions, whenSpec);
                }
            }
            conditions.entrySet().forEach(e -> when.append(buildFact(e.getKey(), e.getValue())));
            if(FieldType.fromString(spec.getOutput()) != null) {
                when.append("\n\toutput: Fact(id == \"output\")");
            } else {
                when.append(String.format("\n\toutput: %s()", spec.getOutput()));
            }
            StringBuilder then = new StringBuilder();
            then.append(parseOutput(rule.getThen()));
            rulesDef.append(String.format(RULE_TEMPLATE, rule.getName(), when, then));
        }
        return rulesDef.toString();
    }

    private String parseOutput(Then then) {
        StringWriter writer = new StringWriter();
        if (!then.isMultiple()) {
            return writer.append("\n\t").append(converter.parseOutput(then.getSingleOutput())).toString();
        }
        then.getMultipleOutput()
            .forEach((left, right) -> writer.append("\n\t").append(converter.parseOutput(left, right)));
        return writer.toString();
    }

    private boolean addSimpleCondition(Map<String, Set<String>> conditions, String condition) {
        Matcher matcher = SIMPLE_TYPE_CONDITION_PATTERN.matcher(condition);
        if (matcher.matches()) {
            String type = matcher.group(1);
            String fact = formatFact(type, null, matcher.group(2), matcher.group(3));
            if (!conditions.containsKey(type)) {
                conditions.put(type, new HashSet<>());
            }
            conditions.get(type).add(fact);
        }
        return matcher.matches();
    }

    private boolean addComposedCondition(Map<String, Set<String>> conditions, String condition) {
        Matcher matcher = COMPOSED_TYPE_CONDITION_PATTERN.matcher(condition);
        if (matcher.matches()) {
            String inputName = matcher.group(1);
            String field = matcher.group(2);
            String fact = String.format("%s %s %s", matcher.group(2), matcher.group(3), parse(inputName, field, matcher.group(4)));
            if (!conditions.containsKey(inputName)) {
                conditions.put(inputName, new HashSet<>());
            }
            conditions.get(inputName).add(fact);
        }
        return matcher.matches();
    }

    private String parse(String type, String field, String value) {
        if (spec.mustParse(type, field)) {
            value = "\"" + value + "\"";
        }
        return value;
    }

    private String formatFact(String type, String field, String symbol, String value) {
        value = parse(type, field, value);
        if (field == null) {
            field = type;
        }
        return String.format("%s %s %s", converter.getGetter(field), symbol, value);
    }

    private String buildFact(String factName, Set<String> conditions) {
        StringBuilder fact = new StringBuilder();
        conditions.forEach(c -> {
            if (fact.length() != 0) {
                fact.append(" && ");
            }
            fact.append(c);
        });
        String type = spec.getInputs().get(factName).getType();
        if (spec.getTypes().containsKey(type)) {
            return String.format(DECLARED_FACT_TEMPLATE, factName, type, fact.toString());
        } else {
            return String.format(FACT_TEMPLATE, factName, factName, fact.toString());
        }
    }

    public Spec getSpec() {
        return spec;
    }

    public List<String> getInputNames() {
        List<String> names = new ArrayList<>();
        spec.getInputs().forEach((k, v) -> names.add(k));
        return names;
    }

    public String getServiceName() {
        return serviceName;
    }

    private Object getOutputInstance() throws ReflectiveOperationException {
        if (FieldType.fromString(spec.getOutput()) != null) {
            return new OutputFact();
        } else if (spec.getTypes().containsKey(spec.getOutput())) {
            return kieContainer.getClassLoader().loadClass(DEFAULT_PACKAGE + "." + spec.getOutput()).newInstance();
        } else {
            // TODO: Return k8s instance
            return null;
        }
    }

    public Object getInputValue(String name, String literalValue) throws Exception {
        Input input = spec.getInputs().get(name);
        if (input == null) {
            throw new IllegalArgumentException("Unexpected input name: " + name);
        }
        if (!spec.isValidType(input.getType())) {
            throw new IllegalArgumentException("Invalid type received: " + input.getType());
        }
        if (literalValue == null) {
            return null;
        }
        Object value = getBasicTypeValue(name, FieldType.fromString(input.getType()), literalValue);
        if (value == null) {
            value = getDeclaredTypeValue(input.getType(), literalValue);
        }
        return value;
    }

    private Fact getBasicTypeValue(String name, FieldType type, String value) throws ParseException {
        Fact f = new Fact(name);
        if(type == null) {
            return null;
        }
        switch (type) {
            case STRING:
                return f.setString(value);
            case BOOLEAN:
                return f.setBoolean(value);
            case NUMBER:
                return f.setNumber(value);
            case INTEGER:
                return f.setInteger(value);
            default:
                return null;
        }
    }

    private Object getDeclaredTypeValue(String type, String literalValue) throws Exception {
        return new ObjectMapper().readValue(literalValue, kieContainer.getClassLoader().loadClass(DEFAULT_PACKAGE + "." + type));
    }

}
