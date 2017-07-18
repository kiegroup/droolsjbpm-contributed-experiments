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
        
        return 0;
    }

}
