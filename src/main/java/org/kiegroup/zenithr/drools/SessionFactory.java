package org.kiegroup.zenithr.drools;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

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
        return RULE_HEADER +
                "rule \"grade A\" when \n" +
                "    grade: DoubleField(name == \"grade\", value >= 90 && <= 100 ) \n" +
                "    output: StringField(name == \"letter\") \n" +
                "then \n" +
                "    output.setValue(\"A\"); \n" +
                "end \n" +
                "rule \"grade B\" when \n" +
                "    grade: DoubleField(name == \"grade\", value >= 80 && < 90 ) \n" +
                "    output: StringField(name == \"letter\") \n" +
                "then \n" +
                "    output.setValue(\"B\"); \n" +
                "end \n" +
                "rule \"grade C\" when \n" +
                "    grade: DoubleField(name == \"grade\", value >= 70 && < 80 ) \n" +
                "    output: StringField(name == \"letter\") \n" +
                "then \n" +
                "    output.setValue(\"C\"); \n" +
                "end \n" +
                "rule \"grade D\" when \n" +
                "    grade: DoubleField(name == \"grade\", value >= 60 && < 70 ) \n" +
                "    output: StringField(name == \"letter\") \n" +
                "then \n" +
                "    output.setValue(\"D\"); \n" +
                "end \n" +
                "rule \"grade F\" when \n" +
                "    grade: DoubleField(name == \"grade\", value < 60 ) \n" +
                "    output: StringField(name == \"letter\") \n" +
                "then \n" +
                "    output.setValue(\"F\"); \n" +
                "end";
    }

    private static final String RULE_HEADER =
            "" +
                    "package org.kiegroup.zenithr.drools \n\n" +
                    "import org.kiegroup.zenithr.drools.model.BooleanField \n" +
                    "import org.kiegroup.zenithr.drools.model.DateField \n" +
                    "import org.kiegroup.zenithr.drools.model.DateTimeField \n" +
                    "import org.kiegroup.zenithr.drools.model.DoubleField \n" +
                    "import org.kiegroup.zenithr.drools.model.LongField \n" +
                    "import org.kiegroup.zenithr.drools.model.StringField \n\n";
}
