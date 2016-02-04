package org.kie.server.services.optaplanner;

import org.kie.server.api.model.instance.SolverInstance;
import org.optaplanner.core.api.solver.Solver;

/**
 * Aggregates solver instance context information
 */
public class SolverInstanceContext {

    private SolverInstance instance;
    private Solver solver;

    public SolverInstanceContext() {
    }

    public SolverInstanceContext(SolverInstance instance) {
        this.instance = instance;
    }

    public SolverInstance getInstance() {
        return instance;
    }

    public void setInstance(SolverInstance instance) {
        this.instance = instance;
    }

    public Solver getSolver() {
        return solver;
    }

    public void setSolver(Solver solver) {
        this.solver = solver;
    }

}
