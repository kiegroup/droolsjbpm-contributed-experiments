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
import java.util.Set;

import org.kie.api.task.model.Status;

public class MapDBQueryUtil {

    public static <T> void addAll(Set<Long> values, Map<T, long[]> map, T id) {
        if (id == null) {
            return;
        }
        if (map.containsKey(id)) {
            long[] v = map.get(id);
            if (v != null) {
                for (long value : v) {
                    values.add(value);
                }
            }
        }
    }
    
    public static <T> void removeAll(Set<Long> values, Map<T, long[]> map, T id) {
        if (id == null) {
            return;
        }
        if (map.containsKey(id)) {
            long[] v = map.get(id);
            if (v != null) {
                for (long value : v) {
                    if (values.contains(value)) {
                        values.remove(value);
                    }
                }
            }
        }
    }
    
    public static <T> List<T> paging(Map<String, Object> params, List<T> fullList) {
        if (params == null) {
            return fullList;
        }
        Number maxResults = (Number) params.get("maxResults");
        Number firstResult = (Number) params.get("firstResult");
        if (firstResult == null) {
            firstResult = 0;
        }
        if (maxResults == null) {
            maxResults = fullList.size() - firstResult.intValue();
        }
        if (maxResults.intValue() > fullList.size()) {
            maxResults = fullList.size();
        }
        return fullList.subList(firstResult.intValue(), maxResults.intValue());
    }

    public static List<String> asStringStatus(List<Status> status) {
        List<String> retval = new ArrayList<>(status.size());
        for (Status s : status) {
            retval.add(s.name());
        }
        return retval;
    }
}
