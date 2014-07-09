package org.kie.server.services.impl;

import org.drools.compiler.kie.builder.impl.InternalKieContainer;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;

public class KieContainerInstance {

    private KieContainerResource resource;
    private InternalKieContainer kieContainer;

    public KieContainerInstance(String containerId, KieContainerStatus status) {
        this(containerId, status, null);
    }

    public KieContainerInstance(String containerId, KieContainerStatus status, InternalKieContainer kieContainer) {
        super();
        this.resource = new KieContainerResource(containerId, kieContainer != null ? new ReleaseId( kieContainer.getContainerReleaseId() ) : null, status);
        this.kieContainer = kieContainer;
    }

    public String getContainerId() {
        return resource.getContainerId();
    }

    public void setContainerId(String containerId) {
        this.resource.setContainerId(containerId);
    }

    public InternalKieContainer getKieContainer() {
        return kieContainer;
    }

    public void setKieContainer(InternalKieContainer kieContainer) {
        this.kieContainer = kieContainer;
        if( kieContainer != null ) {
            this.resource.setReleaseId(new ReleaseId( kieContainer.getReleaseId()));
            this.resource.setResolvedReleaseId(new ReleaseId(kieContainer.getContainerReleaseId()));
        }
    }

    public KieContainerStatus getStatus() {
        return resource.getStatus();
    }

    public void setStatus(KieContainerStatus status) {
        this.resource.setStatus(status);
    }

    public KieContainerResource getResource() {
        return resource;
    }

    public void setResource(KieContainerResource resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return resource.toString();
    }

}
