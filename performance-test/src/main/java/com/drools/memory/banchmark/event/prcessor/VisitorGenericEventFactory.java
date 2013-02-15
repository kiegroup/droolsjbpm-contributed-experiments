package com.drools.memory.banchmark.event.prcessor;

import java.util.Map;

/**
 * @author Victor
 */

public class VisitorGenericEventFactory {
    public VisitorGenericEvent create(VisitorGenericEventId id, String visitorId, String configId, Map<String, String> data) {
        return new VisitorGenericEvent(id,visitorId,configId,System.currentTimeMillis() ,data);
    }
}