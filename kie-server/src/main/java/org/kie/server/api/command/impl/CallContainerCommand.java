package org.kie.server.api.command.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.command.KieServerCommand;
import org.kie.server.api.command.KieServerCommandContext;
import org.kie.server.api.command.ServiceResponse;
import org.kie.server.impl.KieContainerInfoImpl;
import org.kie.server.impl.XStreamXml;

import com.thoughtworks.xstream.XStream;

@XmlRootElement(name="call-container")
@XmlAccessorType(XmlAccessType.NONE)
public class CallContainerCommand implements KieServerCommand {
    private static final long serialVersionUID = -1803374525440238478L;
    
    @XmlAttribute(name="container-id")
    private String    containerId;

    @XmlElement(name="payload")
    private String payload;
    
    public CallContainerCommand() {
        super();
    }
    
    public CallContainerCommand(String containerId, String cmdPayload ) {
        this.containerId = containerId;
        this.payload = cmdPayload; 
    }

    @Override
    public ServiceResponse execute(KieServerCommandContext context) {
        try {
            KieContainerInfoImpl kci = (KieContainerInfoImpl) context.getContainers().get(containerId);
            if( kci != null && kci.getKieContainer() != null ) {
                System.out.println(">>>>>>>>> PAYLOAD = "+payload);
                
                //ClassLoader original = null;
                try {
                    //original = Thread.currentThread().getContextClassLoader();
                    XStream xs = XStreamXml.newXStreamMarshaller();
                    xs.setClassLoader(kci.getKieContainer().getClassLoader());
                    Object cmds = xs.fromXML(payload);
                    System.out.println(">>>>>>>>> ClassName = "+cmds.getClass().getName());
                
                } finally {
                    //Thread.currentThread().setContextClassLoader(original);
                }
                
                return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "Container "+containerId+" successfully called.");
            } else {
                return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "Container "+containerId+" was not instantiated.");
            }
        } catch (Exception e) {
            return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Error calling container "+containerId+": "+
                    e.getClass().getName()+": "+e.getMessage());
        }
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

}
