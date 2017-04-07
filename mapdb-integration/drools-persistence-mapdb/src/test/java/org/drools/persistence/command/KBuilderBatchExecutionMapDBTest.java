/*
 * Copyright 2017 Red Hat Inc
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
package org.drools.persistence.command;

import java.util.Map;

import org.drools.compiler.command.KBuilderBatchExecutionTest;
import org.drools.persistence.mapdb.util.MapDBPersistenceUtil;
import org.junit.After;
import org.kie.api.KieServices;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

public class KBuilderBatchExecutionMapDBTest extends KBuilderBatchExecutionTest {

    private Map<String, Object> context;

    @After
    public void cleanUpPersistence() throws Exception {
        disposeKSession();
        MapDBPersistenceUtil.cleanUp(context);
        context = null;
    }

    protected StatefulKnowledgeSession createKnowledgeSession(KnowledgeBase kbase) {
        if( context == null ) { 
            context = MapDBPersistenceUtil.setupMapDB();
        }
        KieSessionConfiguration ksconf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        Environment env = MapDBPersistenceUtil.createEnvironment(context);
        return (StatefulKnowledgeSession) KieServices.Factory.get().getStoreServices().newKieSession(kbase, ksconf, env);
    }  
    

}
