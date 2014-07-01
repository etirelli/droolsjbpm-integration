package org.kie.server.api.commands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.entity.KieServerCommand;

@XmlRootElement(name = "call-container")
@XmlAccessorType(XmlAccessType.NONE)
public class CallContainerCommand implements KieServerCommand {

    private static final long   serialVersionUID = -1803374525440238478L;

    @XmlAttribute(name = "container-id")
    private String              containerId;

    @XmlElement(name = "payload")
    private String              payload;

    public CallContainerCommand() {
        super();
    }

    public CallContainerCommand(String containerId, String cmdPayload) {
        this.containerId = containerId;
        this.payload = cmdPayload;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "CallContainerCommand [containerId=" + containerId + ", payload=" + payload + "]";
    }

}
