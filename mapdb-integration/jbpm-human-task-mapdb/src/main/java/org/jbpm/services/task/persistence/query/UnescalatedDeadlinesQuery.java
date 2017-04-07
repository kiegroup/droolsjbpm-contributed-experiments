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
import org.jbpm.services.task.query.DeadlineSummaryImpl;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.model.Deadline;
import org.kie.internal.task.api.model.DeadlineSummary;
import org.kie.internal.task.api.model.Deadlines;
import org.kie.internal.task.api.model.InternalTask;

public class UnescalatedDeadlinesQuery implements MapDBQuery<List<DeadlineSummary>> {

    private boolean isEndDeadlines;

    public UnescalatedDeadlinesQuery(boolean isEndDeadlines) {
        this.isEndDeadlines = isEndDeadlines;
    }

    @Override
    public List<DeadlineSummary> execute(UserGroupCallback callback,
            Map<String, Object> params, TaskTableService tts,
            boolean singleResult) {
        Set<Long> ids = new HashSet<>();
        for (long[] taskIds : tts.getByDeadlineId().values()) {
            addAll(ids, taskIds);
        }
        List<DeadlineSummary> retval = new ArrayList<>();
        for (Long taskId : ids) {
            if (tts.getById().containsKey(taskId)) {
                Task task = tts.getById().get(taskId);
                Deadlines d = ((InternalTask) task).getDeadlines();
                List<Deadline> list = isEndDeadlines ? d.getEndDeadlines() : d.getStartDeadlines();
                for(Deadline dl : list) {
                    if (!dl.isEscalated()) {
                        retval.add(new DeadlineSummaryImpl(taskId, dl.getId(), dl.getDate()));
                    }
                }
            }
        }
        return MapDBQueryUtil.paging(params, retval);
    }

    private void addAll(Set<Long> values, long[] v) {
        if (v != null) {
            for (long value : v) {
                values.add(value);
            }
        }
    }

}
