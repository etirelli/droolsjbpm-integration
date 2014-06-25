package org.kie.server.services.api.command;

import java.util.Map;

import org.kie.server.services.api.KieContainerInfo;


public interface KieServerCommandContext {

    public abstract Map<String, KieContainerInfo> getContainers();

}
