package com.drools.memory.banchmark.model.events;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.net.URL;
import java.util.Date;

/**
 * @author Victor
 */
public class MinorEvent extends VisitorEvent{
    public MinorEvent(URL pageUrl, String visitorId, String configId, Date eventTimestamp) {
        super(pageUrl, visitorId, configId, eventTimestamp);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}