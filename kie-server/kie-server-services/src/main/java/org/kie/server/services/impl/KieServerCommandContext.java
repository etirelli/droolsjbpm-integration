package org.kie.server.services.impl;

import java.util.Map;


public interface KieServerCommandContext {

    public abstract Map<String, KieContainerInstance> getContainers();

}
