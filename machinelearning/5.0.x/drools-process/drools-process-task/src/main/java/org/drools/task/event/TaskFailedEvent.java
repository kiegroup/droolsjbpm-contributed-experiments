package org.drools.task.event;

public class TaskFailedEvent extends TaskUserEvent {

	public TaskFailedEvent() {
    }
    
    public TaskFailedEvent(long taskId, String userId) {
        super( taskId, userId );
    }

}
