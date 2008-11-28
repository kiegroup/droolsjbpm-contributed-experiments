package org.drools.process.audit;

import java.util.Date;

import org.drools.WorkingMemory;
import org.drools.audit.WorkingMemoryLogger;
import org.drools.audit.event.LogEvent;
import org.drools.audit.event.RuleFlowLogEvent;
import org.drools.event.KnowledgeRuntimeEventManager;
import org.hibernate.Session;

public class WorkingMemoryDbLogger extends WorkingMemoryLogger {
    
    public WorkingMemoryDbLogger(WorkingMemory workingMemory) {
        super(workingMemory);
    }
    
    public WorkingMemoryDbLogger(KnowledgeRuntimeEventManager session) {
    	super(session);
    }

    public void logEventCreated(LogEvent logEvent) {
        switch (logEvent.getType()) {
            case LogEvent.BEFORE_RULEFLOW_CREATED:
                RuleFlowLogEvent event = (RuleFlowLogEvent) logEvent;
                addLog(event.getProcessInstanceId(), event.getProcessId());
                break;
            case LogEvent.AFTER_RULEFLOW_COMPLETED:
                event = (RuleFlowLogEvent) logEvent;
                updateLog(event.getProcessInstanceId());
                break;
            default:
                // ignore all other events
        }
    }

    private void addLog(long processInstanceId, String processId) {
        ProcessInstanceLog log = new ProcessInstanceLog(processInstanceId, processId);
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        session.save(log);
        session.getTransaction().commit();
    }
    
    private void updateLog(long processInstanceId) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        ProcessInstanceLog log = (ProcessInstanceLog) session.load(ProcessInstanceLog.class, processInstanceId);
        log.setEnd(new Date());
        session.update(log);
        session.getTransaction().commit();
    }
}
