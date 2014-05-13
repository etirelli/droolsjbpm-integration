package org.kie.server.impl;

import javax.xml.bind.annotation.XmlTransient;

import org.drools.compiler.kie.builder.impl.InternalKieContainer;
import org.kie.api.runtime.KieContainer;
import org.kie.server.api.KieContainerInfo;

public class KieContainerInfoImpl implements KieContainerInfo {

    private String       containerId;
    private Status       status;
    @XmlTransient
    private InternalKieContainer kieContainer;

    public KieContainerInfoImpl() {
        super();
    }

    public KieContainerInfoImpl(String containerId, KieContainerInfo.Status status) {
        this( containerId, status, null);
    }
    
    public KieContainerInfoImpl(String containerId, KieContainerInfo.Status status, InternalKieContainer kieContainer) {
        super();
        this.containerId = containerId;
        this.status = status;
        this.kieContainer = kieContainer;
    }

    /* (non-Javadoc)
     * @see org.kie.server.api.KieContainerInfo#getContainerId()
     */
    @Override
    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    @XmlTransient
    public InternalKieContainer getKieContainer() {
        return kieContainer;
    }

    public void setKieContainer(InternalKieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    /* (non-Javadoc)
     * @see org.kie.server.api.KieContainerInfo#getStatus()
     */
    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((containerId == null) ? 0 : containerId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KieContainerInfoImpl other = (KieContainerInfoImpl) obj;
        if (containerId == null) {
            if (other.containerId != null)
                return false;
        } else if (!containerId.equals(other.containerId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ContainerInfo [containerId=" + containerId + ", status=" + status + "]";
    }

}
