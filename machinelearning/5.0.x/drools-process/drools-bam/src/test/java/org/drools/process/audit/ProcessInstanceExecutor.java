package org.drools.process.audit;

import java.io.InputStreamReader;
import java.io.Reader;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.compiler.PackageBuilder;
import org.drools.process.instance.impl.demo.UIWorkItemHandler;
import org.drools.rule.Package;

public class ProcessInstanceExecutor {
    
    public static final void main(String[] args) {
        try {
            //load the process
            RuleBase ruleBase = createKnowledgeBase();
            // create a new session
            StatefulSession session = ruleBase.newStatefulSession();
            new WorkingMemoryDbLogger(session);
            UIWorkItemHandler uiHandler = new UIWorkItemHandler();
            session.getWorkItemManager().registerWorkItemHandler("Human Task", uiHandler);
            uiHandler.setVisible(true);
            new ProcessInstanceExecutorFrame(session).setVisible(true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Creates the knowledge base by loading the process definition.
     */
    private static RuleBase createKnowledgeBase() throws Exception {
        // create a builder
        PackageBuilder builder = new PackageBuilder();
        // load the process
        Reader source = new InputStreamReader(
            ProcessInstanceExecutor.class.getResourceAsStream("/ruleflow.rf"));
        builder.addProcessFromXml(source);
        source = new InputStreamReader(
            ProcessInstanceExecutor.class.getResourceAsStream("/ruleflow2.rf"));
        builder.addProcessFromXml(source);
       // create the knowledge base 
        Package pkg = builder.getPackage();
        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage(pkg);
        return ruleBase;
    }
    
}
