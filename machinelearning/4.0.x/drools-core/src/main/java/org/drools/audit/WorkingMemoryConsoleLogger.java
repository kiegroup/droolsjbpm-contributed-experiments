package org.drools.audit;

import org.drools.WorkingMemory;
import org.drools.WorkingMemoryEventManager;
import org.drools.audit.event.LogEvent;

public class WorkingMemoryConsoleLogger extends WorkingMemoryLogger {

	public WorkingMemoryConsoleLogger(final WorkingMemoryEventManager workingMemoryEventManager) {
		super(workingMemoryEventManager);
	}
	
	public void logEventCreated(LogEvent logEvent) {
		System.out.println(logEvent);
	}

}
