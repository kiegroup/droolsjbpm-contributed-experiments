package com.drools.memory.banchmark.ruleEngine;

import com.drools.memory.banchmark.access.SessionAccessor;
import com.drools.memory.banchmark.access.SessionAction;
import com.drools.memory.banchmark.dao.ConfigDao;
import com.drools.memory.banchmark.model.events.VisitorEvent;
import com.drools.memory.banchmark.model.state.VisitorRTData;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.runtime.conf.KeepReferenceOption;
import org.springframework.beans.factory.annotation.Required;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Victor
 */
public class RuleEngine {

    private KnowledgeBase knowledgeBase;
    private String configId;
    private ConfigDao configDao;
    private ConcurrentHashMap<String, SessionAccessor> visitorSessions = new ConcurrentHashMap<String, SessionAccessor>();
    private Object sessionsLock = new Object();

    @Required
    public void setKnowledgeBase(KnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    @Required
    public void setConfigId(String configId) {
        this.configId = configId;
    }

    @Required
    public void setConfigDao(ConfigDao configDao) {
        this.configDao = configDao;
    }



    public void processVisitorEvent(final VisitorEvent visitorEvent) {
        if (!visitorEvent.getConfigId().equals(configId)){
            throw new RuntimeException(String.format("This SingleConfigDroolsRulesEngine can handle events of config: %s only, but was: %", configId,visitorEvent.getConfigId()));
        }

        SessionAccessor sessionAccessor = visitorSessions.get(visitorEvent.getVisitorId());
        if (sessionAccessor == null){
            synchronized (sessionsLock){
                visitorSessions.putIfAbsent(visitorEvent.getVisitorId(), new SessionAccessor(initVisitorSession(visitorEvent.getVisitorId())));
            }
        }
        sessionAccessor = visitorSessions.get(visitorEvent.getVisitorId());

        sessionAccessor.execute(new SessionAction() {
            @Override
            public void execute(StatefulKnowledgeSession visitorSession) {
                visitorSession.insert(visitorEvent);
                visitorSession.fireAllRules();
            }
        });
    }


    private synchronized StatefulKnowledgeSession initVisitorSession(String visitorId) {
        KnowledgeSessionConfiguration conf =
                KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        conf.setOption(ClockTypeOption.get("pseudo"));
        conf.setOption(KeepReferenceOption.NO);

        StatefulKnowledgeSession visitorSession = knowledgeBase.newStatefulKnowledgeSession(conf, null);
        visitorSession.insert(configDao);
        visitorSession.insert(new VisitorRTData(visitorId));
        return visitorSession;
    }

}