package org.drools.process.workitem.exec;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.drools.process.instance.WorkItemHandler;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;

public class ExecWorkItemHandler implements WorkItemHandler {

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String command = (String) workItem.getParameter("Command");
		CommandLine commandLine = CommandLine.parse(command);
		DefaultExecutor executor = new DefaultExecutor();
		try {
			executor.execute(commandLine);
			manager.completeWorkItem(workItem.getId(), null);
		} catch (Throwable t) {
			t.printStackTrace();
			manager.abortWorkItem(workItem.getId());
		}
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// Do nothing, this work item cannot be aborted
	}

}
