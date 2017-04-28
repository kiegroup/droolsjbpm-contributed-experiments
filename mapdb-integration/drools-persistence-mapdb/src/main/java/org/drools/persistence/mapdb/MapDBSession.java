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
package org.drools.persistence.mapdb;

import org.drools.persistence.api.PersistentSession;
import org.drools.persistence.api.SessionMarshallingHelper;
import org.kie.api.persistence.ObjectStoringStrategy;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.mapdb.DB;

public class MapDBSession implements PersistentSession, MapDBTransformable {
    
    private SessionMarshallingHelper marshallingHelper;
    private byte[] data;
    private Long id;

    @Override
    public void transform() {
        this.data = marshallingHelper.getSnapshot();
    }

    @Override
    public void setEnvironment(Environment env) {
        //do nothing
    }
    
    public KieSession getKieSession() {
        if (marshallingHelper != null) {
            return marshallingHelper.getObject();
        }
        return null;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void setMarshallingHelper(SessionMarshallingHelper marshallingHelper) {
        this.marshallingHelper = marshallingHelper;
    }

    @Override
    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String getMapKey() {
        return "session";
    }

    @Override
    public boolean updateOnMap(DB db, ObjectStoringStrategy[] strategies) {
        new SessionIndexService(db, strategies).update(this);
        return true;
    }
}
