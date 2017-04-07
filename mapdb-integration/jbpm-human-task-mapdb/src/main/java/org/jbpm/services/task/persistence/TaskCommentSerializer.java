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
import java.util.Date;

import org.jbpm.services.task.impl.model.CommentImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.kie.api.task.model.Comment;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class TaskCommentSerializer extends GroupSerializerObjectArray<Comment> {

    @Override
    public void serialize(DataOutput2 out, Comment value) throws IOException {
        out.writeLong(value.getId());
        out.writeBoolean(value.getText() != null);
        if (value.getText() != null) {
            out.writeUTF(value.getText());
        }
        out.writeBoolean(value.getAddedAt() != null);
        if (value.getAddedAt() != null) {
            out.writeLong(value.getAddedAt().getTime());
        }
        out.writeBoolean(value.getAddedBy() != null);
        if (value.getAddedBy() != null) {
            out.writeUTF(value.getAddedBy().getId());
        }
    }

    @Override
    public Comment deserialize(DataInput2 input, int available)
            throws IOException {
        CommentImpl comment = new CommentImpl();
        comment.setId(input.readLong());
        if (input.readBoolean()) {
            comment.setText(input.readUTF());
        }
        if (input.readBoolean()) {
            comment.setAddedAt(new Date(input.readLong()));
        }
        if (input.readBoolean()) {
            comment.setAddedBy(new UserImpl(input.readUTF()));
        }
        return comment;
    }

    @Override
    public int compare(Comment o1, Comment o2) {
        return o1.getId().compareTo(o2.getId());
    }

}
