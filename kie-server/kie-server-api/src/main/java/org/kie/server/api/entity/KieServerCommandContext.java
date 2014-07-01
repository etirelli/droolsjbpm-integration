package org.kie.server.api.entity;

import java.util.Map;


public interface KieServerCommandContext {

    public abstract Map<String, KieContainerInfo> getContainers();

}
