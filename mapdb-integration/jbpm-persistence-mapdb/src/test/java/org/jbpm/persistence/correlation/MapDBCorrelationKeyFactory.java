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
package org.jbpm.persistence.correlation;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.persistence.mapdb.MapDBCorrelationKey;
import org.jbpm.persistence.mapdb.MapDBCorrelationProperty;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.internal.process.CorrelationProperty;

public class MapDBCorrelationKeyFactory implements CorrelationKeyFactory {

    @Override
    public CorrelationKey newCorrelationKey(String businessKey) {
        if (businessKey.isEmpty()) {
            throw new IllegalArgumentException("businessKey cannot be empty");
        }

        MapDBCorrelationKey correlationKey = new MapDBCorrelationKey();
        correlationKey.setName(businessKey);
        MapDBCorrelationProperty prop = new MapDBCorrelationProperty();
        prop.setValue(businessKey);
        correlationKey.setProperties(new ArrayList<CorrelationProperty<?>>());
        correlationKey.getProperties().add(prop);
        return correlationKey;
    }

    public CorrelationKey newCorrelationKey(List<String> properties) {
        if (properties.isEmpty()) {
            throw new IllegalArgumentException("properties cannot be empty");
        }

        MapDBCorrelationKey correlationKey = new MapDBCorrelationKey();
        List<CorrelationProperty<?>> props = new ArrayList<>();
        for (String businessKey : properties) {
            MapDBCorrelationProperty prop = new MapDBCorrelationProperty();
            prop.setValue(businessKey);
            props.add(prop);
        }
        correlationKey.setProperties(props);
        return correlationKey;
    }
}
