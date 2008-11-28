package org.drools.task.query;

import java.util.Date;

public class DeadlineSummary {
    private long taskId;
    private long deadlineId;
    private Date date;
    
    public DeadlineSummary(long taskId,
                           long deadlineId,
                           Date date) {
        super();
        this.taskId = taskId;
        this.deadlineId = deadlineId;
        this.date = date;
    }
    
    public long getTaskId() {
        return taskId;
    }
    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }
    public long getDeadlineId() {
        return deadlineId;
    }
    public void setDeadlineId(long deadlineId) {
        this.deadlineId = deadlineId;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + (int) (deadlineId ^ (deadlineId >>> 32));
        result = prime * result + (int) (taskId ^ (taskId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( !(obj instanceof DeadlineSummary) ) return false;
        DeadlineSummary other = (DeadlineSummary) obj;
        if ( date == null ) {
            if ( other.date != null ) return false;
        } else if ( date.getTime() != other.date.getTime() ) return false;
        if ( deadlineId != other.deadlineId ) return false;
        if ( taskId != other.taskId ) return false;
        return true;
    }
    
    
}
