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

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.kiegroup.zenithr.drools.model.Rule;
import org.kiegroup.zenithr.drools.model.exceptions.InvalidExpressionException;

public class ConverterUtils {

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("(\\$(\\w+)\\.(\\S+))");
    private static final Pattern CONDITION_PATTERN = Pattern.compile("^(\\S+)\\s([<>=!]{1,2})\\s(\\S+)$");

    private static final Pattern NUMERIC_RIGHT_EXP = Pattern.compile("(\\$\\w+\\.)([\\w.]*)((\\s*[/+\\-*]\\s*[0-9])+)");
    private static final Pattern NUMERIC_LEFT_EXP = Pattern.compile("(([0-9]\\s*[/+\\-*]\\s*)+)(\\$\\w+\\.)([\\w.]*)");
    private static final Pattern[] NUMERIC_EXP = {NUMERIC_LEFT_EXP, NUMERIC_RIGHT_EXP};

    private static final Pattern LITERAL_EXP = Pattern.compile("(\\$\\w+\\.)([\\w.]*)");
    private static final String NUMERIC_METHOD = "getNumber";
    private static final String LITERAL_METHOD = "getString";

    private static final String INPUT_FACT = "%s: InputFact( getName() == \"%s\"%s)";

    public static String getInputFacts(Rule rule, String input) {
        Collection<String> matches = rule.getWhen().stream().filter(when -> when.contains(input)).collect(Collectors.toList());
        StringBuilder conditions = new StringBuilder();
        matches.stream().map(ConverterUtils::parseCondition).forEach(c -> conditions.append(" && ").append(c));
        return String.format(INPUT_FACT, input, input, conditions.toString());
    }

    public static String parseCondition(String condition) {
        Matcher matcher = CONDITION_PATTERN.matcher(condition);
        if (!matcher.matches()) {
            throw new InvalidExpressionException("The 'when' condition only supports: <expression> <operator> <expression>");
        }
        Condition c = new Condition();
        c.operand = Operand.fromSymbol(matcher.group(2));
        if (EXPRESSION_PATTERN.matcher(matcher.group(1)).matches()) {
            setExpression(c, matcher.group(1));
            c.literal = matcher.group(3);
        } else if (EXPRESSION_PATTERN.matcher(matcher.group(3)).matches()) {
            setExpression(c, matcher.group(3));
            c.literal = matcher.group(1);
        } else {
            throw new InvalidExpressionException("Invalid condition: " + condition);
        }
        return String.format("%s.%s(\"%s\", %s)", c.variable, c.operand.method, c.path, c.literal);
    }

    public static String replaceVariables(String value) {
        value = value.replaceAll("\"", "\\\\\"");
        for (Pattern p : NUMERIC_EXP) {
            value = replaceMethod(p, NUMERIC_METHOD, value);
        }
        value = replaceMethod(LITERAL_EXP, LITERAL_METHOD, value);
        return "\"" + value + "\"";
    }

    private static String replaceMethod(Pattern p, String method, String value) {
        StringBuffer x = new StringBuffer();
        Matcher m = p.matcher(value);
        while (m.find()) {
            StringBuilder replacement = new StringBuilder();
            if (m.start() != 0) {
                replacement.append("\" + ");
            }
            if (NUMERIC_METHOD.equals(method)) {
                replacement.append("(");
            }
            replacement.append(m.group(1).substring(1)).append(method).append("(\"").append(m.group(2)).append("\")");
            if (NUMERIC_METHOD.equals(method)) {
                replacement.append(m.group(3))
                    .append(")");
            }
            if (!m.hitEnd()) {
                replacement.append(" + \"");
            }
            m.appendReplacement(x, replacement.toString());
        }
        return m.appendTail(x).toString();
    }

    private static class Condition {

        String variable;
        String path;
        Operand operand;
        String literal;
    }

    private static void setExpression(Condition c, String expression) {
        Matcher matcher = EXPRESSION_PATTERN.matcher(expression);
        if (matcher.matches()) {
            c.variable = matcher.group(2);
            c.path = matcher.group(3);
        }
    }

    private enum Operand {
        GREATER_THAN(">", "greaterThan"),
        LESS_THAN("<", "lessThan"),
        EQUALS_TO("==", "equalsTo"),
        GREATER_THAN_EQUALS(">=", "greaterThanEquals"),
        LESS_THAN_EQUALS("<=", "lessThanEquals");

        String symbol;
        String method;

        Operand(String symbol, String method) {
            this.symbol = symbol;
            this.method = method;
        }

        static Operand fromSymbol(String symbol) {
            switch (symbol) {
                case ">":
                    return GREATER_THAN;
                case "<":
                    return LESS_THAN;
                case "==":
                    return EQUALS_TO;
                case ">=":
                    return GREATER_THAN_EQUALS;
                case "<=":
                    return LESS_THAN_EQUALS;
                default:
                    throw new InvalidExpressionException("Invalid symbol provided: " + symbol);
            }
        }
    }
}
