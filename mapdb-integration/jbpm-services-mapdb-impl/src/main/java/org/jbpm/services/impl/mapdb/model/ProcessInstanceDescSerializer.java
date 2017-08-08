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

package org.jbpm.services.impl.mapdb.model;

import java.io.IOException;
import java.util.Date;

import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class ProcessInstanceDescSerializer extends GroupSerializerObjectArray<MapDBProcessInstanceDesc> {

    @Override
    public MapDBProcessInstanceDesc deserialize(DataInput2 input, int available) throws IOException {
        
        Long id = input.readLong(); 
        String processId = input.readUTF(); 
        String processName = input.readUTF(); 
        String processVersion = input.readUTF();
        int state = input.readInt(); 
        String deploymentId = input.readUTF(); 
        Date dataTimeStamp = new Date(input.readLong()); 
        String initiator = input.readUTF();
        String processInstanceDescription = input.readUTF();
        String correlationKey = input.readUTF(); 
        Long parentId = input.readLong();
        
        
        MapDBProcessInstanceDesc desc = new MapDBProcessInstanceDesc(id,
                processId,
                processName,
                processVersion,
                state,
                deploymentId,
                dataTimeStamp,
                initiator,
                processInstanceDescription,
                correlationKey,
                parentId);
        
        return desc;
    }

    @Override
    public void serialize(DataOutput2 out, MapDBProcessInstanceDesc value) throws IOException {
        out.writeLong(value.getId());
        out.writeUTF(value.getProcessId());
        out.writeUTF(value.getProcessName());
        out.writeUTF(value.getProcessVersion());
        out.writeInt(value.getState().intValue());
        out.writeUTF(value.getDeploymentId());
        out.writeLong(value.getDataTimeStamp().getTime());
        out.writeUTF(value.getInitiator());
        out.writeUTF(value.getProcessInstanceDescription());
        out.writeUTF(value.getCorrelationKey());
        out.writeLong(value.getParentId());
        
    }
    
    @Override
    public int compare(MapDBProcessInstanceDesc o1, MapDBProcessInstanceDesc o2) {
        return o1.compareTo(o2);
    }

}
