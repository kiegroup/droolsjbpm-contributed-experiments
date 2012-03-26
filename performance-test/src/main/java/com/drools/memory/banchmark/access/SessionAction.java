package com.drools.memory.banchmark.access;

import org.drools.runtime.StatefulKnowledgeSession;

/**
 * @author victorp
 */
public interface SessionAction {
    void execute(StatefulKnowledgeSession visitorSession);
}
