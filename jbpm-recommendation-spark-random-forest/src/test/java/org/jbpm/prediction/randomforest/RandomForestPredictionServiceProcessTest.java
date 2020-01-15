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

package org.jbpm.prediction.randomforest;

import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.test.services.AbstractKieServicesTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryFilter;

import java.util.*;

import static org.junit.Assert.*;

public class RandomForestPredictionServiceProcessTest extends AbstractKieServicesTest {

    private List<Long> instances = new ArrayList<>();

    @BeforeClass
    public static void setupOnce() {
        System.setProperty("org.jbpm.task.prediction.service", SparkRandomForest.IDENTIFIER);
    }

    @AfterClass
    public static void cleanOnce() {
        System.clearProperty("org.jbpm.task.prediction.service");
    }

    @Override
    protected List<String> getProcessDefinitionFiles() {
        List<String> processes = new ArrayList<>();
        processes.add("BPMN2-UserTask.bpmn2");
        return processes;
    }

    @Override
    public DeploymentUnit prepareDeploymentUnit() throws Exception {
        // specify GROUP_ID, ARTIFACT_ID, VERSION of your kjar
        return createAndDeployUnit("org.jbpm.test.prediction", "random-forest-test", "1.0.0");
    }


    /**
     * For this test insert a quantity of true training samples
     * to verify the random forest class/process is functional.
     * Expect confidence > 90.0 and "approved" to be true.
     */
    @Test
    public void testRepeatedRandomForestPredictionService() {
        System.setProperty("org.jbpm.task.prediction.service.confidence_threshold", "1.0");

        Map<String, Object> outputs;
        outputs = startAndReturnTaskOutputData("test item", "john", 5, false);
        outputs = startAndReturnTaskOutputData("test item", "mary", 5, false);
        for (int i = 0; i < 20; i++) {
            outputs = startAndReturnTaskOutputData("test item", "john", 5, true);
        }
        assertTrue((double) outputs.get("confidence") > 0.9);
        assertEquals(true, outputs.get("approved"));
    }

    /**
     * Insert an equal number of true and false samples, making
     * sure the total number of samples is larger than the dataset
     * size threshold. Since the dataset size
     * threshold will have been met and the probability of true
     * and false will be nearly equal, we expect confidence to be
     * lower than 0.55 (55%).
     */
    @Test
    public void testEqualProbabilityRandomForestPredictionService() {
        System.setProperty("org.jbpm.task.prediction.service.confidence_threshold", "1.0");

        Map<String, Object> outputs = new HashMap<>();

        startAndReturnTaskOutputData("test item", "mary", 5, false);
        for (int i = 0; i < 120; i++) {
            startAndReturnTaskOutputData("test item", "john", 5, false);
            outputs = startAndReturnTaskOutputData("test item", "john", 5, true);
        }

        final double confidence = (double) outputs.get("confidence");
        assertTrue(confidence < 0.55 && confidence > 0.45);
    }

    /**
     * Insert a disproportionate partitioning of true and false samples
     * of a sample set larger than the dataset size threshold. In this
     * case true will have higher probability and as such we expect
     * confidence to be high.
     */
    @Test
    public void testAutocompletion() {

        System.setProperty("org.jbpm.task.prediction.service.confidence_threshold", "0.85");
        Map<String, Object> outputs = new HashMap<>();
        startAndReturnTaskOutputData("item6", "john", 5, false);
        for (int i = 0; i < 200; i++) {
            startAndReturnTaskOutputData("item6", "john", 5, false);
            for (int j = 0 ; j < 6 ; j++) {
                outputs = startAndReturnTaskOutputData("item6", "john", 5, true);
            }
        }
        assertTrue(outputs.isEmpty());
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
