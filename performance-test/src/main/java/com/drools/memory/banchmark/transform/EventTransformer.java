package com.drools.memory.banchmark.transform;

import com.drools.memory.banchmark.event.prcessor.VisitorGenericEvent;
import com.drools.memory.banchmark.event.prcessor.VisitorGenericEventId;
import com.drools.memory.banchmark.model.events.MainEvent;
import com.drools.memory.banchmark.model.events.MinorEvent;
import com.drools.memory.banchmark.model.events.VisitorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * @author Victor
 */
public class EventTransformer {
    private static Logger log = LoggerFactory.getLogger(EventTransformer.class);

    public VisitorEvent transform(VisitorGenericEvent visitorGenericEvent) {

        VisitorEvent visitorEvent = null;
        if (visitorGenericEvent.getId().equals(VisitorGenericEventId.MAIN)) {
            URL url = null;
            try {
                url = new URL(visitorGenericEvent.getData().get("url"));
            } catch (MalformedURLException e) {
                log.error("Failed to get URL from 'in page' event", e);
            }
            visitorEvent = new MinorEvent(url, visitorGenericEvent.getVisitorId(), visitorGenericEvent.getConfigId(), new Date(visitorGenericEvent.getEventTimestamp()));
        } else if (visitorGenericEvent.getId().equals(VisitorGenericEventId.MINOR)) {
            URL url = null;
            URL referer = null;
            try {
                String urlStr = visitorGenericEvent.getData().get("url");
                if (urlStr != null && !urlStr.equals("")) {
                    url = new URL(urlStr);
                }
            } catch (MalformedURLException e) {
                log.error("Failed to get URL from 'enter page' event", e);
            }
            try {
                String urlStr = visitorGenericEvent.getData().get("referer");
                if (urlStr != null && !urlStr.equals("")) {
                    url = new URL(urlStr);
                }
            } catch (MalformedURLException e) {
                log.error("Failed to get referer from 'enter page' event", e);
            }
            visitorEvent = new MainEvent(referer, url, visitorGenericEvent.getVisitorId(),
                    visitorGenericEvent.getConfigId(), new Date(visitorGenericEvent.getEventTimestamp()),
                    visitorGenericEvent.getData().get("title"));
        }
        return visitorEvent;
    }

}
