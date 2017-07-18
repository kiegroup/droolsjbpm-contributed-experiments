package org.jbpm.runtime.manager.mapdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;

import org.drools.persistence.mapdb.KnowledgeStoreServiceImpl;
import org.drools.persistence.mapdb.MapDBEnvironmentName;
import org.jbpm.runtime.manager.impl.DefaultRegisterableItemsFactory;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.SessionNotFoundException;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.task.api.UserGroupCallback;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import com.arjuna.ats.jta.TransactionManager;

public class PerProcessInstanceRuntimeManagerTest {

    private DB db;
    private UserGroupCallback userGroupCallback;
    
    @BeforeClass
    public static void setupOnce() {
        System.setProperty("org.jbpm.runtime.manager.class", MapDBRuntimeManagerFactoryImpl.class.getName());
        
        try {
            InitialContext initContext = new InitialContext();

            initContext.rebind("java:comp/UserTransaction", com.arjuna.ats.jta.UserTransaction.userTransaction());
            initContext.rebind("java:comp/TransactionManager", TransactionManager.transactionManager());
            initContext.rebind("java:comp/TransactionSynchronizationRegistry", new com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new KnowledgeStoreServiceImpl();
    }
    
    @Before
    public void setup() {
        
        Properties properties= new Properties();
        properties.setProperty("mary", "HR");
        properties.setProperty("john", "HR");
        userGroupCallback = new JBossUserGroupCallbackImpl(properties);
        
        db = DBMaker.memoryDB().
                concurrencyScale(64).
                transactionEnable().
                make();
    }
    
    @After
    public void cleanup() {
        if (db != null) {
            db.close();
        }
    }
    
    @Test
    public void testScriptTask() {
        RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
                .newEmptyBuilder()
                .addEnvironmentEntry(MapDBEnvironmentName.DB_OBJECT, db)
                .addAsset(ResourceFactory.newClassPathResource("BPMN2-ScriptTask.bpmn2"), ResourceType.BPMN2)
                .get();
        
        RuntimeManager manager = RuntimeManagerFactory.Factory.get().newPerProcessInstanceRuntimeManager(environment);        
        assertNotNull(manager);
        
        RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = runtime.getKieSession();
        assertNotNull(ksession);       
        
  
        ksession.startProcess("ScriptTask");
        
        manager.disposeRuntimeEngine(runtime);      
        // close manager which will close session maintained by the manager
        manager.close();
    }
    
    @Test
    public void testUserTask() {
        RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
                .newEmptyBuilder()
                .userGroupCallback(userGroupCallback)
                .registerableItemsFactory(new DefaultRegisterableItemsFactory(){

                    @Override
                    public List<ProcessEventListener> getProcessEventListeners(RuntimeEngine runtime) {
                        List<ProcessEventListener> defaultListeners = new ArrayList<ProcessEventListener>();
                        defaultListeners.addAll(getEventListenerFromDescriptor(runtime, ProcessEventListener.class));        
                        return defaultListeners;
                    }

                    @Override
                    public List<TaskLifeCycleEventListener> getTaskListeners() {
                        List<TaskLifeCycleEventListener> defaultListeners = new ArrayList<TaskLifeCycleEventListener>();                        
                        // add listeners from deployment descriptor
                        defaultListeners.addAll(getTaskListenersFromDescriptor());
                        
                        return defaultListeners;
                    }
                    
                })
                .addEnvironmentEntry(MapDBEnvironmentName.DB_OBJECT, db)
                .addAsset(ResourceFactory.newClassPathResource("BPMN2-UserTask.bpmn2"), ResourceType.BPMN2)
                .get();
        
        RuntimeManager manager = RuntimeManagerFactory.Factory.get().newPerProcessInstanceRuntimeManager(environment);        
        assertNotNull(manager);
        
        RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = runtime.getKieSession();
        assertNotNull(ksession);       
        
        ProcessInstance pi = ksession.startProcess("UserTask");
        assertEquals(ProcessInstance.STATE_ACTIVE, pi.getState());
        
        manager.disposeRuntimeEngine(runtime); 
        
        runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(pi.getId()));
        TaskService taskService = runtime.getTaskService();
        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
        assertEquals(1, tasks.size());
        manager.disposeRuntimeEngine(runtime); 
        
        long taskId = tasks.get(0).getId();
        
        runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(pi.getId()));
        taskService = runtime.getTaskService();
        taskService.start(taskId, "john");
        
        taskService.complete(taskId, "john", null);
        manager.disposeRuntimeEngine(runtime);  
        
        try {
            runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(pi.getId()));
            runtime.getKieSession();
            fail("Process instance is already completed");
        } catch (SessionNotFoundException e) {
            // expected
            manager.disposeRuntimeEngine(runtime);
        }
        
            
        // close manager which will close session maintained by the manager
        manager.close();
    }
}
