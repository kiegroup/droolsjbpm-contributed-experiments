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

package org.kiegroup.zenithr.drools.service.impl;

import java.util.List;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kiegroup.zenithr.drools.service.Session;

public class ZenithrSession implements Session {

    private final KieSession kieSession;
    private final Object outputInstance;

    public ZenithrSession(KieContainer kieContainer, Object outputInstance) {
        this.kieSession = kieContainer.newKieSession();
        this.outputInstance = outputInstance;
        this.kieSession.insert(outputInstance);
    }

    @Override
    public void addInput(List<Object> input) {
        if (input != null) {
            input.stream().forEach(i -> kieSession.insert(i));
        }
    }

    @Override
    public Object process() {
        kieSession.fireAllRules();
        return outputInstance;
    }

    @Override
    public void close() throws Exception {
        if (kieSession != null) {
            kieSession.dispose();
        }
    }

}
