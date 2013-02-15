package com.drools.memory.banchmark.singleConfig;

import com.drools.memory.banchmark.event.prcessor.VisitorGenericEvent;


/**
 * @author victorp
 */
public interface VisitorSimulator {
    /**
     * @return the next event of this visitor
     *         in case no events are available returns null
     */
    VisitorGenericEvent getNextEvent();
}
