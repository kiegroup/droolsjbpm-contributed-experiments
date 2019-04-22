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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kiegroup.zenithr.drools.service.RuleService;
import org.kiegroup.zenithr.drools.service.Session;
import org.kiegroup.zenithr.drools.service.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class RuleServiceImpl implements RuleService {

    private static final Logger logger = LoggerFactory.getLogger(RuleServiceImpl.class);

    @Inject
    SessionFactory sessionFactory;

    public Object process(Map<String, String> parameters) throws Exception {
        try (Session session = sessionFactory.getSession()) {
            List<Object> input = buildInput(parameters);
            session.addInput(input);
            return session.process();
        }
    }

    private List<Object> buildInput(Map<String, String> parameters) {
        List<Object> input = new ArrayList<>();
        parameters.forEach((name, value) -> {
            try {
                input.add(sessionFactory.getInputValue(name, value));
            } catch (Exception e) {
                logger.error("Unable to parse input: " + name, e);
            }
        });
        return input;
    }

}
