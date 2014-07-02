package org.kie.server.api.commands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ReleaseId;

@XmlRootElement(name="create-container")
@XmlAccessorType(XmlAccessType.NONE)
public class CreateContainerCommand implements KieServerCommand {
    private static final long serialVersionUID = -1803374525440238478L;
    
    @XmlAttribute(name="container-id")
    private String    containerId;
    @XmlElement(name="release-id")
    private ReleaseId releaseId;
    
    public CreateContainerCommand() {
        super();
    }
    
    public CreateContainerCommand(String containerId, ReleaseId releaseId) {
        this.containerId = containerId;
        this.releaseId = releaseId;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public ReleaseId getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    @Override
    public String toString() {
        return "CreateContainerCommand [containerId=" + containerId + ", releaseId=" + releaseId + "]";
    }

}
