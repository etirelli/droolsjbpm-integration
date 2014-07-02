package org.kie.server.api.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="kie-container-info")
public class KieContainerInfo {
    
    public enum Status {
        STARTED, RUNNING, FAILED
    }

    private String       containerId;
    private Status       status;

    public KieContainerInfo() {
    }

    public KieContainerInfo(String containerId, KieContainerInfo.Status status) {
        this.containerId = containerId;
        this.status = status;
    }

    @XmlAttribute(name="container-id")
    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    @XmlAttribute(name="status")
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
        result = prime * result + ((status == null) ? 0 : status.hashCode());
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
        KieContainerInfo other = (KieContainerInfo) obj;
        if (containerId == null) {
            if (other.containerId != null)
                return false;
        } else if (!containerId.equals(other.containerId))
            return false;
        if (status != other.status)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ContainerInfo [containerId=" + containerId + ", status=" + status + "]";
    }
}