/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.integrationtests.optaplanner;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponse.ResponseType;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.instance.SolverInstance.SolverStatus;
import org.kie.server.client.KieServicesException;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OptaplannerIntegrationTest
        extends OptaplannerKieServerBaseIntegrationTest {
    private static final ReleaseId kjar1 = new ReleaseId("org.kie.server.testing", "cloudbalance",
            "1.0.0.Final");

    private static final String CONTAINER_1_ID = "cloudbalance";
    private static final String SOLVER_1_ID = "cloudsolver";
    private static final String SOLVER_1_CONFIG = "META-INF/cloudbalance-solver.xml";

    private static final String CLASS_CLOUD_BALANCE = "org.kie.server.testing.CloudBalance";
    private static final String CLASS_CLOUD_COMPUTER = "org.kie.server.testing.CloudComputer";
    private static final String CLASS_CLOUD_PROCESS = "org.kie.server.testing.CloudProcess";
    private static final String PERSON_CLASS_NAME = "org.kie.server.testing.Person";

    @BeforeClass
    public static void deployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/cloudbalance").getFile());
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        KieContainer kieContainer = KieServices.Factory.get().newKieContainer(kjar1);
        extraClasses.put(CLASS_CLOUD_BALANCE, Class.forName(CLASS_CLOUD_BALANCE, true, kieContainer.getClassLoader()));
        extraClasses.put(CLASS_CLOUD_COMPUTER, Class.forName(CLASS_CLOUD_COMPUTER, true, kieContainer.getClassLoader()));
        extraClasses.put(CLASS_CLOUD_PROCESS, Class.forName(CLASS_CLOUD_PROCESS, true, kieContainer.getClassLoader()));
    }

    @Test 
    public void testCreateDisposeSolver() throws Exception {
        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile( SOLVER_1_CONFIG );
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );
        assertSuccess( solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, instance ) );
        assertSuccess( solverClient.disposeSolver( CONTAINER_1_ID, SOLVER_1_ID ) );
    }

    @Test
    public void testCreateSolverFromNotExistingContainer() throws Exception {
        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile( SOLVER_1_CONFIG );
        ServiceResponse<SolverInstance> createSolverResponse = solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, instance );

        ResponseType type = createSolverResponse.getType();
        assertEquals("Expected FAILURE response, but got " + type + "!", ServiceResponse.ResponseType.FAILURE, type);
        assertResultContainsString(createSolverResponse.getMsg(), "Failed to create solver. Container does not exist");
    }

    @Test
    public void testCreateSolverWithoutSolverInstance() throws Exception {
        ServiceResponse<SolverInstance> createSolverResponse = solverClient.createSolver(CONTAINER_1_ID, SOLVER_1_ID, null);

        ResponseType type = createSolverResponse.getType();
        assertEquals("Expected FAILURE response, but got " + type + "!", ServiceResponse.ResponseType.FAILURE, type);
        assertResultContainsStringRegex(createSolverResponse.getMsg(), "Failed to create solver for container.*Solver configuration file is null.*");
    }

    @Test
    public void testCreateSolverWrongSolverInstanceConfigPath() throws Exception {
        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile("NonExistingPath");
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );
        ServiceResponse<SolverInstance> createSolverResponse = solverClient.createSolver(CONTAINER_1_ID, SOLVER_1_ID, instance);

        ResponseType type = createSolverResponse.getType();
        assertEquals("Expected FAILURE response, but got " + type + "!", ServiceResponse.ResponseType.FAILURE, type);
        assertResultContainsStringRegex(createSolverResponse.getMsg(), ".*The solverConfigResource.*does not exist as a classpath resource in the classLoader.*");
    }

    @Test
    public void testCreateSolverNullContainer() throws Exception {
        ServiceResponse<SolverInstance> createSolverResponse = solverClient.createSolver(null, SOLVER_1_ID, null);
        ResponseType type = createSolverResponse.getType();
        assertEquals("Expected FAILURE response, but got " + type + "!", ServiceResponse.ResponseType.FAILURE, type);
    }

    @Test
    public void testCreateDuplicitSolver() throws Exception {
        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile( SOLVER_1_CONFIG );
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );

        ServiceResponse<SolverInstance> createSolverResponse = solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, instance );
        assertSuccess(createSolverResponse);

        createSolverResponse = solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, instance );
        ResponseType type = createSolverResponse.getType();
        assertEquals("Expected FAILURE response, but got " + type + "!", ServiceResponse.ResponseType.FAILURE, type);
    }

    @Test(expected = KieServicesException.class)
    public void testDisposeNotExistingSolver() throws Exception {
        solverClient.disposeSolver( CONTAINER_1_ID, SOLVER_1_ID );
    }

    @Test
    public void testGetSolverState() throws Exception {
        SolverInstance instance = new SolverInstance();
        instance.setSolverConfigFile( SOLVER_1_CONFIG );
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );
        assertSuccess( solverClient.createSolver( CONTAINER_1_ID, SOLVER_1_ID, instance ) );

        ServiceResponse<SolverInstance> solverState = solverClient.getSolverState(CONTAINER_1_ID, SOLVER_1_ID);
        assertSuccess(solverState);

        SolverInstance returnedInstance = solverState.getResult();
        assertEquals(CONTAINER_1_ID, returnedInstance.getContainerId());
        assertEquals(SOLVER_1_CONFIG, returnedInstance.getSolverConfigFile());
        assertEquals(SOLVER_1_ID, returnedInstance.getSolverId());
        assertEquals(SolverInstance.getSolverInstanceKey(CONTAINER_1_ID, SOLVER_1_ID), returnedInstance.getSolverInstanceKey());
        assertEquals(SolverStatus.NOT_SOLVING, returnedInstance.getStatus());
        assertNull(returnedInstance.getScore());
    }

    @Test
    public void testGetNotExistingSolverState() throws Exception {
        assertSuccess( client.createContainer( CONTAINER_1_ID, new KieContainerResource( CONTAINER_1_ID, kjar1 ) ) );

        try {
            solverClient.getSolverState(CONTAINER_1_ID, SOLVER_1_ID);
        } catch (KieServicesException e) {
            assertResultContainsStringRegex(e.getMessage(), ".*Solver.*not found in container.*");
        }
    }
}
