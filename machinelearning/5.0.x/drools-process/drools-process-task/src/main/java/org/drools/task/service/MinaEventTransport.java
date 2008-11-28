package org.drools.task.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.drools.eventmessaging.EventTriggerTransport;
import org.drools.eventmessaging.Payload;

public class MinaEventTransport implements EventTriggerTransport {
    private String uuid;
    private Map<String, IoSession> sessions;
    private int responseId;
    private boolean remove;
    
    MinaEventTransport(String uuid, int responseId, Map<String, IoSession> sessions, boolean remove) {
        this.uuid = uuid;
        this.responseId = responseId;
        this.sessions = sessions;
        this.remove = remove;
    }

    public void trigger(Payload payload) {        
        IoSession session = sessions.get( uuid );
        List args = new ArrayList( 1 );
        args.add( payload );
        Command cmd = new Command( responseId, CommandName.EventTriggerResponse, args);             
        session.write( cmd );        
    }
    
    public boolean isRemove() {
        return this.remove;
    }
}
