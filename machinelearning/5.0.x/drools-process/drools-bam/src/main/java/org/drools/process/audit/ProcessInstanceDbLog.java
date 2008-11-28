package org.drools.process.audit;

import java.util.List;

import org.hibernate.Session;

public class ProcessInstanceDbLog {
    
    public static List<ProcessInstanceLog> findProcessInstances() {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        List result = session.createQuery("from ProcessInstanceLog").list();
        session.getTransaction().commit();
        return result;
    }

    public static List<ProcessInstanceLog> findProcessInstances(String processId) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        List result = session.createQuery(
            "from ProcessInstanceLog as log where log.processId = ?")
                .setString(0, processId).list();
        session.getTransaction().commit();
        return result;
    }

}
