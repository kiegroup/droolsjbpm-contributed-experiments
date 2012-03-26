package com.drools.memory.banchmark.model.events;

import java.net.URL;
import java.util.Date;

/**
 * @author Victor
 */
public class VisitorEvent {
    private URL pageUrl;
    private String visitorId;
    private String configId;
    private Date eventTimestamp;

    public VisitorEvent(URL pageUrl, String visitorId, String configId, Date eventTimestamp) {
        this.pageUrl = pageUrl;
        this.visitorId = visitorId;
        this.configId = configId;
        this.eventTimestamp = eventTimestamp;
    }


    public URL getPageUrl() {
        return pageUrl;
    }

    public String getVisitorId() {
        return visitorId;
    }

    public String getConfigId() {
        return configId;
    }

    public Date getEventTimestamp() {
        return eventTimestamp;
    }
}
