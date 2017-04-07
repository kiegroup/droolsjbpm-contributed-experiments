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

import java.io.IOException;
import java.util.Map;

import org.kie.internal.task.api.model.Deadline;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class TaskDeadlineSerializer extends GroupSerializerObjectArray<Deadline> {

    @Override
    public void serialize(DataOutput2 out, Deadline value) throws IOException {
        TaskSerializer.writeDeadline("deadline", out, value);
    }

    @Override
    public Deadline deserialize(DataInput2 input, int available) throws IOException {
        Map<String, Object> map = TaskSerializer.readFully(input);
        return TaskSerializer.readDeadline("deadline", map);
    }

    @Override
    public int compare(Deadline o1, Deadline o2) {
        return Long.valueOf(o1.getId()).compareTo(o2.getId());
    }

}
