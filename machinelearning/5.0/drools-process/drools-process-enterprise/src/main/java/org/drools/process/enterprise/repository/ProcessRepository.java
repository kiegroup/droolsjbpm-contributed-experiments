package org.drools.process.enterprise.repository;

import java.util.List;

import javax.ejb.Remote;

@Remote
public interface ProcessRepository {

	void storeProcess(String processId, String processXML);
	
	ProcessInfo getProcessInfo(String processId);
	
	List<ProcessInfo> getProcessInfos();

}