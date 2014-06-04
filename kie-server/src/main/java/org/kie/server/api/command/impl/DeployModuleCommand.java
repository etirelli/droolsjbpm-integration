package org.kie.server.api.command.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.server.api.KieContainerInfo;
import org.kie.server.api.command.KieServerCommand;
import org.kie.server.api.command.KieServerCommandContext;
import org.kie.server.api.command.ServiceResponse;
import org.kie.server.impl.KieContainerInfoImpl;

@XmlRootElement(name="deploy-module")
@XmlAccessorType(XmlAccessType.NONE)
public class DeployModuleCommand implements KieServerCommand<ServiceResponse> {
    private static final long serialVersionUID = -1803374525440238478L;
    
    @XmlAttribute(name="container-id")
    private String    containerId;
    @XmlElement(name="release-id")
    private ReleaseId releaseId;
    
    public DeployModuleCommand() {
        super();
    }
    
    public DeployModuleCommand(String containerId, ReleaseId releaseId) {
        this.containerId = containerId;
        this.releaseId = releaseId;
    }

    @Override
    public ServiceResponse execute(KieServerCommandContext context) {
        if( ! context.getContainers().containsKey(containerId) ) {
            KieServices ks = KieServices.Factory.get();
            KieContainer kieContainer = ks.newKieContainer(releaseId);
            if( kieContainer != null ) {
                context.getContainers().put(containerId, new KieContainerInfoImpl(containerId, KieContainerInfo.Status.STARTED, kieContainer));
                return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "Container "+containerId+" successfully deployed with KJar "+releaseId+".");
            } else {
                return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Failed to create container "+containerId+" with KJar "+releaseId+".");
            }        
        } else {
            return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Container "+containerId+" already exists.");
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
