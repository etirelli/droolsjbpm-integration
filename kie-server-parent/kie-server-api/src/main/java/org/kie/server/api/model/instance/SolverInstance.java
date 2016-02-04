package org.kie.server.api.model.instance;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "solver-instance")
public class SolverInstance {

    public static enum SolverStatus {
        NOT_SOLVING, SOLVING
    }

    @XmlElement(name="container-id")
    private String containerId;

    @XmlElement(name = "solver-id")
    private String solverId;

    @XmlElement(name = "solver-config-file")
    private String solverConfigFile;

    @XmlElement(name = "status")
    private SolverStatus status;

    @XmlElement(name = "score")
    private Number[] score;

    public SolverInstance() {
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getSolverId() {
        return solverId;
    }

    public void setSolverId(String solverId) {
        this.solverId = solverId;
    }

    public String getSolverConfigFile() {
        return solverConfigFile;
    }

    public void setSolverConfigFile(String solverConfigFile) {
        this.solverConfigFile = solverConfigFile;
    }

    public SolverStatus getStatus() {
        return status;
    }

    public void setStatus(SolverStatus status) {
        this.status = status;
    }

    public Number[] getScore() {
        return score;
    }

    public void setScore(Number[] score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "SolverInstance{" +
               "containerId='" + containerId + '\'' +
               ", solverId='" + solverId + '\'' +
               ", solverConfigFile='" + solverConfigFile + '\'' +
               ", status=" + status +
               ", score=" + score +
               '}';
    }

    public String getSolverInstanceKey() {
        return getSolverInstanceKey( this.containerId, this.solverId );
    }

    public static String getSolverInstanceKey( String containerId, String solverId ) {
        return containerId + "/" + solverId;
    }

}
