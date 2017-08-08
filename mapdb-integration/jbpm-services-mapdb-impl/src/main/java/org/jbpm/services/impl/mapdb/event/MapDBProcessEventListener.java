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

package org.jbpm.services.impl.mapdb.event;

import java.util.Date;

import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.services.impl.mapdb.model.MapDBProcessInstanceDesc;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.process.CorrelationKey;
import org.mapdb.BTreeMap;


public class MapDBProcessEventListener extends DefaultProcessEventListener {

    private final BTreeMap<Long, MapDBProcessInstanceDesc> processInstancesById;
    private IdentityProvider identityProvider;
    
    public MapDBProcessEventListener(BTreeMap<Long, MapDBProcessInstanceDesc> processInstancesById, IdentityProvider identityProvider) {
        this.processInstancesById = processInstancesById;
        this.identityProvider = identityProvider;
    }
    
    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        ProcessInstance pi = event.getProcessInstance();
        
        MapDBProcessInstanceDesc desc = processInstancesById.get(pi.getId());
        desc.setState(pi.getState());
        
        processInstancesById.put(pi.getId(), desc);
    }

    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        ProcessInstanceImpl pi = (ProcessInstanceImpl) event.getProcessInstance();
        
        // store correlation key in its external form
        String ckey = "";
        CorrelationKey correlationKey = (CorrelationKey) pi.getMetaData().get("CorrelationKey");
        if (correlationKey != null) {
            ckey = correlationKey.toExternalForm();
        }
        long parentProcessInstanceId = -1;
        try {
            parentProcessInstanceId = (Long) pi.getMetaData().get("ParentProcessInstanceId");

        } catch (Exception e) {           
        }
        
        MapDBProcessInstanceDesc desc = new MapDBProcessInstanceDesc(pi.getId(),
                pi.getProcessId(),
                pi.getProcessName(),
                pi.getProcess().getVersion(),
                ProcessInstance.STATE_ACTIVE,
                (String) event.getKieRuntime().getEnvironment().get("deploymentId"),
                new Date(),
                identityProvider.getName(),
                pi.getDescription(),
                ckey,
                parentProcessInstanceId);
        
        processInstancesById.put(pi.getId(), desc);
    }

}
