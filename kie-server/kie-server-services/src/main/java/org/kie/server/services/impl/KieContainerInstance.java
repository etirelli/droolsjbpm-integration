package org.kie.server.services.impl;

import org.drools.compiler.kie.builder.impl.InternalKieContainer;
import org.kie.server.api.model.KieContainerInfo;

public class KieContainerInstance {

    private KieContainerInfo     info;
    private InternalKieContainer kieContainer;

    public KieContainerInstance(String containerId, KieContainerInfo.Status status) {
        this(containerId, status, null);
    }

    public KieContainerInstance(String containerId, KieContainerInfo.Status status, InternalKieContainer kieContainer) {
        super();
        this.info = new KieContainerInfo(containerId, status);
        this.kieContainer = kieContainer;
    }

    public String getContainerId() {
        return info.getContainerId();
    }

    public void setContainerId(String containerId) {
        this.info.setContainerId(containerId);
    }

    public InternalKieContainer getKieContainer() {
        return kieContainer;
    }

    public void setKieContainer(InternalKieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public KieContainerInfo.Status getStatus() {
        return info.getStatus();
    }

    public void setStatus(KieContainerInfo.Status status) {
        this.info.setStatus(status);
    }

    public KieContainerInfo getInfo() {
        return info;
    }

    public void setInfo(KieContainerInfo info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return info.toString();
    }

}
