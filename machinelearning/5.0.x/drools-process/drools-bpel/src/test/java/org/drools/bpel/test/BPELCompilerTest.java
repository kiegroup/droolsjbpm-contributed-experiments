package org.drools.bpel.test;

import junit.framework.TestCase;

import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.bpel.compiler.BPELCompiler;
import org.drools.bpel.core.BPELProcess;
import org.drools.bpel.instance.BPELProcessInstance;
import org.drools.bpel.test.BPELTestUtil.WebServiceInvocationHandler;
import org.drools.common.AbstractRuleBase;
import org.drools.common.InternalWorkingMemory;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.ProcessBuilder;
import org.drools.process.instance.ProcessInstance;
import org.drools.process.instance.WorkItem;
import org.drools.process.instance.WorkItemHandler;
import org.drools.reteoo.ReteooWorkingMemory;

public class BPELCompilerTest extends TestCase {
	
	private WorkingMemory getWorkingMemory() throws Exception {
		// Compile process
		BPELCompiler compiler = new BPELCompiler();
		BPELProcess process = compiler.compileProcess(
			BPELCompilerTest.class.getResource("/purchaseOrderProcess.bpel"));
		
		// Build process
        PackageBuilder packageBuilder = new PackageBuilder();
        ProcessBuilder processBuilder = new ProcessBuilder(packageBuilder);
        processBuilder.buildProcess(process, "/purchaseOrderProcess.bpel");
        
        // Load process
        AbstractRuleBase ruleBase = (AbstractRuleBase) RuleBaseFactory.newRuleBase();
        ruleBase.addProcess(process);
        InternalWorkingMemory workingMemory = new ReteooWorkingMemory(1, ruleBase);
        WorkItemHandler handler = new WebServiceInvocationHandler();
        workingMemory.getWorkItemManager().registerWorkItemHandler("WebServiceInvocation", handler);
        return workingMemory;
	}
	
	public void testPurchaseOrderProcessNormalFlow() throws Exception {
        // Execute process
		WorkingMemory workingMemory = getWorkingMemory();
        BPELProcessInstance processInstance = (BPELProcessInstance)
        	workingMemory.startProcess("http://drools.jboss.org/example/bpel/purchase");
        
        // start process
        BPELTestUtil.webServiceInvocation(processInstance, "purchasing", "{http://manufacturing.org/wsdl/purchase}purchaseOrderPT", "sendPurchaseOrder", 
    		"<POMessage><customerInfo>Jack</customerInfo><purchaseOrder>PURCHASE_ORDER</purchaseOrder></POMessage>");

        // reply to web service invocations
        WorkItem workItem = BPELTestUtil.findWebServiceInvocation(workingMemory, "scheduling", "{http://manufacturing.org/wsdl/purchase}schedulingPT", "requestProductionScheduling");
        BPELTestUtil.replyWebServiceInvocation(workingMemory, workItem, null);

        workItem = BPELTestUtil.findWebServiceInvocation(workingMemory, "invoicing", "{http://manufacturing.org/wsdl/purchase}computePricePT", "initiatePriceCalculation");
        BPELTestUtil.replyWebServiceInvocation(workingMemory, workItem, null);
        
        workItem = BPELTestUtil.findWebServiceInvocation(workingMemory, "shipping", "{http://manufacturing.org/wsdl/purchase}shippingPT", "requestShipping");
        BPELTestUtil.replyWebServiceInvocation(workingMemory, workItem, "<shippingInfoMessage><shippingInfo>SHIPPING_INFO</shippingInfo></shippingInfoMessage>");
        
        workItem = BPELTestUtil.findWebServiceInvocation(workingMemory, "invoicing", "{http://manufacturing.org/wsdl/purchase}computePricePT", "sendShippingPrice");
        BPELTestUtil.replyWebServiceInvocation(workingMemory, workItem, null);
        
        // invoke web service callbacks
        BPELTestUtil.webServiceInvocation(processInstance, "shipping", "{http://manufacturing.org/wsdl/purchase}shippingCallbackPT", "sendSchedule", "<scheduleMessage><schedule>SCHEDULE</schedule></scheduleMessage>");
        BPELTestUtil.webServiceInvocation(processInstance, "invoicing", "{http://manufacturing.org/wsdl/purchase}invoiceCallbackPT", "sendInvoice", "<InvMessage><IVC>INVOICE</IVC></InvMessage>");

        // reply to web service invocation
        workItem = BPELTestUtil.findWebServiceInvocation(workingMemory, "scheduling", "{http://manufacturing.org/wsdl/purchase}schedulingPT", "sendShippingSchedule");
        BPELTestUtil.replyWebServiceInvocation(workingMemory, workItem, null);

        workItem = BPELTestUtil.findWebServiceInvocation(workingMemory, "purchasing", "{http://manufacturing.org/wsdl/purchase}purchaseOrderPT", "sendPurchaseOrder");
        BPELTestUtil.replyWebServiceInvocation(workingMemory, workItem, null);
        
        assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
	}

	public void testPurchaseOrderProcessFault() throws Exception {
        // Execute process
		WorkingMemory workingMemory = getWorkingMemory();
        BPELProcessInstance processInstance = (BPELProcessInstance)
        	workingMemory.startProcess("http://drools.jboss.org/example/bpel/purchase");
        
        // start process
        BPELTestUtil.webServiceInvocation(processInstance, "purchasing", "{http://manufacturing.org/wsdl/purchase}purchaseOrderPT", "sendPurchaseOrder", 
    		"<POMessage><customerInfo>Jack</customerInfo><purchaseOrder>PURCHASE_ORDER</purchaseOrder></POMessage>");

        // reply to web service invocations
        WorkItem workItem = BPELTestUtil.findWebServiceInvocation(workingMemory, "scheduling", "{http://manufacturing.org/wsdl/purchase}schedulingPT", "requestProductionScheduling");
        BPELTestUtil.replyWebServiceInvocation(workingMemory, workItem, null);

        workItem = BPELTestUtil.findWebServiceInvocation(workingMemory, "invoicing", "{http://manufacturing.org/wsdl/purchase}computePricePT", "initiatePriceCalculation");
        BPELTestUtil.replyWebServiceInvocation(workingMemory, workItem, null);
        
        workItem = BPELTestUtil.findWebServiceInvocation(workingMemory, "shipping", "{http://manufacturing.org/wsdl/purchase}shippingPT", "requestShipping");
        BPELTestUtil.replyWebServiceInvocationFault(workingMemory, workItem, "{http://manufacturing.org/wsdl/purchase}cannotCompleteOrder", "SHIPPING FAULT");

        workItem = BPELTestUtil.findWebServiceInvocation(workingMemory, "purchasing", "{http://manufacturing.org/wsdl/purchase}purchaseOrderPT", "sendPurchaseOrder");
        BPELTestUtil.replyWebServiceInvocation(workingMemory, workItem, null);

        assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
	}
	
}
