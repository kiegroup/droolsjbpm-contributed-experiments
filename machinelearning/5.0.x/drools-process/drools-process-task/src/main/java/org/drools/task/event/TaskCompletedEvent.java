package org.drools.task.event;


public class TaskCompletedEvent extends TaskUserEvent {
	
    public TaskCompletedEvent() {
    }
    
    public TaskCompletedEvent(long taskId, String userId) {
        super( taskId, userId );
    }
    
}
