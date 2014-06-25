package org.kie.server.services.api.command.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.services.api.command.KieServerCommand;
import org.kie.server.services.api.command.KieServerCommandContext;
import org.kie.server.services.api.command.ServiceResponse;
import org.kie.server.services.impl.KieContainerInfoImpl;

@XmlRootElement(name="dispose-container")
@XmlAccessorType(XmlAccessType.NONE)
public class DisposeContainerCommand implements KieServerCommand {
    private static final long serialVersionUID = -1803374525440238478L;
    
    @XmlAttribute(name="container-id")
    private String    containerId;
    
    public DisposeContainerCommand() {
        super();
    }
    
    public DisposeContainerCommand(String containerId) {
        this.containerId = containerId;
    }

    @Override
    public ServiceResponse execute(KieServerCommandContext context) {
        try {
            KieContainerInfoImpl kci = (KieContainerInfoImpl) context.getContainers().remove(containerId);
            if( kci != null && kci.getKieContainer() != null ) {
                kci.getKieContainer().dispose();
                return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "Container "+containerId+" successfully disposed.");
            } else {
                return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "Container "+containerId+" was not instantiated.");
            }
        } catch (Exception e) {
            return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Error disposing container "+containerId+": "+
                    e.getClass().getName()+": "+e.getMessage());
        }
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

}
