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

import org.jbpm.services.task.impl.model.GroupImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.User;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class OrganizationalEntitySerializer extends
        GroupSerializerObjectArray<OrganizationalEntity> {

    @Override
    public void serialize(DataOutput2 out, OrganizationalEntity value)
            throws IOException {
        out.writeBoolean(value instanceof User);
        out.writeUTF(value.getId());
    }

    @Override
    public OrganizationalEntity deserialize(DataInput2 input, int available) throws IOException {
        OrganizationalEntity entity = null;
        if (input.readBoolean()) {
            entity = new UserImpl(input.readUTF());
        } else {
            entity = new GroupImpl(input.readUTF());
        }
        return entity;
    }

    @Override
    public int compare(OrganizationalEntity o1, OrganizationalEntity o2) {
        return o1.getId().compareTo(o2.getId());
    }

}
