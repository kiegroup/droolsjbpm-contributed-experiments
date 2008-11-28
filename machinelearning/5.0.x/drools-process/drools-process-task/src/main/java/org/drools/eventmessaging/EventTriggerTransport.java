package org.drools.eventmessaging;


public interface EventTriggerTransport {
    public void trigger(Payload payload);
    
    public boolean isRemove();
}
