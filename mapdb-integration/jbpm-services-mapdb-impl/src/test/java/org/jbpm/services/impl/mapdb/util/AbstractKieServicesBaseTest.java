/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.services.impl.mapdb.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.kie.services.impl.FormManagerServiceImpl;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.ProcessServiceImpl;
import org.jbpm.kie.services.impl.UserTaskServiceImpl;
import org.jbpm.kie.services.impl.bpmn2.BPMN2DataServiceImpl;
import org.jbpm.runtime.manager.impl.DefaultRegisterableItemsFactory;
import org.jbpm.runtime.manager.impl.deploy.DeploymentDescriptorImpl;
import org.jbpm.runtime.manager.impl.error.ExecutionErrorHandlerInterceptor;
import org.jbpm.runtime.manager.mapdb.MapDBRuntimeManagerFactoryImpl;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.impl.mapdb.MapDBKModuleDeploymentService;
import org.jbpm.services.impl.mapdb.MapDBRuntimeDataServiceImpl;
import org.jbpm.services.impl.mapdb.event.MapDBProcessEventListener;
import org.jbpm.services.task.commands.TaskCommandExecutorImpl;
import org.jbpm.services.task.events.TaskEventSupport;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.jbpm.services.task.impl.command.CommandBasedTaskService;
import org.jbpm.services.task.mapdb.persistence.TaskTransactionInterceptor;
import org.jbpm.services.task.persistence.MapDBTaskPersistenceContextManager;
import org.junit.BeforeClass;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.api.task.TaskService;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.DeploymentDescriptorBuilder;
import org.kie.internal.runtime.conf.ObjectModel;
import org.kie.internal.task.api.UserGroupCallback;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arjuna.ats.jta.TransactionManager;

public abstract class AbstractKieServicesBaseTest {

	private static final Logger logger = LoggerFactory.getLogger(AbstractKieServicesBaseTest.class);

	protected static final String ARTIFACT_ID = "test-module";
	protected static final String GROUP_ID = "org.jbpm.test";
	protected static final String VERSION = "1.0.0-SNAPSHOT";

	
	protected DeploymentService deploymentService;
	protected DefinitionService bpmn2Service;
	protected RuntimeDataService runtimeDataService;
	protected ProcessService processService;
	protected UserTaskService userTaskService;

	protected TestIdentityProvider identityProvider;

    protected FormManagerService formManagerService;
    
    private DB db;
    private UserGroupCallback userGroupCallback;
    
    @BeforeClass
    public static void setupOnce() {        
        try {
            InitialContext initContext = new InitialContext();

            initContext.rebind("java:comp/UserTransaction", com.arjuna.ats.jta.UserTransaction.userTransaction());
            initContext.rebind("java:comp/TransactionManager", TransactionManager.transactionManager());
            initContext.rebind("java:comp/TransactionSynchronizationRegistry", new com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void close() {
        if (db != null) {
            db.close();
        }
    }

	protected void configureServices() {
	    
	    db = DBMaker.memoryDB().
                concurrencyScale(64).
                transactionEnable().
                make();
	    
	    Properties properties= new Properties();
        properties.setProperty("mary", "HR");
        properties.setProperty("john", "HR");
        userGroupCallback = new JBossUserGroupCallbackImpl(properties);
				
		identityProvider = new TestIdentityProvider();
        formManagerService = new FormManagerServiceImpl();

		// build definition service
		bpmn2Service = new BPMN2DataServiceImpl();

		// build deployment service
		deploymentService = new MapDBKModuleDeploymentService();
		((KModuleDeploymentService)deploymentService).setBpmn2Service(bpmn2Service);		
		((KModuleDeploymentService)deploymentService).setIdentityProvider(identityProvider);
		((KModuleDeploymentService)deploymentService).setManagerFactory(new MapDBRuntimeManagerFactoryImpl());
		((KModuleDeploymentService)deploymentService).setFormManagerService(formManagerService);
		((MapDBKModuleDeploymentService)deploymentService).setDb(db);
		((MapDBKModuleDeploymentService)deploymentService).setUserGroupCallback(userGroupCallback);
		
		TaskService taskService = newTaskService();

		// build runtime data service
		runtimeDataService = new MapDBRuntimeDataServiceImpl(db);		
		((MapDBRuntimeDataServiceImpl)runtimeDataService).setIdentityProvider(identityProvider);
		((MapDBRuntimeDataServiceImpl)runtimeDataService).setTaskService(taskService);
		((MapDBRuntimeDataServiceImpl)runtimeDataService).setUserGroupCallback(userGroupCallback);			
		
		((KModuleDeploymentService)deploymentService).setRuntimeDataService(runtimeDataService);
		((MapDBKModuleDeploymentService)deploymentService).setRegisterableItemsFactory(
		        
		        new DefaultRegisterableItemsFactory(){

                    @Override
                    public List<ProcessEventListener> getProcessEventListeners(RuntimeEngine runtime) {
                        List<ProcessEventListener> defaultListeners = new ArrayList<ProcessEventListener>();
                        defaultListeners.addAll(getEventListenerFromDescriptor(runtime, ProcessEventListener.class));   
                        
                        defaultListeners.add(new MapDBProcessEventListener(((MapDBRuntimeDataServiceImpl)runtimeDataService).getProcessInstancesById(), identityProvider));
                        
                        return defaultListeners;
                    }

                    @Override
                    public List<TaskLifeCycleEventListener> getTaskListeners() {
                        List<TaskLifeCycleEventListener> defaultListeners = new ArrayList<TaskLifeCycleEventListener>();                        
                        // add listeners from deployment descriptor
                        defaultListeners.addAll(getTaskListenersFromDescriptor());
                        
                        return defaultListeners;
                    }
                    
                }
		        );

		// set runtime data service as listener on deployment service
		((KModuleDeploymentService)deploymentService).addListener(((MapDBRuntimeDataServiceImpl) runtimeDataService));
		((KModuleDeploymentService)deploymentService).addListener(((BPMN2DataServiceImpl) bpmn2Service));		

		// build process service
		processService = new ProcessServiceImpl();
		((ProcessServiceImpl) processService).setDataService(runtimeDataService);
		((ProcessServiceImpl) processService).setDeploymentService(deploymentService);

		// build user task service
		userTaskService = new UserTaskServiceImpl();
		((UserTaskServiceImpl) userTaskService).setDataService(runtimeDataService);
		((UserTaskServiceImpl) userTaskService).setDeploymentService(deploymentService);
	}

    protected String getPom(ReleaseId releaseId, ReleaseId... dependencies) {
        String pom =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "  <groupId>" + releaseId.getGroupId() + "</groupId>\n" +
                "  <artifactId>" + releaseId.getArtifactId() + "</artifactId>\n" +
                "  <version>" + releaseId.getVersion() + "</version>\n" +
                "\n";
        if (dependencies != null && dependencies.length > 0) {
            pom += "<dependencies>\n";
            for (ReleaseId dep : dependencies) {
                pom += "<dependency>\n";
                pom += "  <groupId>" + dep.getGroupId() + "</groupId>\n";
                pom += "  <artifactId>" + dep.getArtifactId() + "</artifactId>\n";
                pom += "  <version>" + dep.getVersion() + "</version>\n";
                pom += "</dependency>\n";
            }
            pom += "</dependencies>\n";
        }
        pom += "</project>";
        return pom;
    }

    protected InternalKieModule createKieJar(KieServices ks, ReleaseId releaseId, List<String> resources) {
    	return createKieJar(ks, releaseId, resources, null);
    }

   protected InternalKieModule createKieJar(KieServices ks, ReleaseId releaseId, List<String> resources, Map<String, String> extraResources ) {


        KieFileSystem kfs = createKieFileSystemWithKProject(ks);
        kfs.writePomXML( getPom(releaseId) );

        if (createDescriptor()) {
            DeploymentDescriptor customDescriptor = new DeploymentDescriptorImpl("org.jbpm.domain");
            DeploymentDescriptorBuilder ddBuilder = customDescriptor.getBuilder();

            for (ObjectModel listener : getProcessListeners()) {
                ddBuilder.addEventListener(listener);
            }
            for (ObjectModel listener : getTaskListeners()) {
                ddBuilder.addTaskEventListener(listener);
            }
            if (extraResources == null) {
                extraResources = new HashMap<String, String>();
            }
            extraResources.put("src/main/resources/" + DeploymentDescriptor.META_INF_LOCATION, customDescriptor.toXml());
        }
        for (String resource : resources) {
            kfs.write("src/main/resources/KBase-test/" + resource, ResourceFactory.newClassPathResource(resource));
        }
        if (extraResources != null) {
	        for (Map.Entry<String, String> entry : extraResources.entrySet()) {
				kfs.write(entry.getKey(), ResourceFactory.newByteArrayResource(entry.getValue().getBytes()));
			}
        }

        KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        if (!kieBuilder.buildAll().getResults().getMessages().isEmpty()) {
            for (Message message : kieBuilder.buildAll().getResults().getMessages()) {
                logger.error("Error Message: ({}) {}", message.getPath(), message.getText());
            }
            throw new RuntimeException(
                    "There are errors builing the package, please check your knowledge assets!");
        }

        return ( InternalKieModule ) kieBuilder.getKieModule();
    }



    protected KieFileSystem createKieFileSystemWithKProject(KieServices ks) {
        KieModuleModel kproj = ks.newKieModuleModel();

        KieBaseModel kieBaseModel1 = kproj.newKieBaseModel("KBase-test").setDefault(true).addPackage("*")
                .setEqualsBehavior( EqualityBehaviorOption.EQUALITY )
                .setEventProcessingMode( EventProcessingOption.STREAM );


        kieBaseModel1.newKieSessionModel("ksession-test").setDefault(true)
                .setType(KieSessionModel.KieSessionType.STATEFUL)
                .setClockType( ClockTypeOption.get("realtime") )
                .newWorkItemHandlerModel("Log", "new org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler()");

        kieBaseModel1.newKieSessionModel("ksession-test-2").setDefault(false)
        .setType(KieSessionModel.KieSessionType.STATEFUL)
        .setClockType( ClockTypeOption.get("realtime") )
        .newWorkItemHandlerModel("Log", "new org.jbpm.kie.services.test.objects.KieConteinerSystemOutWorkItemHandler(kieContainer)");

        kieBaseModel1.newKieSessionModel("ksession-test2").setDefault(false)
        .setType(KieSessionModel.KieSessionType.STATEFUL)
        .setClockType( ClockTypeOption.get("realtime") );

        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.writeKModuleXML(kproj.toXML());
        return kfs;
    }

    public static void cleanupSingletonSessionId() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        if (tempDir.exists()) {

            String[] jbpmSerFiles = tempDir.list(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {

                    return name.endsWith("-jbpmSessionId.ser");
                }
            });
            for (String file : jbpmSerFiles) {
                logger.debug("Temp dir to be removed {} file {}",tempDir, file);
                new File(tempDir, file).delete();
            }
        }
    }

    protected boolean createDescriptor() {
        return false;
    }

    protected List<ObjectModel> getProcessListeners() {
        return new ArrayList<>();
    }

    protected List<ObjectModel> getTaskListeners() {
        return new ArrayList<>();
    }

	public void setDeploymentService(DeploymentService deploymentService) {
		this.deploymentService = deploymentService;
	}

	public void setBpmn2Service(DefinitionService bpmn2Service) {
		this.bpmn2Service = bpmn2Service;
	}

	public void setRuntimeDataService(RuntimeDataService runtimeDataService) {
		this.runtimeDataService = runtimeDataService;
	}

	public void setProcessService(ProcessService processService) {
		this.processService = processService;
	}

	public void setUserTaskService(UserTaskService userTaskService) {
		this.userTaskService = userTaskService;
	}

    public void setIdentityProvider(TestIdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }
   
    protected TaskService newTaskService() {
        Environment env = KieServices.get().newEnvironment();
        
        TaskEventSupport taskEventSupport = new TaskEventSupport();
        MapDBTaskPersistenceContextManager tpcm = new MapDBTaskPersistenceContextManager(env);
        env.set(EnvironmentName.TASK_PERSISTENCE_CONTEXT_MANAGER, tpcm);
        env.set(EnvironmentName.TASK_USER_GROUP_CALLBACK, userGroupCallback);
        TaskCommandExecutorImpl commandExecutor = new TaskCommandExecutorImpl(env, taskEventSupport);
        commandExecutor.addInterceptor(new TaskTransactionInterceptor(env));
        commandExecutor.addInterceptor(new ExecutionErrorHandlerInterceptor(env));
        CommandBasedTaskService taskService = new CommandBasedTaskService(commandExecutor, taskEventSupport);
        return taskService;

    }
}
