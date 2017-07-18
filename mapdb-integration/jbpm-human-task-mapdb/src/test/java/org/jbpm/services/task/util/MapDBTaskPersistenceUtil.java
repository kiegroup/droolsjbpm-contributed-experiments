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
package org.jbpm.services.task.util;

import org.drools.persistence.api.TransactionManager;
import org.drools.persistence.jta.JtaTransactionManagerFactory;
import org.drools.persistence.jta.TransactionLockInterceptor;
import org.jbpm.services.task.commands.TaskCommandExecutorImpl;
import org.jbpm.services.task.events.TaskEventSupport;
import org.jbpm.services.task.identity.DefaultUserInfo;
import org.jbpm.services.task.identity.MvelUserGroupCallbackImpl;
import org.jbpm.services.task.impl.TaskDeadlinesServiceImpl;
import org.jbpm.services.task.impl.command.CommandBasedTaskService;
import org.jbpm.services.task.mapdb.persistence.TaskTransactionInterceptor;
import org.jbpm.services.task.persistence.MapDBTaskPersistenceContextManager;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.internal.task.api.InternalTaskService;

public class MapDBTaskPersistenceUtil {

    public static InternalTaskService createTaskService(Environment env) {
        TaskEventSupport taskEventSupport = new TaskEventSupport();
        if (env.get(EnvironmentName.TASK_USER_GROUP_CALLBACK) == null) {
            env.set(EnvironmentName.TASK_USER_GROUP_CALLBACK, new MvelUserGroupCallbackImpl(true));
        }
        if (env.get(EnvironmentName.TASK_USER_INFO) == null) {
            env.set(EnvironmentName.TASK_USER_INFO, new DefaultUserInfo(true));
        }
        TransactionManager txm = new JtaTransactionManagerFactory().newTransactionManager(env);
        env.set(EnvironmentName.TRANSACTION_MANAGER, txm);
        MapDBTaskPersistenceContextManager tpcm = new MapDBTaskPersistenceContextManager(env);
        env.set(EnvironmentName.TASK_PERSISTENCE_CONTEXT_MANAGER, tpcm);
        //taskEventSupport.addEventListener(new BAMTaskEventListener(true));
        TaskCommandExecutorImpl commandExecutor = new TaskCommandExecutorImpl(env, taskEventSupport);
        TaskTransactionInterceptor interceptor = new TaskTransactionInterceptor(env);
        commandExecutor.addInterceptor(interceptor);
        commandExecutor.addInterceptor(new TransactionLockInterceptor(env));
        if (TaskDeadlinesServiceImpl.getInstance() == null) {
            TaskDeadlinesServiceImpl.initialize(commandExecutor);
        }
        return new CommandBasedTaskService(commandExecutor, taskEventSupport);
    }
    
}
