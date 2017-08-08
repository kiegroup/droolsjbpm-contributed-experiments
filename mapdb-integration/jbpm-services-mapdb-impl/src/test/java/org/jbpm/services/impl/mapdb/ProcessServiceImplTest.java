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

package org.jbpm.services.impl.mapdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.kie.scanner.KieMavenRepository.getKieMavenRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.impl.mapdb.util.AbstractKieServicesBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryFilter;
import org.kie.scanner.KieMavenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessServiceImplTest extends AbstractKieServicesBaseTest {
    private static final Logger logger = LoggerFactory.getLogger(ProcessServiceImplTest.class);

    private List<DeploymentUnit> units = new ArrayList<DeploymentUnit>();

    @Before
    public void prepare() {
        configureServices();
        logger.debug("Preparing kjar");
        KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        List<String> processes = new ArrayList<String>();
        processes.add("assets/BPMN2-ScriptTask.bpmn2");
        processes.add("assets/BPMN2-UserTask.bpmn2");

        InternalKieModule kJar1 = createKieJar(ks, releaseId, processes);
        File pom = new File("target/kmodule", "pom.xml");
        pom.getParentFile().mkdir();
        try {
            FileOutputStream fs = new FileOutputStream(pom);
            fs.write(getPom(releaseId).getBytes());
            fs.close();
        } catch (Exception e) {

        }
        KieMavenRepository repository = getKieMavenRepository();
        repository.deployArtifact(releaseId, kJar1, pom);
    }

    @After
    public void cleanup() {
        cleanupSingletonSessionId();
        if (units != null && !units.isEmpty()) {
            for (DeploymentUnit unit : units) {
                try {
                deploymentService.undeploy(unit);
                } catch (Exception e) {
                    e.printStackTrace();
                    // do nothing in case of some failed tests to avoid next test to fail as well
                }
            }
            units.clear();
        }
        close();
    }

    @Test
    public void testStartProcess() {
        assertNotNull(deploymentService);

        KModuleDeploymentUnit deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);

        deploymentService.deploy(deploymentUnit);
        units.add(deploymentUnit);
        assertNotNull(processService);

        long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "ScriptTask");
        assertNotNull(processInstanceId);

        ProcessInstance pi = processService.getProcessInstance(processInstanceId);
        assertNull(pi);
    }
    
    @Test
    public void testStartAndAbortProcess() {
        assertNotNull(deploymentService);

        KModuleDeploymentUnit deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);

        deploymentService.deploy(deploymentUnit);
        units.add(deploymentUnit);
        assertNotNull(processService);

        boolean isDeployed = deploymentService.isDeployed(deploymentUnit.getIdentifier());
        assertTrue(isDeployed);

        long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "UserTask");
        assertNotNull(processInstanceId);

        ProcessInstance pi = processService.getProcessInstance(processInstanceId);
        assertNotNull(pi);

        processService.abortProcessInstance(processInstanceId);

        pi = processService.getProcessInstance(processInstanceId);
        assertNull(pi);
    }
    
    @Test
    public void testStartAndCompleteProcessWithUserTask() {
        assertNotNull(deploymentService);

        KModuleDeploymentUnit deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);

        deploymentService.deploy(deploymentUnit);
        units.add(deploymentUnit);
        assertNotNull(processService);

        boolean isDeployed = deploymentService.isDeployed(deploymentUnit.getIdentifier());
        assertTrue(isDeployed);

        long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "UserTask");
        assertNotNull(processInstanceId);

        ProcessInstance pi = processService.getProcessInstance(processInstanceId);
        assertNotNull(pi);
        
        List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        
        userTaskService.completeAutoProgress(tasks.get(0).getId(), "john", null);

        pi = processService.getProcessInstance(processInstanceId);
        assertNull(pi);
    }
}
