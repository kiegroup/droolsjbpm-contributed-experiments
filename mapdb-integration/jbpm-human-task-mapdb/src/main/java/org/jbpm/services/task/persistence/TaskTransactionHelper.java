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
package org.jbpm.services.task.persistence;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.drools.persistence.api.TransactionManager;

public class TaskTransactionHelper {

    private static final String TSK_UPDETEABLE_RESOURCE = "task-updateable-resource";
    
    @SuppressWarnings("unchecked")
    public static void addToUpdatableSet(TransactionManager txm, MapDBElement element) {
        if (element == null) {
            return;
        }
        Set<MapDBElement> toBeUpdated = (Set<MapDBElement>) txm.getResource(TSK_UPDETEABLE_RESOURCE);
        if (toBeUpdated == null) {
            toBeUpdated = new LinkedHashSet<MapDBElement>();
            txm.putResource(TSK_UPDETEABLE_RESOURCE, toBeUpdated);
        }
        toBeUpdated.add(element);
    }

    @SuppressWarnings("unchecked")
    public static void removeFromUpdatableSet(TransactionManager txm, MapDBElement element) {
        Set<MapDBElement> toBeUpdated = (Set<MapDBElement>) txm.getResource(TSK_UPDETEABLE_RESOURCE);
        if (toBeUpdated == null) {
            return;
        }
        toBeUpdated.remove(element);
    }

    @SuppressWarnings("unchecked")
    public static Set<MapDBElement> getUpdateableSet(TransactionManager txm) {
        Set<MapDBElement> toBeUpdated = (Set<MapDBElement>) txm.getResource(TSK_UPDETEABLE_RESOURCE);
        if (toBeUpdated == null) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<MapDBElement>(toBeUpdated);
    }


}
