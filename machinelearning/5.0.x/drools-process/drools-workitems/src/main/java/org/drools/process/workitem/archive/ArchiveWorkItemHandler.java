package org.drools.process.workitem.archive;

import java.io.File;
import java.util.List;

import org.apache.commons.compress.Archive;
import org.apache.commons.compress.ArchiverFactory;
import org.drools.process.instance.WorkItemHandler;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;

public class ArchiveWorkItemHandler implements WorkItemHandler {

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String archive = (String) workItem.getParameter("Archive");
		List<File> files = (List<File>) workItem.getParameter("Files");
		try {
			Archive archiver = ArchiverFactory.getInstance("tar");
			if (files != null) {
				for (File file: files) {
					archiver.add(file);
				}
			}
			archiver.save(new File(archive));
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
