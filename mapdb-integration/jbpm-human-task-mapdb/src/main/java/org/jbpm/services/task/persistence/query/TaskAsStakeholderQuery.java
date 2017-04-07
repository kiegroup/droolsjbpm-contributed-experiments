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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;

public class TaskAsStakeholderQuery implements MapDBQuery<List<TaskSummary>> {

    @Override
    public List<TaskSummary> execute(UserGroupCallback callback, 
            Map<String, Object> params, TaskTableService tts, boolean singleResult) {
        String userId = (String) params.get("userId");
        Set<Long> values = new HashSet<>();
        MapDBQueryUtil.addAll(values, tts.getByStakeholder(), userId);
        
        final List<TaskSummary> retval = new ArrayList<TaskSummary>();
        for (Long taskId : values) {
            if (tts.getById().containsKey(taskId)) {
                Task task = tts.getById().get(taskId);
                if (task != null) {
                    retval.add(new TaskSummaryImpl(task));
                }
            }
        }
        return MapDBQueryUtil.paging(params, retval);
    }
}
