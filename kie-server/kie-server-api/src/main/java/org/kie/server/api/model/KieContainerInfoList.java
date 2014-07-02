package org.kie.server.api.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="kie-containers")
@XmlAccessorType (XmlAccessType.FIELD)
public class KieContainerInfoList {
    
    @XmlElement(name = "kie-container-info")
    private List<KieContainerInfo> containers;

    public KieContainerInfoList() {
        super();
    }

    public KieContainerInfoList(List<KieContainerInfo> containers) {
        super();
        this.containers = containers;
    }
    
    public List<KieContainerInfo> getContainers() {
        return containers;
    }
    
    public void setContainers(List<KieContainerInfo> containers) {
        this.containers = containers;
    }

}
