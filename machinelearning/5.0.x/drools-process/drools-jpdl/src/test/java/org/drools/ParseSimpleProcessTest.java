package org.drools;

import junit.framework.TestCase;

import org.drools.jpdl.JpdlParser;
import org.drools.jpdl.core.JpdlProcess;
import org.drools.process.core.validation.ProcessValidationError;
import org.drools.process.instance.ProcessInstance;
import org.drools.process.instance.WorkItem;
import org.drools.process.instance.WorkItemHandler;
import org.drools.process.instance.WorkItemManager;
import org.drools.rule.Package;

public class ParseSimpleProcessTest extends TestCase {

	public void testSimpleProcess() throws Exception {
	    JpdlParser parser = new JpdlParser();
	    JpdlProcess process = parser.loadJpdlProcess("simple/processdefinition.xml");
	    ProcessValidationError[] errors = parser.getErrors();
	    for (ProcessValidationError error: errors) {
            System.err.println(error);
        }
        assertEquals(0, errors.length);
        
        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        Package p = new Package("com.sample");
        p.addProcess(process);
        ruleBase.addPackage( p );
        
        WorkingMemory workingMemory = ruleBase.newStatefulSession();
        ProcessInstance processInstance = workingMemory.startProcess("simple");
        assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
    }

    public void testSimpleProcess2() throws Exception {
        JpdlParser parser = new JpdlParser();
        JpdlProcess process = parser.loadJpdlProcess("simple2/processdefinition.xml");
        ProcessValidationError[] errors = parser.getErrors();
        for (ProcessValidationError error: errors) {
            System.err.println(error);
        }
        assertEquals(0, errors.length);
        
        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        Package p = new Package("com.sample");
        p.addProcess(process);
        ruleBase.addPackage( p );
        
        WorkingMemory workingMemory = ruleBase.newStatefulSession();
        TestWorkItemHandler handler = new TestWorkItemHandler();
        workingMemory.getWorkItemManager().registerWorkItemHandler(
            "Email", handler);
        assertTrue(handler.getWorkItemId() == -1);
        ProcessInstance processInstance = workingMemory.startProcess("simple");
        assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
    }

    private static class TestWorkItemHandler implements WorkItemHandler {
        private long workItemId = -1;
        public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
            workItemId = workItem.getId();
        }
        public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        }
        public long getWorkItemId() {
            return workItemId;
        }
    }
    
}
