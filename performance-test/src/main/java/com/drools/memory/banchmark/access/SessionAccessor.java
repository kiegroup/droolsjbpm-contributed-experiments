package com.drools.memory.banchmark.access;

import org.drools.runtime.StatefulKnowledgeSession;

/**
 * @author victorp
 */
public class SessionAccessor {
    private final  StatefulKnowledgeSession statefulKnowledgeSession;

    public SessionAccessor(StatefulKnowledgeSession statefulKnowledgeSession) {
        this.statefulKnowledgeSession = statefulKnowledgeSession;
    }

    public synchronized void execute(SessionAction sessionAction){
        sessionAction.execute(statefulKnowledgeSession);
    }
}
