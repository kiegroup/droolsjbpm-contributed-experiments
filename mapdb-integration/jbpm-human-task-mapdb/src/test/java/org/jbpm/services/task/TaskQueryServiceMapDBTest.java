/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.services.task;

import java.util.Map;

import javax.naming.InitialContext;

import org.jbpm.persistence.mapdb.util.MapDBProcessPersistenceUtil;
import org.jbpm.services.task.util.MapDBTaskPersistenceUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.kie.api.runtime.Environment;

import com.arjuna.ats.jta.TransactionManager;


public class TaskQueryServiceMapDBTest extends TaskQueryServiceBaseTest {

    private Map<String, Object> context;

    @BeforeClass
    public static void configureTx() {
        try {
            InitialContext initContext = new InitialContext();

            initContext.rebind("java:comp/UserTransaction", com.arjuna.ats.jta.UserTransaction.userTransaction());
            initContext.rebind("java:comp/TransactionManager", TransactionManager.transactionManager());
            initContext.rebind("java:comp/TransactionSynchronizationRegistry", new com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    @Before
    public void setup() {
        context = MapDBProcessPersistenceUtil.setupMapDB();
        Environment env = MapDBProcessPersistenceUtil.createEnvironment(context);
        this.taskService = MapDBTaskPersistenceUtil.createTaskService(env);
    }
    
    @After
    public void clean() {
        super.tearDown();
        MapDBProcessPersistenceUtil.cleanUp(context);
    }
    
    @Override
    public void testGetTasksByStatusByProcessIdByTaskName() {
        //do nothing because Query elements are not created
    }
}
