package org.kie.server.api.command;

import java.util.Map;

import org.kie.server.api.KieContainerInfo;


public interface KieServerCommandContext {

    public abstract Map<String, KieContainerInfo> getContainers();

}
