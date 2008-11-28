package org.drools.jpdl.instance.node;

import org.drools.jpdl.core.node.MailNode;
import org.drools.process.instance.WorkItemManager;
import org.drools.process.instance.impl.WorkItemImpl;
import org.drools.workflow.instance.NodeInstance;

public class MailNodeInstance extends JpdlNodeInstance {

	private static final long serialVersionUID = 1L;
	
	public MailNode getMailNode() {
		return (MailNode) getNode();
	}

	public void execute(NodeInstance from, String type) {
		MailNode mailNode = getMailNode();
		WorkItemManager workItemManager = getProcessInstance().getWorkingMemory().getWorkItemManager();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setName("JpdlEmail");
		workItem.setProcessInstanceId(getProcessInstance().getId());
		workItem.setParameter("template", mailNode.getTemplate());
        workItem.setParameter("actors", mailNode.getActors());
        workItem.setParameter("to", mailNode.getTo());
        workItem.setParameter("subject", mailNode.getSubject());
        workItem.setParameter("text", mailNode.getText());
		workItemManager.internalExecuteWorkItem(workItem);
		leave();
	}

}
