package org.kiegroup.zenithr.drools;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RuleService {
    public static String getGradeLetter(Double grade) throws IOException {
        KieContainer kieContainer = KieContainerHolder.getInstance().getKieContainer();
        Object parameters = getParameters(kieContainer, grade);
        KieSession kieSession = kieContainer.newKieSession();
        Map<String, String> results = new HashMap<>();
        kieSession.setGlobal("map", results);
        kieSession.insert(parameters);
        kieSession.fireAllRules();
        return results.get("letter");
    }

    @SuppressWarnings("unchecked")
    public static Object getParameters(KieContainer kieContainer, Double grade) {
        try {
            Class clazz = kieContainer.getClassLoader().loadClass("org.kiegroup.zenithr.drools.Parameters");
            Object object = clazz.newInstance();
            Method setGrade = clazz.getMethod("setGrade", Double.class);
            setGrade.invoke(object, grade);
            return object;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
