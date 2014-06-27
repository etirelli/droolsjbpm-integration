package org.kie.server.services.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.Path;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.kie.api.builder.ReleaseId;
import org.kie.server.services.api.KieContainerInfo;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.api.command.CommandScript;
import org.kie.server.services.api.command.KieServerCommandContext;
import org.kie.server.services.api.command.ServiceResponse;
import org.kie.server.services.api.command.impl.CallContainerCommand;
import org.kie.server.services.api.command.impl.CreateContainerCommand;
import org.kie.server.services.api.command.impl.DisposeContainerCommand;
import org.kie.server.services.api.command.impl.ListContainersCommand;

@Path("/server")
public class KieServerImpl implements KieServer {

    private final Map<String, KieContainerInfo> containers;
    private final KieServerCommandContext   context;

    public KieServerImpl() {
        this.containers = new ConcurrentHashMap<String, KieContainerInfo>();
        this.context = new KieServerCommandContextImpl(containers);
    }

    @Override
    public Response execute(CommandScript command) {
        return Response.ok(new GenericEntity<List<ServiceResponse>>(command.execute(context)){}).build();
    }
    
    @Override
    public Response listContainers() {
        return Response.ok(new ListContainersCommand().execute(context)).build();
    }

    @Override
    public Response createContainer(String id, ReleaseId releaseId ) {
        return Response.ok(new CreateContainerCommand(id, releaseId).execute(context)).build();
    }
    
    @Override
    public Response disposeContainer(String id) {
        return Response.ok(new DisposeContainerCommand(id).execute(context)).build();
    }
    
    @Override
    public Response execute(String id, String cmdPayload) {
        return Response.ok(new CallContainerCommand(id, cmdPayload).execute(context)).build();
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
