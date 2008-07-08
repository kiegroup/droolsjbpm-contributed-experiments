package org.drools.process.enterprise.processinstance;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class WorkItemInfo {

	private @Id long workItemId;
	private String name;
	// TODO
    private @Transient Map<String, Object> parameters;
    private long processInstanceId;
    
	public long getWorkItemId() {
		return workItemId;
	}
	
	public void setWorkItemId(long workItemId) {
		this.workItemId = workItemId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Map<String, Object> getParameters() {
		return parameters;
	}
	
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
	public long getProcessInstanceId() {
		return processInstanceId;
	}
	
	public void setProcessInstanceId(long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	
}
