package org.drools.bpel.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.drools.WorkingMemory;
import org.drools.bpel.instance.BPELProcessInstance;
import org.drools.process.instance.WorkItem;
import org.drools.process.instance.WorkItemHandler;
import org.drools.process.instance.WorkItemManager;

public class BPELTestUtil {
	
	private BPELTestUtil() {
	}

    public static WorkItem findWebServiceInvocation(WorkingMemory workingMemory, String partnerLink, String portType, String operation) {
        Set<WorkItem> workItems =
        	((WorkItemManager) workingMemory.getWorkItemManager()).getWorkItems();
        for (Iterator<WorkItem> iterator = workItems.iterator(); iterator.hasNext(); ) {
            WorkItem workItem = iterator.next();
            if ("WebServiceInvocation".equals(workItem.getName())
                    && workItem.getParameter("PartnerLink").equals(partnerLink)
                    && workItem.getParameter("PortType").equals(portType)
                    && workItem.getParameter("Operation").equals(operation)) {
                return workItem;
            }
        }
        return null;
    }
    
    public static void replyWebServiceInvocation(WorkingMemory workingMemory, WorkItem workItem, String result) {
        System.out.println("Replying to web service invocation "
                + workItem.getParameter("PartnerLink") + " "
                + workItem.getParameter("PortType") + " "
                + workItem.getParameter("Operation") + ", message = "
                + workItem.getParameter("Message") + ": "
                + result);
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("Result", result);
        workingMemory.getWorkItemManager().completeWorkItem(workItem.getId(), results);
    }
    
    public static void replyWebServiceInvocationFault(WorkingMemory workingMemory, WorkItem workItem, String faultName, String result) {
        System.out.println("Replying to web service invocation "
                + workItem.getParameter("PartnerLink") + " "
                + workItem.getParameter("PortType") + " "
                + workItem.getParameter("Operation") + ", faultName = "
                + workItem.getParameter("FaultName") + ", message = "
                + workItem.getParameter("Message") + ": "
                + result);
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("Result", result);
        results.put("FaultName", faultName);
        workingMemory.getWorkItemManager().completeWorkItem(workItem.getId(), results);
    }
    
    public static void webServiceInvocation(BPELProcessInstance processInstance, String partnerLink, String portType, String operation, String result) {
        System.out.println("Web service invocation "
                + partnerLink + " "
                + portType + " "
                + operation + ": "
                + result);
        processInstance.signalEvent("message", new String[] { partnerLink, portType, operation, result } );
    }
    
    public static class WebServiceInvocationHandler implements WorkItemHandler {

        public void executeWorkItem(org.drools.runtime.process.WorkItem workItem, org.drools.runtime.process.WorkItemManager manager) {
            System.out.println("Web service invoked "
                + workItem.getParameter("PartnerLink") + " "
                + workItem.getParameter("PortType") + " "
                + (workItem.getParameter("FaultName") == null ? ""
                		: "fault=" + workItem.getParameter("FaultName")) + " "
                + workItem.getParameter("Operation") + ", message = "
                + workItem.getParameter("Message"));
        }
        
        public void abortWorkItem(org.drools.runtime.process.WorkItem workItem, org.drools.runtime.process.WorkItemManager manager) {
            System.out.println("Web service invocation aborted "
                + workItem.getParameter("PartnerLink") + " "
                + workItem.getParameter("PortType") + " "
                + (workItem.getParameter("FaultName") == null ? ""
            		: workItem.getParameter("FaultName"))
                + workItem.getParameter("Operation") + ", message = "
                + workItem.getParameter("Message"));
        }

    }
    
}
