package org.drools.persistence.processinstance;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class ProcessInstanceEventInfo {

	private @Id @GeneratedValue(strategy=GenerationType.AUTO) Long id;
	private String eventType;
	private long processInstanceId;
	
	ProcessInstanceEventInfo() {
	}
	
	public ProcessInstanceEventInfo(long processInstanceId, String eventType) {
		this.processInstanceId = processInstanceId;
		this.eventType = eventType;
	}
	
	public long getProcessInstanceId() {
		return processInstanceId;
	}
	
	public String getEventType() {
		return eventType;
	}
	
}
