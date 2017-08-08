/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.runtime.manager.mapdb.model;

import java.io.IOException;

import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class ContextMappingInfoSerializer extends GroupSerializerObjectArray<MapDBContextMappingInfo> {

    @Override
    public MapDBContextMappingInfo deserialize(DataInput2 input, int available) throws IOException {
        MapDBContextMappingInfo mapping = new MapDBContextMappingInfo();
        
        mapping.setKsessionId(input.readLong());
        mapping.setContextId(input.readUTF());
        mapping.setOwnerId(input.readUTF());
        
        return mapping;
    }

    @Override
    public void serialize(DataOutput2 out, MapDBContextMappingInfo mapping) throws IOException {
        out.writeLong(mapping.getKsessionId());
        out.writeUTF(mapping.getContextId());
        out.writeUTF(mapping.getOwnerId());
    }

    @Override
    public int compare(MapDBContextMappingInfo o1, MapDBContextMappingInfo o2) {
        
        return o1.compareTo(o2);
    }

}
