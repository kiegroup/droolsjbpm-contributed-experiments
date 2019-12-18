/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kiegroup.zenithr.drools.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class OutputFact {

    private Collection<Output> outputs;

    public OutputFact() {
        this.outputs = Collections.emptyList();
    }

    public Collection<Output> get() {
        return outputs;
    }

    public OutputFact add(Output output) {
        Collection<Output> next = new ArrayList<>(outputs);
        next.add(output);
        this.outputs = Collections.unmodifiableCollection(next);
        return this;
    }
}
