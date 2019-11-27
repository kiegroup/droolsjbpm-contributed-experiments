/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.prediction.naivebayes;

import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.test.services.AbstractKieServicesTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryFilter;

import java.util.*;
import java.util.logging.Logger;

import static org.junit.Assert.assertNotNull;

public abstract class NaiveBayesRecommendationServiceProcessTest extends AbstractKieServicesTest {

    private Logger LOGGER = Logger.getLogger(NaiveBayesRecommendationServiceProcessTest.class.getName());

    private List<Long> instances = new ArrayList<>();

    @BeforeClass
    public static void setupOnce() {
        System.setProperty("org.jbpm.task.prediction.service", SmileNaiveBayes.IDENTIFIER);
    }

    @AfterClass
    public static void cleanOnce() {
        System.clearProperty("org.jbpm.task.prediction.service");
    }

    @Override
    protected List<String> getProcessDefinitionFiles() {
        List<String> processes = new ArrayList<String>();
        processes.add("BPMN2-UserTask.bpmn2");
        return processes;
    }

    @Override
    public DeploymentUnit prepareDeploymentUnit() throws Exception {
        // specify GROUP_ID, ARTIFACT_ID, VERSION of your kjar
        return createAndDeployUnit("org.jbpm.test.prediction", "naive-bayes-test", "1.0.0");
    }


    protected Map<String, Object> startAndReturnTaskOutputData(String item, String userId, Integer level, Boolean approved) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("item", item);
        parameters.put("level", level);
        parameters.put("actor", userId);
        long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "UserTask", parameters);
        instances.add(processInstanceId);

        List<TaskSummary> tasks = runtimeDataService.getTasksByStatusByProcessInstanceId(processInstanceId, null, new QueryFilter());
        assertNotNull(tasks);

        if (!tasks.isEmpty()) {

            Long taskId = tasks.get(0).getId();

            Map<String, Object> outputs = userTaskService.getTaskOutputContentByTaskId(taskId);
            assertNotNull(outputs);

            userTaskService.completeAutoProgress(taskId, userId, Collections.singletonMap("approved", approved));

            return outputs;
        }

        return new HashMap<>();
    }
}