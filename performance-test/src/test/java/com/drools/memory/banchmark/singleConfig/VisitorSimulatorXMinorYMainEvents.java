package com.drools.memory.banchmark.singleConfig;

import com.drools.memory.banchmark.event.prcessor.VisitorGenericEvent;
import com.drools.memory.banchmark.event.prcessor.VisitorGenericEventFactory;
import com.drools.memory.banchmark.event.prcessor.VisitorGenericEventId;

import java.util.HashMap;
import java.util.Map;

/**
 * @author victorp
 * Simulates X minor and then Y main events of the visitor
 * After all pages were retrieved getNextEvent(..) will return null
 */
public class VisitorSimulatorXMinorYMainEvents implements VisitorSimulator {
    private int minorCountToSend;
    private int mainCountToSend;
    private final String visitorId;
    private final String configId;

    private final VisitorGenericEventFactory visitorGenericEventFactory;

    public VisitorSimulatorXMinorYMainEvents(int minorCountToSend,
                                                 int mainCountToSend,
                                                 String visitorId,
                                                 String configId,
                                                 VisitorGenericEventFactory visitorGenericEventFactory) {
        this.minorCountToSend = minorCountToSend;
        this.mainCountToSend = mainCountToSend;
        this.visitorId = visitorId;
        this.configId = configId;
        this.visitorGenericEventFactory = visitorGenericEventFactory;
    }


    @Override
    public VisitorGenericEvent getNextEvent() {
        VisitorGenericEvent visitorGenericEvent = null;
        if (minorCountToSend > 0) {
            Map<String, String> data = new HashMap<String, String>();
            data.put("url", "http://localhost:8080/test/visitor/url");
            visitorGenericEvent = visitorGenericEventFactory.create(VisitorGenericEventId.MAIN, visitorId, configId, data);
            minorCountToSend--;
        }else if (mainCountToSend > 0) {
            Map<String, String> data = new HashMap<String, String>();
            data.put("url", "http://localhost:8080/test/visitor/url");
            data.put("title", "test-title");
            visitorGenericEvent = visitorGenericEventFactory.create(VisitorGenericEventId.MINOR, visitorId, configId, data);
            mainCountToSend--;
        }
     return visitorGenericEvent;
    }
}
