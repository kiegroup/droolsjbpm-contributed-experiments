package org.drools.process.enterprise.processinstance;

import java.util.Map;

import javax.ejb.Remote;

@Remote
public interface ProcessService {

	long startProcess(String processId);
	
	void completeWorkItem(long workItemId, Map<String, Object> results);
	
}
