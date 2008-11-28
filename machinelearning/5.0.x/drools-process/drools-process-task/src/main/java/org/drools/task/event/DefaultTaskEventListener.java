package org.drools.task.event;

public class DefaultTaskEventListener implements TaskEventListener {

    public void taskClaimed(TaskClaimedEvent event) {        
    }

    public void taskCompleted(TaskCompletedEvent event) {
    }

	public void taskFailed(TaskFailedEvent event) {
	}

	public void taskSkipped(TaskSkippedEvent event) {
	}

}
