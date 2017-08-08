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
