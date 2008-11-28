package org.drools.bpel.test;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.audit.WorkingMemoryFileLogger;
import org.drools.bpel.compiler.BPELCompiler;
import org.drools.bpel.instance.BPELProcessInstance;
import org.drools.process.instance.impl.demo.UIWorkItemHandler;

public class BPELTest {

    public static final void main(String[] args) {
        try {
        	RuleBase ruleBase = RuleBaseFactory.newRuleBase();
    		new BPELCompiler().loadProcess(ruleBase, "/BPELProcess.bpel");
            WorkingMemory workingMemory = ruleBase.newStatefulSession();
            final WorkingMemoryFileLogger logger = new WorkingMemoryFileLogger(workingMemory);
            UIWorkItemHandler uiHandler = new UIWorkItemHandler() {
				private static final long serialVersionUID = 4L;
				public void dispose() {
            		super.dispose();
            		logger.writeToDisk();
            	}
            };
            workingMemory.getWorkItemManager().registerWorkItemHandler("WebServiceInvocation", uiHandler);
            uiHandler.setVisible(true);
            BPELProcessInstance processInstance = (BPELProcessInstance)
            	workingMemory.startProcess("http://drools.jboss.org/bpel/sample");
            processInstance.signalEvent("message",
        		new String[] {
    				"client", 
    				"{http://drools.jboss.org/bpel/sample}BPELProcess", 
    				"process",
            		"<BPELProcessRequestMessage>" +
            		"  <payload><input>input</input></payload>" +
            		"  <input>hello</input>" +
            		"</BPELProcessRequestMessage>"
        		}
            );
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
