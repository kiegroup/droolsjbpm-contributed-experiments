package org.kiegroup.zenithr.drools;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionFactory {
    private static SessionFactory INSTANCE;
    private KieContainer kieContainer;

    private SessionFactory(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public static SessionFactory getInstance() {
        if (INSTANCE == null) {
            synchronized (SessionFactory.class) {
                if (INSTANCE == null) {
                    KieServices ks = KieServices.Factory.get();
                    KieRepository kr = ks.getRepository();
                    KieFileSystem kfs = ks.newKieFileSystem();

                    kfs.write("src/main/resources/org/kiegroup/zenithr/drools/rule.drl", getRule());

                    KieBuilder kb = ks.newKieBuilder(kfs);

                    kb.buildAll(); // kieModule is automatically deployed to KieRepository if successfully built.
                    if (kb.getResults().hasMessages(Message.Level.ERROR)) {
                        throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
                    }

                    KieContainer kieContainer = ks.newKieContainer(kr.getDefaultReleaseId());
                    INSTANCE = new SessionFactory(kieContainer);
                }
            }
        }
        return INSTANCE;
    }

    KieSession newKieSession() {
        return kieContainer.newKieSession();
    }

    private static String getRule() {
        StringWriter drl = new StringWriter();
        drl.append(RULE_HEADER);

        String getJson = System.getProperty("GET");
        System.out.println(getJson);
        JsonObject spec = Json.createReader(new StringReader(getJson)).readObject();
        Map<String, String> inputTypeMap = getDataTypeMap(spec.getJsonArray("input"));
        String outputType = spec.getJsonObject("output").getString("type");
        List<JsonObject> rules = spec.getJsonArray("rules").getValuesAs(JsonObject.class);
        for (JsonObject rule : rules) {
            drl.append(getRule(rule, inputTypeMap, outputType)).append('\n');
        }

        System.out.println(drl.toString());
        return drl.toString();
    }

    private static Map<String, String> getDataTypeMap(JsonArray jsonArray) {
        Map<String, String> dataTypeMap = new HashMap<>();
        for (JsonObject entry : jsonArray.getValuesAs(JsonObject.class)) {
            dataTypeMap.put(entry.getString("name"), entry.getString("type"));
        }
        return dataTypeMap;
    }

    private static String getRule(JsonObject ruleObject, Map<String, String> inputTypes, String outputType) {
        StringWriter ruleString = new StringWriter();
        String when = ruleObject.getString("when");
        String name = ruleObject.getString("name", when);
        ruleString.append("rule ").append('"').append(name).append('"').append(" when").append('\n');
        for (String input : inputTypes.keySet()) {
            ruleString.append('\t').append(input).append(": FactField(name == ").append('"').append(input).append('"').append(")\n");
        }
        when = qualifyFields(when, inputTypes);
        ruleString.append('\t').append("output: FactField(name == ").append('"').append("output").append('"').append(", ").append(when).append(")\n");
        ruleString.append("then \n");
        String then = ruleObject.getJsonObject("then").getString("output");
        ruleString.append("\t").append("output.setStringValue(").append('"').append(then).append('"').append(");").append(";\n");
        ruleString.append("end");
        return ruleString.toString();
    }

    private static String qualifyFields(String when, Map<String, String> inputTypes) {
        for (String name : inputTypes.keySet()) {
            String type = inputTypes.get(name);
            String qualifiedField = name + "." + getFieldName(type);
            when = when.replaceAll(name, qualifiedField);
        }
        return when;
    }

    private static String getFieldName(String type) {
        switch (type) {
            case "boolean":
                return "booleanValue";
            case "int":
                return "intValue";
            case "double":
                return "doubleValue";
            case "long":
                return "longValue";
            case "date":
                return "dateValue";
            case "datetime":
                return "dateValue";
            case "string":
                return "stringValue";
            default:
                return null;
        }
    }

    private static final String RULE_HEADER =
            "" +
                    "package org.kiegroup.zenithr.drools \n\n" +
                    "import org.kiegroup.zenithr.drools.FactField \n\n";
}
