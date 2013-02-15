package com.drools.memory.banchmark.event.prcessor;

import java.util.Map;

/**
 * @author Victor
 */
public class VisitorGenericEvent{
    private  VisitorGenericEventId id;
    private String visitorId;
    private String configId;
    private long eventTimestamp;//in millis
    private Map<String, String> data;

    protected VisitorGenericEvent(VisitorGenericEventId id, String visitorId, String configId, long eventTimestamp, Map<String, String> data) {
        this.id = id;
        this.visitorId = visitorId;
        this.configId = configId;
        this.eventTimestamp = eventTimestamp;
        this.data = data;
    }

    public VisitorGenericEventId getId() {
        return id;
    }

    public String getVisitorId() {
        return visitorId;
    }

    public String getConfigId() {
        return configId;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public Map<String, String> getData() {
        return data;
    }
}
