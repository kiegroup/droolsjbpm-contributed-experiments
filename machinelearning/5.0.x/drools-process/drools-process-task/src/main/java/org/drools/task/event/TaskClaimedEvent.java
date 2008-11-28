package org.drools.task.event;

public class TaskClaimedEvent extends TaskUserEvent {

	public TaskClaimedEvent() {
    }
    
    public TaskClaimedEvent(long taskId, String userId) {
        super( taskId, userId );
    }

}
