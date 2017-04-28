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
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Task;

public class AttachmentsByTaskIdQuery implements MapDBQuery<List<Attachment>> {

    @Override
    public List<Attachment> execute(UserGroupCallback callback,
            Map<String, Object> params, TaskTableService tts,
            boolean singleResult) {
        Long taskId = (Long) params.get("taskId");
        if (tts.getById().containsKey(taskId)) {
            Task task = tts.getById().get(taskId);
            return MapDBQueryUtil.paging(params, task.getTaskData().getAttachments());
        }
        return new ArrayList<>();
    }
}
