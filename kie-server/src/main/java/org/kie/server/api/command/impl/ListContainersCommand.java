package org.kie.server.api.command.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.command.KieServerCommand;
import org.kie.server.api.command.KieServerCommandContext;
import org.kie.server.api.command.ServiceResponse;

@XmlRootElement(name="list-containers")
@XmlAccessorType(XmlAccessType.NONE)
public class ListContainersCommand implements KieServerCommand {
    private static final long serialVersionUID = -1803374525440238478L;
    
    public ListContainersCommand() {
        super();
    }
    
    @Override
    public ServiceResponse execute(KieServerCommandContext context) {
        try {
            return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS,context.getContainers().values().toString() );
        } catch (Exception e) {
            return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Error listing containers: "+
                    e.getClass().getName()+": "+e.getMessage());
        }
    }
}
