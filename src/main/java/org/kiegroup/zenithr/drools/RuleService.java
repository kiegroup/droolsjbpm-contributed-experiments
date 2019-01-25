package org.kiegroup.zenithr.drools;

import org.kie.api.runtime.KieSession;

import java.util.List;
import java.util.Map;

public class RuleService {
    public static Object getOutput(Map<String, String[]> parameters) {
        SessionFactory sessionFactory = SessionFactory.getInstance();
        KieSession kieSession = sessionFactory.newKieSession();
        List<FactField> facts = sessionFactory.getFacts(parameters);
        for (FactField fact : facts) {
            kieSession.insert(fact);
        }
        FactField output = new FactField();
        output.setName("output");
        kieSession.insert(output);
        kieSession.fireAllRules();
        return sessionFactory.getOutputObject(output);
    }
}
