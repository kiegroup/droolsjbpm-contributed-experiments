package org.kiegroup.zenithr.drools;

import org.drools.core.io.impl.ReaderResource;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;

import java.io.IOException;
import java.io.StringReader;

public class KieContainerHolder {
    private static KieContainerHolder INSTANCE;
    private KieContainer kieContainer;

    private KieContainerHolder(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public static KieContainerHolder getInstance() throws IOException {
        if (INSTANCE == null) {
            synchronized (KieContainerHolder.class) {
                if (INSTANCE == null) {
                    KieServices ks = KieServices.Factory.get();
                    KieRepository kr = ks.getRepository();
                    KieFileSystem kfs = ks.newKieFileSystem();

                    kfs.write("src/main/java/org/kiegroup/zenithr/drools/Parameters.java", getPOJO());
                    kfs.write("src/main/resources/org/kiegroup/zenithr/drools/rule.drl", getRule());

                    KieBuilder kb = ks.newKieBuilder(kfs);

                    kb.buildAll(); // kieModule is automatically deployed to KieRepository if successfully built.
                    if (kb.getResults().hasMessages(Message.Level.ERROR)) {
                        throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
                    }

                    KieContainer kieContainer = ks.newKieContainer(kr.getDefaultReleaseId());
                    INSTANCE = new KieContainerHolder(kieContainer);
                }
            }
        }
        return INSTANCE;
    }

    public KieContainer getKieContainer() {
        return kieContainer;
    }

    private static String getRule() {
        return "" +
                "package org.kiegroup.zenithr.drools \n\n" +
                "import org.kiegroup.zenithr.drools.Parameters \n\n" +
                "global java.util.Map map \n\n" +
                "rule \"grade A\" when \n" +
                "    params : Parameters(grade >= 90 && <= 100 ) \n" +
                "then \n" +
                "    map.put( \"letter\", \"A\" ); \n" +
                "end \n" +
                "rule \"grade B\" when \n" +
                "    params : Parameters(grade >= 80 && < 90 ) \n" +
                "then \n" +
                "    map.put( \"letter\", \"B\" ); \n" +
                "end \n" +
                "rule \"grade C\" when \n" +
                "    params : Parameters(grade >= 70 && < 80 ) \n" +
                "then \n" +
                "    map.put( \"letter\", \"C\" ); \n" +
                "end \n" +
                "rule \"grade D\" when \n" +
                "    params : Parameters(grade >= 60 && < 70 ) \n" +
                "then \n" +
                "    map.put( \"letter\", \"D\" ); \n" +
                "end \n" +
                "rule \"grade F\" when \n" +
                "    params : Parameters(grade < 60 ) \n" +
                "then \n" +
                "    map.put( \"letter\", \"F\" ); \n" +
                "end";
    }

    private static ReaderResource getPOJO() {
        final JavaClassSource parametersClass = Roaster.create(JavaClassSource.class);
        parametersClass.setPackage("org.kiegroup.zenithr.drools").setName("Parameters");
        parametersClass.addProperty(Double.class, "grade");
        return new ReaderResource(new StringReader(parametersClass.toString()));
    }
}
