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

package org.jbpm.services.impl.mapdb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.marshalling.impl.ClassObjectMarshallingStrategyAcceptor;
import org.drools.core.marshalling.impl.SerializablePlaceholderResolverStrategy;
import org.drools.persistence.mapdb.MapDBEnvironmentName;
import org.jbpm.kie.services.impl.DeployedUnitImpl;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.services.impl.model.ProcessAssetDesc;
import org.jbpm.process.audit.event.AuditEventBuilder;
import org.jbpm.runtime.manager.impl.SimpleRegisterableItemsFactory;
import org.jbpm.runtime.manager.impl.deploy.DeploymentDescriptorImpl;
import org.jbpm.runtime.manager.impl.deploy.DeploymentDescriptorManager;
import org.jbpm.runtime.manager.impl.deploy.DeploymentDescriptorMerger;
import org.jbpm.services.api.model.DeployedAsset;
import org.jbpm.services.api.model.DeployedUnit;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.manager.RegisterableItemsFactory;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.MergeMode;
import org.kie.internal.runtime.conf.NamedObjectModel;
import org.kie.internal.runtime.conf.ObjectModel;
import org.kie.api.task.UserGroupCallback;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MapDBKModuleDeploymentService extends KModuleDeploymentService {
    
    private static Logger logger = LoggerFactory.getLogger(MapDBKModuleDeploymentService.class);

    private DB db;
    private RegisterableItemsFactory registerableItemsFactory;
    private UserGroupCallback userGroupCallback;
    
    private DeploymentDescriptorMerger merger = new DeploymentDescriptorMerger();
    
    @Override
    public void onInit() {
    }    

    public void setDb(DB db) {
        this.db = db;
    }    
    
    public void setRegisterableItemsFactory(RegisterableItemsFactory registerableItemsFactory) {
        this.registerableItemsFactory = registerableItemsFactory;
    }

    
    public void setUserGroupCallback(UserGroupCallback userGroupCallback) {
        this.userGroupCallback = userGroupCallback;
    }

    @Override
    protected RuntimeEnvironmentBuilder boostrapRuntimeEnvironmentBuilder(KModuleDeploymentUnit deploymentUnit,
            DeployedUnit deployedUnit, KieContainer kieContainer, MergeMode mode) {
        DeploymentDescriptor descriptor = deploymentUnit.getDeploymentDescriptor();
        if (descriptor == null || ((DeploymentDescriptorImpl)descriptor).isEmpty()) { // skip empty descriptors as its default can override settings
            DeploymentDescriptorManager descriptorManager = new DeploymentDescriptorManager("org.jbpm.domain");
            List<DeploymentDescriptor> descriptorHierarchy = descriptorManager.getDeploymentDescriptorHierarchy(kieContainer);

            descriptor = merger.merge(descriptorHierarchy, mode);
            deploymentUnit.setDeploymentDescriptor(descriptor);
        } else if (descriptor != null && !deploymentUnit.isDeployed()) {
            DeploymentDescriptorManager descriptorManager = new DeploymentDescriptorManager("org.jbpm.domain");
            List<DeploymentDescriptor> descriptorHierarchy = descriptorManager.getDeploymentDescriptorHierarchy(kieContainer);

            descriptorHierarchy.add(0, descriptor);
            descriptor = merger.merge(descriptorHierarchy, mode);
            deploymentUnit.setDeploymentDescriptor(descriptor);
        }

        // first set on unit the strategy
        deploymentUnit.setStrategy(descriptor.getRuntimeStrategy());

        // setting up runtime environment via builder
        RuntimeEnvironmentBuilder builder = RuntimeEnvironmentBuilder.Factory.get().newEmptyBuilder();
        
        Map<String, Object> contaxtParams = new HashMap<String, Object>();
        
        contaxtParams.put("classLoader", kieContainer.getClassLoader());
        // process object models that are globally configured (environment entries, session configuration)
        for (NamedObjectModel model : descriptor.getEnvironmentEntries()) {
            Object entry = getInstanceFromModel(model, kieContainer, contaxtParams);
            builder.addEnvironmentEntry(model.getName(), entry);
        }

        for (NamedObjectModel model : descriptor.getConfiguration()) {
            Object entry = getInstanceFromModel(model, kieContainer, contaxtParams);
            builder.addConfiguration(model.getName(), (String) entry);
        }
        ObjectMarshallingStrategy[] mStrategies = new ObjectMarshallingStrategy[descriptor.getMarshallingStrategies().size() + 1];
        int index = 0;
        for (ObjectModel model : descriptor.getMarshallingStrategies()) {
            Object strategy = getInstanceFromModel(model, kieContainer, contaxtParams);
            mStrategies[index] = (ObjectMarshallingStrategy)strategy;
            index++;
        }
        // lastly add the main default strategy
        mStrategies[index] = new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT);
        builder.addEnvironmentEntry(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES, mStrategies);

        builder.addEnvironmentEntry("KieDeploymentDescriptor", descriptor);
        builder.addEnvironmentEntry("KieContainer", kieContainer);
        
        // populate all assets with roles for this deployment unit
        List<String> requiredRoles = descriptor.getRequiredRoles(DeploymentDescriptor.TYPE_VIEW);
        if (requiredRoles != null && !requiredRoles.isEmpty()) {
            for (DeployedAsset desc : deployedUnit.getDeployedAssets()) {
                if (desc instanceof ProcessAssetDesc) {
                    ((ProcessAssetDesc) desc).setRoles(requiredRoles);
                }
            }
        }

        // Classes 3: classes added from descriptor
        List<String> remoteableClasses = descriptor.getClasses();
        if (remoteableClasses != null && !remoteableClasses.isEmpty()) {
            for (String className : remoteableClasses) {
                Class<?> descriptorClass = null;
                try {
                    descriptorClass = kieContainer.getClassLoader().loadClass(className);
                    logger.debug( "Loaded {} into the classpath from deployment descriptor {}", className, kieContainer.getReleaseId().toExternalForm());
                } catch (ClassNotFoundException cnfe) {
                    throw new IllegalArgumentException("Class " + className + " not found in the project");
                } catch (NoClassDefFoundError e) {
                    throw new IllegalArgumentException("Class " + className + " not found in the project");
                }
                addClassToDeployedUnit(descriptorClass, (DeployedUnitImpl) deployedUnit);
            }
        }

        builder.addEnvironmentEntry(MapDBEnvironmentName.DB_OBJECT, db);
        builder.userGroupCallback(userGroupCallback);
        builder.persistence(false);
        
        return builder;
    }

    @Override
    protected RegisterableItemsFactory getRegisterableItemsFactory(AuditEventBuilder auditLoggerBuilder, KieContainer kieContainer, KModuleDeploymentUnit unit) {
        if (registerableItemsFactory != null) {
            return registerableItemsFactory;
        }
        return new SimpleRegisterableItemsFactory();
    }

    @Override
    protected Object getInstanceFromModel(ObjectModel model, KieContainer kieContainer, Map<String, Object> contaxtParams) {
        contaxtParams.remove("entityManagerFactory");
        return super.getInstanceFromModel(model, kieContainer, contaxtParams);
    }
    
    
}
