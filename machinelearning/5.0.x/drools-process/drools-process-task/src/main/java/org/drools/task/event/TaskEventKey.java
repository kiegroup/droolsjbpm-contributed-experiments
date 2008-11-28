package org.drools.task.event;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.drools.eventmessaging.EventKey;


public class TaskEventKey implements EventKey, Externalizable {
    private Class<? extends TaskEvent> event;
    private long taskId;
    
    public TaskEventKey() {
        
    }
        
    public TaskEventKey(Class<? extends TaskEvent> event, long taskId) {
        this.event = event;
        this.taskId = taskId;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(  event.getName() );
        out.writeLong( taskId );
    }  
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        String className = in.readUTF();
        try {
            event = (Class< ? extends TaskEvent>) Thread.currentThread().getContextClassLoader().loadClass( className );
        } catch( Exception e ) {
            event = (Class< ? extends TaskEvent>) getClass().getClassLoader().loadClass( className );
        }
        
        taskId = in.readLong();
    }    
            
    public Class< ? extends TaskEvent> getEvent() {
        return event;
    }

    public long getTaskId() {
        return this.taskId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((event == null) ? 0 : event.hashCode());
        result = prime * result + (int) (taskId ^ (taskId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( !(obj instanceof TaskEventKey) ) return false;
        TaskEventKey other = (TaskEventKey) obj;
        if ( event == null ) {
            if ( other.event != null ) return false;
        } else if ( !event.equals( other.event ) ) return false;
        if ( taskId != other.taskId ) return false;
        return true;
    }     
}
