package org.drools.task.event;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.drools.eventmessaging.Payload;

public class EventPayload implements Payload, Externalizable {
    private TaskEvent event;

    public EventPayload() {
        
    }
    
    public EventPayload(TaskEvent event) {
        this.event = event;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject( event );
    }    
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        event = ( TaskEvent ) in.readObject();
    }
    
    public Object get() {
        return event;
    }       
}
