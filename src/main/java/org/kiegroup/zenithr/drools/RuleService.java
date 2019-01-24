package org.kiegroup.zenithr.drools;

import org.kie.api.runtime.KieSession;
import org.kiegroup.zenithr.drools.model.DoubleField;
import org.kiegroup.zenithr.drools.model.StringField;

public class RuleService {
    public static String getGradeLetter(Double grade) {
        KieSession kieSession = SessionFactory.getInstance().newKieSession();
        DoubleField input = new DoubleField();
        input.setName("grade");
        input.setValue(grade);
        StringField output = new StringField();
        output.setName("letter");
        kieSession.insert(input);
        kieSession.insert(output);
        kieSession.fireAllRules();
        return output.getValue();
    }
}
