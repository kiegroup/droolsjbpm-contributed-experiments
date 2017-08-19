/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.services.jbpm.mapdb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.drools.core.impl.InternalKieContainer;
import org.jbpm.document.service.impl.DocumentImpl;
import org.jbpm.kie.services.impl.AbstractDeploymentService;
import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.kie.services.impl.FormManagerServiceImpl;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.services.impl.ProcessServiceImpl;
import org.jbpm.kie.services.impl.UserTaskServiceImpl;
import org.jbpm.kie.services.impl.bpmn2.BPMN2DataServiceImpl;
import org.jbpm.runtime.manager.impl.DefaultRegisterableItemsFactory;
import org.jbpm.runtime.manager.impl.deploy.DeploymentDescriptorManager;
import org.jbpm.runtime.manager.impl.deploy.DeploymentDescriptorMerger;
import org.jbpm.runtime.manager.impl.identity.UserDataServiceProvider;
import org.jbpm.runtime.manager.mapdb.MapDBRuntimeManagerFactoryImpl;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.impl.mapdb.MapDBKModuleDeploymentService;
import org.jbpm.services.impl.mapdb.MapDBRuntimeDataServiceImpl;
import org.jbpm.services.impl.mapdb.event.MapDBProcessEventListener;
import org.jbpm.services.task.identity.JAASUserGroupCallbackImpl;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.MergeMode;
import org.kie.internal.runtime.conf.ObjectModel;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.scanner.KieModuleMetaData;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.Severity;
import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.DefinitionServiceBase;
import org.kie.server.services.jbpm.JBPMKieContainerCommandServiceImpl;
import org.kie.server.services.jbpm.JBPMKieSessionLookupHandler;
import org.kie.server.services.jbpm.ProcessServiceBase;
import org.kie.server.services.jbpm.RuntimeDataServiceBase;
import org.kie.server.services.jbpm.UserTaskServiceBase;
import org.kie.server.services.jbpm.security.JMSUserGroupAdapter;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapDBjBPMKieServerExtension implements KieServerExtension {

    public static final String EXTENSION_NAME = "jBPM";

    private static final Logger logger = LoggerFactory.getLogger(MapDBjBPMKieServerExtension.class);

    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty("org.jbpm.mapdb.server.ext.disabled", "false"));

    private KieServerImpl kieServer;
    private KieServerRegistry context;

    private DeploymentService deploymentService;
    private DefinitionService definitionService;
    private ProcessService processService;
    private UserTaskService userTaskService;
    private RuntimeDataService runtimeDataService;
    private FormManagerService formManagerService;

    private KieContainerCommandService kieContainerCommandService;

    private DeploymentDescriptorManager deploymentDescriptorManager = new DeploymentDescriptorManager("");
    private DeploymentDescriptorMerger merger = new DeploymentDescriptorMerger();

    private List<Object> services = new ArrayList<Object>();
    private boolean initialized = false;
    
    private DB db;

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isActive() {
        return disabled == false;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
  
        db = DBMaker.fileDB(getLocation() + File.separator + "jbpm.mapdb").
                concurrencyScale(64).
                transactionEnable().
                fileMmapEnableIfSupported().
                fileMmapPreclearDisable().
                cleanerHackEnable().
                make();

        // loaded from system property as callback info isn't stored as configuration in kie server repository
        String callbackConfig = System.getProperty(KieServerConstants.CFG_HT_CALLBACK);

        // if no other callback set, use jaas by default
        if (callbackConfig == null || callbackConfig.isEmpty()) {
            System.setProperty(KieServerConstants.CFG_HT_CALLBACK, "jaas");
            JAASUserGroupCallbackImpl.addExternalUserGroupAdapter(new JMSUserGroupAdapter());
        }
        

        // configure user group callback
        UserGroupCallback userGroupCallback = UserDataServiceProvider.getUserGroupCallback();

        this.kieServer = kieServer;
        this.context = registry;

        formManagerService = new FormManagerServiceImpl();

        // build definition service
        definitionService = new BPMN2DataServiceImpl();

        // build deployment service
        deploymentService = new MapDBKModuleDeploymentService();
        ((KModuleDeploymentService)deploymentService).setBpmn2Service(definitionService);        
        ((KModuleDeploymentService)deploymentService).setIdentityProvider(registry.getIdentityProvider());
        ((KModuleDeploymentService)deploymentService).setManagerFactory(new MapDBRuntimeManagerFactoryImpl());
        ((KModuleDeploymentService)deploymentService).setFormManagerService(formManagerService);
        ((MapDBKModuleDeploymentService)deploymentService).setDb(db);
        ((MapDBKModuleDeploymentService)deploymentService).setUserGroupCallback(userGroupCallback);


        // build runtime data service
        runtimeDataService = new MapDBRuntimeDataServiceImpl(db);       
        ((MapDBRuntimeDataServiceImpl)runtimeDataService).setIdentityProvider(registry.getIdentityProvider());        
        ((MapDBRuntimeDataServiceImpl)runtimeDataService).setUserGroupCallback(userGroupCallback);          
        
        ((KModuleDeploymentService)deploymentService).setRuntimeDataService(runtimeDataService);
        ((MapDBKModuleDeploymentService)deploymentService).setRegisterableItemsFactory(
                
                new DefaultRegisterableItemsFactory(){

                    @Override
                    protected Object getInstanceFromModel(ObjectModel model, ClassLoader classloader, Map<String, Object> contaxtParams) {
                        contaxtParams.remove("entityManagerFactory");
                        return super.getInstanceFromModel(model, classloader, contaxtParams);
                    }

                    @Override
                    public List<ProcessEventListener> getProcessEventListeners(RuntimeEngine runtime) {
                        List<ProcessEventListener> defaultListeners = new ArrayList<ProcessEventListener>();
                        defaultListeners.addAll(getEventListenerFromDescriptor(runtime, ProcessEventListener.class));   
                        
                        defaultListeners.add(new MapDBProcessEventListener(((MapDBRuntimeDataServiceImpl)runtimeDataService).getProcessInstancesById(), registry.getIdentityProvider()));
                        
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
        ((KModuleDeploymentService)deploymentService).addListener(((BPMN2DataServiceImpl) definitionService));   

        // build process service
        processService = new ProcessServiceImpl();
        ((ProcessServiceImpl) processService).setDataService(runtimeDataService);
        ((ProcessServiceImpl) processService).setDeploymentService(deploymentService);

        // build user task service
        userTaskService = new UserTaskServiceImpl();
        ((UserTaskServiceImpl) userTaskService).setDataService(runtimeDataService);
        ((UserTaskServiceImpl) userTaskService).setDeploymentService(deploymentService);


        this.kieContainerCommandService = new JBPMKieContainerCommandServiceImpl(context, deploymentService, new DefinitionServiceBase(definitionService, context),
                new ProcessServiceBase(processService, definitionService, runtimeDataService, context), new UserTaskServiceBase(userTaskService, context),
                new RuntimeDataServiceBase(runtimeDataService, context), 
                null, 
                null,
                null, 
                null,
                null);

        if (registry.getKieSessionLookupManager() != null) {
            registry.getKieSessionLookupManager().addHandler(new JBPMKieSessionLookupHandler());
        }

        services.add(formManagerService);
        services.add(deploymentService);
        services.add(definitionService);
        services.add(processService);
        services.add(userTaskService);
        services.add(runtimeDataService);

        initialized = true;
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        ((AbstractDeploymentService)deploymentService).shutdown();
        
        if (db != null) {
            db.close();
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        List<Message> messages = (List<Message>) parameters.get(KieServerConstants.KIE_SERVER_PARAM_MESSAGES);
        try {
            KieModuleMetaData metaData = (KieModuleMetaData) parameters.get(KieServerConstants.KIE_SERVER_PARAM_MODULE_METADATA);
            if (metaData.getProcesses() == null || metaData.getProcesses().isEmpty()) {
                logger.info("Container {} does not include processes, {} skipped", id, this);
                return;
            }

            
            boolean hasStatefulSession = false;
            boolean hasDefaultSession = false;
            // let validate if they are any stateful sessions defined and in case there are not, skip this container
            InternalKieContainer kieContainer = (InternalKieContainer)kieContainerInstance.getKieContainer();
            Collection<String> kbaseNames = kieContainer.getKieBaseNames();
            Collection<String> ksessionNames = new ArrayList<String>();
            for (String kbaseName : kbaseNames) {
                ksessionNames = kieContainer.getKieSessionNamesInKieBase(kbaseName);

                for (String ksessionName : ksessionNames) {
                    KieSessionModel model = kieContainer.getKieSessionModel(ksessionName);
                    if (model.getType().equals(KieSessionModel.KieSessionType.STATEFUL)) {
                        hasStatefulSession = true;
                    }

                    if (model.isDefault()) {
                        hasDefaultSession = true;
                    }
                }
            }
            if (!hasStatefulSession) {
                logger.info("Container {} does not define stateful ksession thus cannot be handled by extension {}", id, this);
                return;
            }
            ReleaseId releaseId = kieContainerInstance.getKieContainer().getReleaseId();

            KModuleDeploymentUnit unit = new CustomIdKmoduleDeploymentUnit(id, releaseId.getGroupId(), releaseId.getArtifactId(), releaseId.getVersion());

            if (!hasDefaultSession) {
                unit.setKbaseName(kbaseNames.iterator().next());
                unit.setKsessionName(ksessionNames.iterator().next());
            }
            // override defaults if config options are given
            KieServerConfig config = new KieServerConfig(kieContainerInstance.getResource().getConfigItems());

            String runtimeStrategy = config.getConfigItemValue(KieServerConstants.PCFG_RUNTIME_STRATEGY);
            if (runtimeStrategy != null && !runtimeStrategy.isEmpty()) {
                unit.setStrategy(RuntimeStrategy.valueOf(runtimeStrategy));
            }
            String mergeMode = config.getConfigItemValue(KieServerConstants.PCFG_MERGE_MODE);
            if (mergeMode != null && !mergeMode.isEmpty()) {
                unit.setMergeMode(MergeMode.valueOf(mergeMode));
            }
            String ksession = config.getConfigItemValue(KieServerConstants.PCFG_KIE_SESSION);
            unit.setKsessionName(ksession);
            String kbase = config.getConfigItemValue(KieServerConstants.PCFG_KIE_BASE);
            unit.setKbaseName(kbase);

            // reuse kieContainer to avoid unneeded bootstrap
            unit.setKieContainer(kieContainer);

            if (System.getProperty(KieServerConstants.CFG_JBPM_TASK_CLEANUP_LISTENER, "true").equalsIgnoreCase("true")) {
                logger.debug("Registering TaskCleanUpProcessEventListener");
                addTaskCleanUpProcessListener(unit, kieContainer);
            }

            if (System.getProperty(KieServerConstants.CFG_JBPM_PROCESS_IDENTITY_LISTENER, "true").equalsIgnoreCase("true")) {
                logger.debug("Registering IdentityProviderAwareProcessListener");
                addProcessIdentityProcessListener(unit, kieContainer);
            }

            deploymentService.deploy(unit);
            // in case it was deployed successfully pass all known classes to marshallers (jaxb, json etc)
            DeployedUnit deployedUnit = deploymentService.getDeployedUnit(unit.getIdentifier());
            Set<Class<?>> customClasses = new HashSet<Class<?>>(deployedUnit.getDeployedClasses());
            // add custom classes that come from extension itself
            customClasses.add(DocumentImpl.class);
            kieContainerInstance.addExtraClasses(customClasses);

            logger.debug("Container {} created successfully by extension {}", id, this);
        } catch (Exception e) {
            
            messages.add(new Message(Severity.ERROR, "Error when creating container " + id +" by extension " + this + " due to " + ExceptionUtils.getRootCause(e).getMessage()));
            logger.error("Error when creating container {} by extension {}", id, this, e);
        }
    }

    @Override
    public boolean isUpdateContainerAllowed(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // first check if there are any active process instances
        List<Integer> states = new ArrayList<Integer>();
        states.add(ProcessInstance.STATE_ACTIVE);
        states.add(ProcessInstance.STATE_PENDING);
        states.add(ProcessInstance.STATE_SUSPENDED);
        Collection<ProcessInstanceDesc> activeProcesses = runtimeDataService.getProcessInstancesByDeploymentId(id, states, new QueryContext());
        if (!activeProcesses.isEmpty()) {
            parameters.put(KieServerConstants.FAILURE_REASON_PROP, "Update of container forbidden - there are active process instances for container " + id);
            return false;
        }

        return true;
    }

    @Override
    public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // essentially it's a redeploy to make sure all components are up to date,
        // though update of kie base is done only once on kie server level and KieContainer is reused across all extensions
        disposeContainer(id, kieContainerInstance, parameters);

        createContainer(id, kieContainerInstance, parameters);
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        if (!deploymentService.isDeployed(id)) {
            logger.info("No container with id {} found", id);
            return;
        }
        List<Integer> states = new ArrayList<Integer>();
        states.add(ProcessInstance.STATE_ACTIVE);
        states.add(ProcessInstance.STATE_PENDING);
        states.add(ProcessInstance.STATE_SUSPENDED);

        KModuleDeploymentUnit unit = (KModuleDeploymentUnit) deploymentService.getDeployedUnit(id).getDeploymentUnit();
        deploymentService.undeploy(new CustomIdKmoduleDeploymentUnit(id, unit.getGroupId(), unit.getArtifactId(), unit.getVersion()));
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
            = ServiceLoader.load(KieServerApplicationComponentsService.class);
        List<Object> appComponentsList = new ArrayList<Object>();
        Object [] services = {
                deploymentService,
                definitionService,
                processService,
                userTaskService,
                runtimeDataService,
                formManagerService,                
                context
        };
        for( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) {
           appComponentsList.addAll(appComponentsService.getAppComponents(EXTENSION_NAME, type, services));
        }
        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        if (serviceType.isAssignableFrom(kieContainerCommandService.getClass())) {
            return (T) kieContainerCommandService;
        }

        Object [] services = {
                deploymentService,
                definitionService,
                processService,
                userTaskService,
                runtimeDataService,
                formManagerService,
                context
        };

        for (Object service : services) {
            if (service != null && serviceType.isAssignableFrom(service.getClass())) {
                return (T) service;
            }
        }

        return null;
    }

    @Override
    public String getImplementedCapability() {
        return KieServerConstants.CAPABILITY_BPM;
    }

    @Override
    public List<Object> getServices() {
        return services;
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public Integer getStartOrder() {
        return 0;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }

    private static class CustomIdKmoduleDeploymentUnit extends KModuleDeploymentUnit {

        private String id;

        public CustomIdKmoduleDeploymentUnit(String id, String groupId, String artifactId, String version) {
            super(groupId, artifactId, version);
            this.id = id;
        }

        @Override
        public String getIdentifier() {
            return this.id;
        }
    }

   
    protected void addTaskCleanUpProcessListener(final KModuleDeploymentUnit unit, final InternalKieContainer kieContainer) {
        final DeploymentDescriptor descriptor = getDeploymentDescriptor(unit, kieContainer);
        descriptor.getBuilder().addEventListener(
                new ObjectModel(
                        "mvel",
                        "new org.jbpm.services.task.admin.listener.TaskCleanUpProcessEventListener(taskService)"
                )
        );
        unit.setDeploymentDescriptor(descriptor);
    }

    protected void addProcessIdentityProcessListener(final KModuleDeploymentUnit unit, final InternalKieContainer kieContainer) {
        final DeploymentDescriptor descriptor = getDeploymentDescriptor(unit, kieContainer);
        descriptor.getBuilder().addEventListener(
                new ObjectModel(
                        "mvel",
                        "new org.jbpm.kie.services.impl.IdentityProviderAwareProcessListener(ksession)"
                )
        );
        unit.setDeploymentDescriptor(descriptor);
    }

    protected DeploymentDescriptor getDeploymentDescriptor(KModuleDeploymentUnit unit, InternalKieContainer kieContainer) {
        DeploymentDescriptor descriptor = unit.getDeploymentDescriptor();
        if (descriptor == null) {
            List<DeploymentDescriptor> descriptorHierarchy = deploymentDescriptorManager.getDeploymentDescriptorHierarchy(kieContainer);
            descriptor = merger.merge(descriptorHierarchy, MergeMode.MERGE_COLLECTIONS);
        }
        return descriptor;
    }

    protected String getLocation() {
        String location = System.getProperty("jbpm.data.dir", System.getProperty("jboss.server.data.dir"));
        if (location == null) {
            location = System.getProperty("java.io.tmpdir");
        }
        return location;
    }
}