package org.drools.task.event;

public class TaskSkippedEvent extends TaskUserEvent {

	public TaskSkippedEvent() {
    }
    
    public TaskSkippedEvent(long taskId, String userId) {
        super( taskId, userId );
    }

}
