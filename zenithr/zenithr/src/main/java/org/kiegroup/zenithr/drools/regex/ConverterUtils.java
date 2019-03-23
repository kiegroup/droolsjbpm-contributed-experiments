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

package org.kiegroup.zenithr.drools.regex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kiegroup.zenithr.drools.model.FieldType;
import org.kiegroup.zenithr.drools.model.Spec;

public class ConverterUtils {

    private static final String VARIABLE_SEPARATOR = "\\.";
    private static final String VARIABLES_PATTERN = "\\$(\\w+(\\.\\w+)*)";

    private Map<String, FieldType> types = new HashMap<>();

    public ConverterUtils(Spec spec) {
        spec.getInputs().values().forEach(input -> addType(spec, input.getName(), input.getType()));
        addType(spec, Spec.FIELD_OUTPUT, spec.getOutput());
    }

    private void addType(Spec spec, String name, String type) {
        FieldType fieldType = FieldType.fromString(type);
        if (fieldType != null) {
            types.put(name, fieldType);
        } else if (spec.getTypes().containsKey(type)) {
            Map<String, FieldType> definition = spec.getTypes().get(type).getDefinition();
            definition.forEach((field, attributeType) -> types.put(name + "." + field, attributeType));
        }
    }

    public String parseOutput(String right) {
        return parseOutput(null, right);
    }

    /**
     * Only supports one level. Example: output type is Person, then parseOutput(average, ($a + $b) / 2)
     */
    public String parseOutput(String left, String right) {
        String type;
        String setter;
        if(left == null) {
            type = Spec.FIELD_OUTPUT;
            setter = "output." + types.get(Spec.FIELD_OUTPUT).getValue();
        } else {
            type = "output." + left;
            setter = "output." + left;
        }
        StringBuilder output = new StringBuilder();
        if (FieldType.STRING.equals(types.get(type))) {
            output.append(getSetter(setter, getStringRightSide(right)));
        } else {
            output.append(getSetter(setter, getObjectRightSide(right)));
        }
        return output.append(";").toString();
    }

    private String getStringRightSide(String right) {
        Pattern p = Pattern.compile(VARIABLES_PATTERN);
        Matcher m = p.matcher(right);
        StringBuffer b = new StringBuffer();
        List<String> methods = new ArrayList<>();
        while (m.find()) {
            methods.add(getGetter(m.group(1)));
            m.appendReplacement(b, "%s");
        }
        if (b.length() == 0) {
            return "\"" + right + "\"";
        }
        m.appendTail(b);
        StringBuffer result = new StringBuffer("String.format(\"").append(b.toString()).append("\"");
        methods.stream().forEach(s -> result.append(", ").append(s));
        result.append(")");
        return result.toString();
    }

    private String getObjectRightSide(String right) {
        Pattern p = Pattern.compile(VARIABLES_PATTERN);
        Matcher m = p.matcher(right);
        StringBuffer b = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(b, getGetter(m.group(1)));
        }
        if (b.length() == 0) {
            return right;
        }
        m.appendTail(b);
        return b.toString();
    }

    /**
     * Converts the provided text input into a getter. Examples:
     * <br><code>getGetter("person"); // "person"</code>
     * <br><code>getGetter("person.name"); // "person.getName()"</code>
     * <br><code>getGetter("foo.bar.baz"); // "foo.getBar().getBaz()"</code>
     * 
     * @param text The pattern to convert into a getter
     */
    public String getGetter(String text) {
        String[] parts = text.split(VARIABLE_SEPARATOR);
        if (parts.length == 0) {
            return "";
        }
        if (parts.length == 1) {
            return getGetter(text + "." + types.get(text).getValue());
        }
        StringBuffer getter = new StringBuffer(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            getter.append(".get").append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1)).append("()");
        }
        return getter.toString();
    }

    /**
     * Converts the provided text input into a setter. The value will be used as a string. Examples:
     * <br><code>getSetter("age", 34); // "age = 34"</code>
     * <br><code>getSetter("person.name", "\"Joe\""); // "person.setName(\"Joe\")"</code>
     * <br><code>getSetter("foo.bar.baz", false); // "foo.getBar().setBaz(false)"</code>
     * 
     * @param text The pattern to convert into a setter
     * @param value The value to set to the setter
     */
    private String getSetter(String text, Object value) {
        String[] parts = text.split(VARIABLE_SEPARATOR);
        if (parts.length == 0) {
            return "";
        }
        StringBuffer setter = new StringBuffer(parts[0]);
        if (parts.length == 1) {
            return getSetter(text + "." + types.get(text).getValue(), value);
        } else {
            for (int i = 1; i < parts.length; i++) {
                if (i == parts.length - 1) {
                    setter.append(".set").append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1)).append("(").append(value).append(")");
                } else {
                    setter.append(".get").append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1)).append("()");
                }
            }
        }
        return setter.toString();
    }

}
