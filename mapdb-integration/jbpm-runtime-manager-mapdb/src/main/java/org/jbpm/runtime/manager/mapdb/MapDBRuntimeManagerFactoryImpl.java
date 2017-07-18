package org.jbpm.runtime.manager.mapdb;

import org.jbpm.runtime.manager.impl.RuntimeManagerFactoryImpl;
import org.jbpm.runtime.manager.impl.SimpleRuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.SessionFactory;
import org.kie.internal.runtime.manager.TaskServiceFactory;


public class MapDBRuntimeManagerFactoryImpl extends RuntimeManagerFactoryImpl {

    @Override
    protected SessionFactory getSessionFactory(RuntimeEnvironment environment) {
        return new MapDBSessionFactory(environment);
    }

    @Override
    protected TaskServiceFactory getTaskServiceFactory(RuntimeEnvironment environment) {
        return new MapDBTaskServiceFactory(environment);
    }

    @Override
    public RuntimeManager newSingletonRuntimeManager(RuntimeEnvironment environment, String identifier) {
        adjustEnvironment(environment);
        return super.newSingletonRuntimeManager(environment, identifier);
    }

    @Override
    public RuntimeManager newPerRequestRuntimeManager(RuntimeEnvironment environment, String identifier) {
        adjustEnvironment(environment);
        return super.newPerRequestRuntimeManager(environment, identifier);
    }

    @Override
    public RuntimeManager newPerProcessInstanceRuntimeManager(RuntimeEnvironment environment, String identifier) {
        adjustEnvironment(environment);
        return super.newPerProcessInstanceRuntimeManager(environment, identifier);
    }

    @Override
    public RuntimeManager newPerCaseRuntimeManager(RuntimeEnvironment environment, String identifier) {
        adjustEnvironment(environment);
        return super.newPerCaseRuntimeManager(environment, identifier);
    }

    protected void adjustEnvironment(RuntimeEnvironment environment) {
        ((SimpleRuntimeEnvironment)environment).setMapper(new MapDBMapper(environment));
    }
}
