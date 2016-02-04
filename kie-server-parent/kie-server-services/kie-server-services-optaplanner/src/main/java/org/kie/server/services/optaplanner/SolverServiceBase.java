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

package org.kie.server.services.optaplanner;

import org.kie.server.api.model.*;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optaplanner solver service
 */
public class SolverServiceBase {

    private static final Logger logger = LoggerFactory.getLogger( SolverServiceBase.class );

    private KieServerRegistry context;
    private Map<String, SolverInstanceContext> solvers = new ConcurrentHashMap<String, SolverInstanceContext>();

    public SolverServiceBase(KieServerRegistry context) {
        this.context = context;
    }

    public ServiceResponse<SolverInstance> createSolver(String containerId, String solverId, SolverInstance solverInstance) {
        if ( solverInstance == null || solverInstance.getSolverConfigFile() == null ) {
            logger.error( "Error creating solver. Configuration file name is null: " + solverInstance );
            return new ServiceResponse<SolverInstance>(
                    ServiceResponse.ResponseType.FAILURE, "Failed to create solver for container " + containerId +
                                                          ". Solver configuration file is null: " + solverInstance );
        }
        solverInstance.setContainerId( containerId );
        solverInstance.setSolverId( solverId );

        try {
            KieContainerInstanceImpl ci = context.getContainer( containerId );
            if ( ci == null ) {
                logger.error( "Error creating solver. Container does not exist: " + containerId );
                return new ServiceResponse<SolverInstance>( ServiceResponse.ResponseType.FAILURE, "Failed to create solver. Container does not exist: " + containerId );
            }

            // have to synchronize on the ci or a concurrent call to dispose may create inconsistencies
            synchronized ( ci ) {
                if( solvers.containsKey( solverInstance.getSolverInstanceKey() ) ) {
                    logger.error( "Error creating solver. Solver '" + solverId + "' already exists for container '" + containerId + "'." );
                    return new ServiceResponse<SolverInstance>( ServiceResponse.ResponseType.FAILURE, "Failed to create solver. Solver '" + solverId +
                                                                                                      "' already exists for container '" + containerId + "'." );
                }
                SolverInstanceContext sic = new SolverInstanceContext( solverInstance );

                try {
                    SolverFactory<?> solverFactory = SolverFactory.createFromKieContainerXmlResource( ci.getKieContainer(), solverInstance.getSolverConfigFile() );

                    Solver<?> solver = solverFactory.buildSolver();

                    sic.setSolver( solver );
                    sic.getInstance().setStatus( SolverInstance.SolverStatus.NOT_SOLVING );

                    logger.info( "Solver '" + solverId + "' successfully created in container '" + containerId + "'" );
                    return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.SUCCESS,
                                                               "Solver '" + solverId + "' successfully created in container '" + containerId + "'",
                                                               solverInstance );

                } catch( Exception e ) {
                    logger.error("Error creating solver factory for solver " + solverInstance, e);
                    return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.FAILURE,
                                                               "Error creating solver factory for solver: " + e.getMessage(),
                                                               solverInstance );
                }
            }
        } catch (Exception e) {
            logger.error("Error creating solver '" + solverId + "' in container '" + containerId + "'", e);
            return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.FAILURE,
                                                       "Error creating solver '" + solverId + "' in container '" + containerId + "': " + e.getMessage(),
                                                       solverInstance );
        }
    }

    public ServiceResponse<SolverInstance> getSolverState( String containerId, String solverId ) {
        try {
            SolverInstanceContext sic = solvers.get( SolverInstance.getSolverInstanceKey( containerId, solverId ) );
            if( sic != null ) {
                Solution bestSolution = sic.getSolver().getBestSolution();
                sic.getInstance().setScore( bestSolution != null ? bestSolution.getScore().toLevelNumbers() : null );
                return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.SUCCESS,
                                                           "Solver '" + solverId + "' state successfully retrieved from container '" + containerId + "'",
                                                           sic.getInstance() );
            } else {
                return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.FAILURE,
                                                           "Solver '" + solverId + "' not found in container '" + containerId + "'",
                                                           null );
            }
        } catch (Exception e) {
            logger.error("Error retrieving solver '" + solverId + "' state from container '" + containerId + "'", e);
            return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.FAILURE,
                                                       "Error retrieving solver '" + solverId + "' state from container '" + containerId + "'" + e.getMessage(),
                                                       null );
        }
    }

    public ServiceResponse<Solution> getBestSolution( String containerId, String solverId ) {
        try {
            SolverInstanceContext sic = solvers.get( SolverInstance.getSolverInstanceKey( containerId, solverId ) );
            if( sic != null ) {
                Solution bestSolution = sic.getSolver().getBestSolution();
                return new ServiceResponse<Solution>(ServiceResponse.ResponseType.SUCCESS,
                                                           "Best computed solution for '" + solverId + "' successfully retrieved from container '" + containerId + "'",
                                                           bestSolution );
            } else {
                return new ServiceResponse<Solution>(ServiceResponse.ResponseType.FAILURE,
                                                           "Solver '" + solverId + "' not found in container '" + containerId + "'",
                                                           null );
            }
        } catch (Exception e) {
            logger.error("Error retrieving solver '" + solverId + "' state from container '" + containerId + "'", e);
            return new ServiceResponse<Solution>(ServiceResponse.ResponseType.FAILURE,
                                                       "Error retrieving solver '" + solverId + "' state from container '" + containerId + "'" + e.getMessage(),
                                                       null );
        }
    }

    public ServiceResponse<SolverInstance> updateSolverState( String containerId, String solverId, SolverInstance instance ) {
        // requires implementation
        return new ServiceResponse<SolverInstance>(ServiceResponse.ResponseType.FAILURE,
                                                   "Unknown error updating solver state.",
                                                   null );
    }

    public ServiceResponse<Void> disposeSolver( String containerId, String solverId ) {
        try {
            SolverInstanceContext sic = solvers.remove( SolverInstance.getSolverInstanceKey( containerId, solverId ) );
            return new ServiceResponse<Void>(ServiceResponse.ResponseType.SUCCESS,
                                                       "Solver '" + solverId + "' successfully disposed from container '" + containerId + "'",
                                                       null );
        } catch (Exception e) {
            logger.error("Error disposing solver '" + solverId + "' from container '" + containerId + "'", e);
            return new ServiceResponse<Void>(ServiceResponse.ResponseType.FAILURE,
                                                       "Error disposing solver '" + solverId + "' from container '" + containerId + "'. Message: " + e.getMessage(),
                                                       null );
        }
    }

    public KieServerRegistry getKieServerRegistry() {
        return this.context;
    }
}
