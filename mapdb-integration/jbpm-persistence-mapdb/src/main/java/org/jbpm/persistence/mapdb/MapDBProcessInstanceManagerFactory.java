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
package org.jbpm.persistence.mapdb;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.process.instance.ProcessInstanceManager;
import org.jbpm.process.instance.ProcessInstanceManagerFactory;

public class MapDBProcessInstanceManagerFactory implements
        ProcessInstanceManagerFactory {

    @Override
    public ProcessInstanceManager createProcessInstanceManager(
            InternalKnowledgeRuntime kruntime) {
        return new MapDBProcessInstanceManager(kruntime);
    }

}
