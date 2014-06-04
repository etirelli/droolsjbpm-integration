package org.kie.server.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response;

import org.kie.server.api.KieContainerInfo;
import org.kie.server.api.KieServer;
import org.kie.server.api.command.BatchExecutionCommand;
import org.kie.server.api.command.KieServerCommandContext;

public class KieServerImpl implements KieServer {

    private final Map<String, KieContainerInfo> containers;
    private final KieServerCommandContext   context;

    public KieServerImpl() {
        this.containers = new ConcurrentHashMap<String, KieContainerInfo>();
        this.context = new KieServerCommandContextImpl(containers);
    }

    @Override
    public <T> Response execute(BatchExecutionCommand command) {
        return Response.ok(command.execute(context)).build();
    }

    public static class KieServerCommandContextImpl implements KieServerCommandContext {

        private final Map<String, KieContainerInfo> containers;

        public KieServerCommandContextImpl(Map<String, KieContainerInfo> containers) {
            this.containers = containers;
        }

        @Override
        public Map<String, KieContainerInfo> getContainers() {
            return containers;
        }
    }
}
