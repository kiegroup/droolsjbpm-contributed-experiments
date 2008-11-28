package org.drools.process.workitem.finder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.finder.FileFinder;
import org.apache.commons.finder.Finder;
import org.drools.process.instance.WorkItemHandler;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;

public class FinderWorkItemHandler implements WorkItemHandler {

	private FileFinder finder;
	
	public FinderWorkItemHandler() {
		finder = new FileFinder();
	}
	
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String path = (String) workItem.getParameter("Path");
		String regex = (String) workItem.getParameter("Regex");
		Map<String, Object> options = new HashMap<String, Object>();
		options.put(Finder.REGEX, regex);
		File[] files = finder.find(new File(path), options);
		List<File> fileList = new ArrayList<File>();
		for (File file: files) {
			fileList.add(file);
		}
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("Files", fileList);
		manager.completeWorkItem(workItem.getId(), results);
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// Do nothing, this work item cannot be aborted
	}

}
