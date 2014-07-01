package org.kie.server.api.entity;


public interface KieContainerInfo {
    
    public enum Status {
        STARTED, RUNNING, FAILED
    }

    public abstract String getContainerId();

    public abstract Status getStatus();

}