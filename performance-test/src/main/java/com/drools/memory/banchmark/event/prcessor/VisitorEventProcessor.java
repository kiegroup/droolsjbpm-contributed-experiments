package com.drools.memory.banchmark.event.prcessor;

import com.drools.memory.banchmark.ruleEngine.RuleEngine;
import com.drools.memory.banchmark.model.events.VisitorEvent;
import com.drools.memory.banchmark.transform.EventTransformer;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Victor
 */
public class VisitorEventProcessor{
    private EventTransformer eventTransformer;
    private RuleEngine ruleEngine;

    @Required
    public void setEventTransformer(EventTransformer eventTransformer) {
        this.eventTransformer = eventTransformer;
    }

    @Required
    public void setVisitorCEP(RuleEngine visitorCEP) {
        this.ruleEngine = visitorCEP;
    }

    public void processEvent(VisitorGenericEvent visitorGenericEvent) {
        final VisitorEvent visitorEvent = eventTransformer.transform(visitorGenericEvent);
        ruleEngine.processVisitorEvent(visitorEvent);
    }


}