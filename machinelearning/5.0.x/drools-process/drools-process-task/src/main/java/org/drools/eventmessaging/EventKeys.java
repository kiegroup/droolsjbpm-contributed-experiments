package org.drools.eventmessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventKeys {
    private Map<EventKey, List<EventTriggerTransport>> keys;
    
    public EventKeys() {
        keys = new HashMap<EventKey, List<EventTriggerTransport>>();
    }
    
    public void register(EventKey key, EventTriggerTransport target) {
        List<EventTriggerTransport> list = keys.get( key);
        if ( list == null ) {
            list = new ArrayList<EventTriggerTransport>();
            keys.put( key, list );
        }
        list.add( target );
    }
    
    public void unregister(EventKey key, EventTriggerTransport target) {
        List<EventTriggerTransport> list = keys.get( key);
        if ( list != null ) {
            list.remove( target );
        }
    }
    
    public List<EventTriggerTransport> getTargets(EventKey key) {
        return keys.get(  key  );
    }
    
    public List<EventTriggerTransport> removeKey(EventKey key) {
        return keys.remove( key );
    }    
    
    
}
