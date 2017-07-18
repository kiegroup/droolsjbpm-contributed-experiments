package org.jbpm.runtime.manager.mapdb;

import org.kie.api.KieServices;
import org.kie.api.persistence.jpa.KieStoreServices;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.internal.runtime.manager.SessionFactory;

public class MapDBSessionFactory implements SessionFactory {

    private RuntimeEnvironment environment;
    private KieStoreServices storeServices = KieServices.Factory.get().getStoreServices();
    
    public MapDBSessionFactory(RuntimeEnvironment environment) {
        this.environment = environment;
    }
    
    @Override
    public KieSession newKieSession() {
        KieSession ksession = storeServices.newKieSession(environment.getKieBase(), environment.getConfiguration(), environment.getEnvironment());
        return ksession;
    }

    @Override
    public KieSession findKieSessionById(Long sessionId) {
        KieSession ksession = storeServices.loadKieSession(sessionId, environment.getKieBase(), environment.getConfiguration(), environment.getEnvironment());
        return ksession;
    }

    @Override
    public void close() {
        // no-op
        
    }

    @Override
    public void onDispose(Long sessionId) {
        // no-op
        
    }

}
