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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class SessionFactory {
    private static SessionFactory INSTANCE;
    private KieContainer kieContainer;
    private static Map<String, String> inputTypeMap;
    private static String outputType;

    private SessionFactory(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public static void init() {
        KieServices ks = KieServices.Factory.get();
        KieRepository kr = ks.getRepository();
        KieFileSystem kfs = ks.newKieFileSystem();

        kfs.write("src/main/resources/org/kiegroup/zenithr/drools/rule.drl", getRules());

        KieBuilder kb = ks.newKieBuilder(kfs);

        kb.buildAll(); // kieModule is automatically deployed to KieRepository if successfully built.
        if (kb.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
        }

        KieContainer kieContainer = ks.newKieContainer(kr.getDefaultReleaseId());
        INSTANCE = new SessionFactory(kieContainer);
    }

    public static SessionFactory getInstance() {
        if (INSTANCE == null) {
            synchronized (SessionFactory.class) {
                if (INSTANCE == null) {
                    init();
                }
            }
        }
        return INSTANCE;
    }

    KieSession newKieSession() {
        return kieContainer.newKieSession();
    }

    private static String getRules() {
        StringWriter drl = new StringWriter();
        drl.append(RULE_HEADER);

        String getJson = System.getProperty("GET");
        System.out.println(getJson);
        JsonObject spec = Json.createReader(new StringReader(getJson)).readObject();
        inputTypeMap = getDataTypeMap(spec.getJsonArray("input"));
        outputType = spec.getJsonObject("output").getString("type");
        List<JsonObject> rules = spec.getJsonArray("rules").getValuesAs(JsonObject.class);
        for (JsonObject rule : rules) {
            drl.append(getRule(rule, inputTypeMap, outputType)).append('\n');
        }

        System.out.println(drl.toString());
        return drl.toString();
    }

    private static Map<String, String> getDataTypeMap(JsonArray jsonArray) {
        Map<String, String> dataTypeMap = new LinkedHashMap<>();
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
        String output = ruleObject.getJsonObject("then").getString("output");
        output = qualifyGetters(output, inputTypes);
        ruleString.append("\t").append("output.").append(getSetter(outputType)).append('(').append(getValue(outputType, output)).append(");\n");
        ruleString.append("end");
        return ruleString.toString();
    }

    private static String getValue(String type, String value) {
        switch (type) {
            case "string":
                return '"' + value + '"';
            case "boolean":
            case "int":
            case "double":
            case "long":
            case "date":
            case "datetime":
            default:
                return value;
        }
    }

    private static String qualifyFields(String when, Map<String, String> inputTypes) {
        for (String name : inputTypes.keySet()) {
            String type = inputTypes.get(name);
            String qualifiedField = name + "." + getFieldName(type);
            when = when.replaceAll(name, qualifiedField);
        }
        return when;
    }

    private static String qualifyGetters(String when, Map<String, String> inputTypes) {
        for (String name : inputTypes.keySet()) {
            String type = inputTypes.get(name);
            String qualifiedField = name + "." + getGetter(type) + "()";
            when = when.replaceAll(name, qualifiedField);
        }
        return when;
    }

    private static String getSetter(String type) {
        switch (type) {
            case "boolean":
                return "setBooleanValue";
            case "int":
                return "setIntValue";
            case "double":
                return "setDoubleValue";
            case "long":
                return "setLongValue";
            case "date":
                return "setDateValue";
            case "datetime":
                return "setDateValue";
            case "string":
                return "setStringValue";
            default:
                return null;
        }
    }

    private static String getGetter(String type) {
        switch (type) {
            case "boolean":
                return "getBooleanValue";
            case "int":
                return "getIntValue";
            case "double":
                return "getDoubleValue";
            case "long":
                return "getLongValue";
            case "date":
                return "getDateValue";
            case "datetime":
                return "getDateValue";
            case "string":
                return "getStringValue";
            default:
                return null;
        }
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

    List<FactField> getFacts(Map<String, String[]> parameters) {
        List<FactField> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : inputTypeMap.entrySet()) {
            String name = entry.getKey();
            String[] paramValues = parameters.get(name);
            if (paramValues != null) {
                FactField factField = getFact(entry.getValue(), paramValues[0]);
                factField.setName(name);
                list.add(factField);
            }
        }
        return list;
    }

    private FactField getFact(String type, String value) throws NumberFormatException {
        FactField factField = new FactField();
        switch (type) {
            case "boolean":
                factField.setBooleanValue(Boolean.parseBoolean(value));
                break;
            case "int":
                factField.setIntValue(Integer.parseInt(value));
                break;
            case "double":
                factField.setDoubleValue(Double.parseDouble(value));
                break;
            case "long":
                factField.setLongValue(Long.parseLong(value));
                break;
            case "date":
                factField.setDateValue(new Date(LocalDate.parse(value).toEpochDay()));
                break;
            case "datetime":
                factField.setDateValue(new Date(LocalDateTime.parse(value).toEpochSecond(ZoneOffset.UTC)));
                break;
            case "string":
                factField.setStringValue(value);
                break;
        }
        return factField;
    }

    Object getOutputObject(FactField output) {
        switch (outputType) {
            case "boolean":
                return output.getBooleanValue();
            case "int":
                return output.getIntValue();
            case "double":
                return output.getDoubleValue();
            case "long":
                return output.getLongValue();
            case "date":
                return output.getDateValue();
            case "datetime":
                return output.getDateValue();
            case "string":
                return output.getStringValue();
            default:
                return null;
        }
    }

    public Map<String, String> getInputTypeMap() {
        return Collections.unmodifiableMap(inputTypeMap);
    }

    private static final String RULE_HEADER =
            "" +
                    "package org.kiegroup.zenithr.drools \n\n" +
                    "import org.kiegroup.zenithr.drools.FactField \n\n";
}
