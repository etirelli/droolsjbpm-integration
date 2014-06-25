package org.kie.server.services.api.command.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.drools.compiler.kie.builder.impl.InternalKieContainer;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.server.services.api.KieContainerInfo;
import org.kie.server.services.api.command.KieServerCommand;
import org.kie.server.services.api.command.KieServerCommandContext;
import org.kie.server.services.api.command.ServiceResponse;
import org.kie.server.services.impl.KieContainerInfoImpl;

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

    @Override
    public ServiceResponse execute(KieServerCommandContext context) {
        try {
            if( ! context.getContainers().containsKey(containerId) ) {
                KieServices ks = KieServices.Factory.get();
                InternalKieContainer kieContainer = (InternalKieContainer) ks.newKieContainer(releaseId);
                if( kieContainer != null ) {
                    context.getContainers().put(containerId, new KieContainerInfoImpl(containerId, KieContainerInfo.Status.STARTED, kieContainer));
                    return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "Container "+containerId+" successfully deployed with module "+releaseId+".");
                } else {
                    return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Failed to create container "+containerId+" with module "+releaseId+".");
                }        
            } else {
                return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Container "+containerId+" already exists.");
            }
        } catch (Exception e) {
            return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Error creating container "+containerId+
                    " with module "+releaseId+": "+e.getClass().getName()+": "+e.getMessage());
        }
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

}
