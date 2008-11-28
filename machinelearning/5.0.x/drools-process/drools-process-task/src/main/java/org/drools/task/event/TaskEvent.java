package org.drools.task.event;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.EventObject;

public abstract class TaskEvent extends EventObject implements Externalizable {
	
    private static final String dummySource = "";
    private long taskId;
    
    public TaskEvent() {
        super( dummySource );
    }
    
    public TaskEvent(long taskId) {
        super( taskId );
        this.taskId = (Long) taskId;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong( taskId );
    }  
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        taskId = in.readLong();
        source = taskId;
    }
    
    public long getTaskId() {
        return taskId;
    }
        
}
