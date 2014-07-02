package org.kie.server.api.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="response")
@XmlAccessorType(XmlAccessType.NONE)
public class ServiceResponse {
    public static enum ResponseType {
        SUCCESS, FAILURE;
    }
    
    @XmlAttribute
    private ServiceResponse.ResponseType type;
    @XmlAttribute
    private String msg;
    @XmlElements({
        @XmlElement(name = "container", type = KieContainerInfo.class)
    })
    private List<KieContainerInfo> containers;
    @XmlElements({
        @XmlElement(name = "kie-server-info", type = KieServerInfo.class),
        @XmlElement(name = "results", type = String.class)
    })
    private Object result;
    
    
    public ServiceResponse() {
    }
    
    public ServiceResponse(ServiceResponse.ResponseType type, String msg) {
        this.type = type;
        this.msg = msg;
    }
    
    public ServiceResponse(ServiceResponse.ResponseType type, String msg, List<KieContainerInfo> containers) {
        this.type = type;
        this.msg = msg;
        this.containers = containers;
    }
    
    public ServiceResponse(ServiceResponse.ResponseType type, String msg, Object result ) {
        this.type = type;
        this.msg = msg;
        this.result = result;
    }
    
    public ServiceResponse.ResponseType getType() {
        return type;
    }
    
    public String getMsg() {
        return msg;
    }
    
    public void setType(ServiceResponse.ResponseType type) {
        this.type = type;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    public List<KieContainerInfo> getContainers() {
        return containers;
    }
    
    public void setContainers(List<KieContainerInfo> containers) {
        this.containers = containers;
    }
    
    public Object getResult() {
        return result;
    }
    
    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ServiceResponse[" + type + ", msg='" + msg + "']";
    }
}