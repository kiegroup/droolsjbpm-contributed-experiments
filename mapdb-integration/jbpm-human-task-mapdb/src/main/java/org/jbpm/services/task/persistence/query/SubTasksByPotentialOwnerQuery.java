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
package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;

public class SubTasksByPotentialOwnerQuery implements MapDBQuery<List<TaskSummary>> {

    @Override
    public List<TaskSummary> execute(UserGroupCallback callback,
            Map<String, Object> params, TaskTableService tts,
            boolean singleResult) {
        Long parentId = (Long) params.get("parentId");
        String userId = (String) params.get("userId");
        List<Status> status = Arrays.asList(Status.Created, Status.Ready, 
                Status.Reserved, Status.InProgress, Status.Suspended);
        @SuppressWarnings("unchecked")
        List<String> groupIds = (List<String>) params.get("groupIds");

        Set<Long> ids = new HashSet<>();
        MapDBQueryUtil.addAll(ids, tts.getByActualOwner(), userId);
        MapDBQueryUtil.addAll(ids, tts.getByPotentialOwner(), userId);
        for (String groupId : groupIds) {
            MapDBQueryUtil.addAll(ids, tts.getByActualOwner(), groupId);
            MapDBQueryUtil.addAll(ids, tts.getByPotentialOwner(), groupId);
        }
        MapDBQueryUtil.removeAll(ids, tts.getByExclOwner(), userId);
        
        Set<Long> idsByParent = new HashSet<>();
        MapDBQueryUtil.addAll(idsByParent, tts.getByParentId(), parentId);
        
        ids.retainAll(idsByParent);
        
        Set<Long> idsByStatus = new HashSet<>();
        if (status != null) {
            List<String> strStatus = MapDBQueryUtil.asStringStatus(status);
            for (Long value : ids) {
                String taskStatus = tts.getTaskStatusById().get(value);
                if (taskStatus != null && strStatus.contains(taskStatus)) {
                    idsByStatus.add(value);
                }
            }
            ids.retainAll(idsByStatus); //and operation
        }
        
        List<TaskSummary> retval = new ArrayList<>(ids.size());
        for (Long id : ids) {
            if (tts.getById().containsKey(id)) {
                Task task = tts.getById().get(id);
                if (task != null) {
                    retval.add(new TaskSummaryImpl(task));
                }
            }
        }
        return MapDBQueryUtil.paging(params, retval);
    }
}
