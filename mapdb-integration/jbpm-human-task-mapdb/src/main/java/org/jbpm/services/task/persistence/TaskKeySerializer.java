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

import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class TaskKeySerializer extends GroupSerializerObjectArray<TaskKey> {

    @Override
    public void serialize(DataOutput2 out, TaskKey value) throws IOException {
        out.writeBoolean(value.getTaskId() != null);
        if (value.getTaskId() != null) {
            out.writeLong(value.getTaskId());
        }
        out.writeBoolean(value.getAttachmentId() != null);
        if (value.getAttachmentId() != null) {
            out.writeLong(value.getAttachmentId());
        }
        out.writeBoolean(value.getCommentId() != null);
        if (value.getCommentId() != null) {
            out.writeLong(value.getCommentId());
        }
        out.writeBoolean(value.getContentId() != null);
        if (value.getContentId() != null) {
            out.writeLong(value.getContentId());
        }
        out.writeBoolean(value.getDeadlineId() != null);
        if (value.getDeadlineId() != null) {
            out.writeLong(value.getDeadlineId());
        }
    }

    @Override
    public TaskKey deserialize(DataInput2 input, int available) throws IOException {
        Long taskId = null, attachmentId = null, contentId = null, commentId = null, deadlineId = null;
        if (input.readBoolean()) {
            taskId = input.readLong();
        }
        if (input.readBoolean()) {
            attachmentId = input.readLong();
        }
        if (input.readBoolean()) {
            commentId = input.readLong();
        }
        if (input.readBoolean()) {
            contentId = input.readLong();
        }
        if (input.readBoolean()) {
            deadlineId = input.readLong();
        }
        return new TaskKey(taskId, contentId, attachmentId, commentId, deadlineId);
    }

    /*@Override
    public int compare(TaskKey o1, TaskKey o2) {
        return o1.compareTo(o2);
    }*/

    @Override
    public TaskKey nextValue(TaskKey value) {
        return new TaskKey(value.getTaskId());
    }
}
