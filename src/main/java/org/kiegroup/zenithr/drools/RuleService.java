package org.kiegroup.zenithr.drools;

import org.kie.api.runtime.KieSession;

public class RuleService {
    public static String getGradeLetter(Double grade) {
        KieSession kieSession = SessionFactory.getInstance().newKieSession();
        FactField input = new FactField();
        input.setName("grade");
        input.setDoubleValue(grade);
        FactField output = new FactField();
        output.setName("output");
        kieSession.insert(input);
        kieSession.insert(output);
        kieSession.fireAllRules();
        return output.getStringValue();
    }
}
