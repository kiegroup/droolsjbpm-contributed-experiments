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

import org.jbpm.runtime.manager.impl.error.ExecutionErrorHandlerInterceptor;
import org.jbpm.services.task.commands.TaskCommandExecutorImpl;
import org.jbpm.services.task.events.TaskEventSupport;
import org.jbpm.services.task.impl.command.CommandBasedTaskService;
import org.jbpm.services.task.mapdb.persistence.TaskTransactionInterceptor;
import org.jbpm.services.task.persistence.MapDBTaskPersistenceContextManager;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.task.TaskService;
import org.kie.internal.runtime.manager.TaskServiceFactory;


public class MapDBTaskServiceFactory implements TaskServiceFactory {

    private RuntimeEnvironment environment;
    
    public MapDBTaskServiceFactory(RuntimeEnvironment environment) {
        this.environment = environment;
    }
    
    @Override
    public TaskService newTaskService() {
        Environment env = environment.getEnvironment();
        
        TaskEventSupport taskEventSupport = new TaskEventSupport();
        MapDBTaskPersistenceContextManager tpcm = new MapDBTaskPersistenceContextManager(env);
        env.set(EnvironmentName.TASK_PERSISTENCE_CONTEXT_MANAGER, tpcm);
        env.set(EnvironmentName.TASK_USER_GROUP_CALLBACK, environment.getUserGroupCallback());
        TaskCommandExecutorImpl commandExecutor = new TaskCommandExecutorImpl(env, taskEventSupport);
        commandExecutor.addInterceptor(new TaskTransactionInterceptor(env));
        commandExecutor.addInterceptor(new ExecutionErrorHandlerInterceptor(env));
        CommandBasedTaskService taskService = new CommandBasedTaskService(commandExecutor, taskEventSupport);
        return taskService;

    }

    @Override
    public void close() {
        // no-op

    }

}
