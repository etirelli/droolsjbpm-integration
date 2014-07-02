package org.kie.server.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Path;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.drools.compiler.kie.builder.impl.InternalKieContainer;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.CreateContainerCommand;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.commands.ListContainersCommand;
import org.kie.server.api.model.KieContainerInfo;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.services.api.KieServer;

import com.thoughtworks.xstream.XStream;

@Path("/server")
public class KieServerImpl implements KieServer {

    private final Map<String, KieContainerInstance> containers;
    private final KieServerCommandContext   context;
    
    public static final Pattern LOOKUP           = Pattern.compile("[\"']?lookup[\"']?\\s*[:=]\\s*[\"']([^\"']+)[\"']");

    public KieServerImpl() {
        this.containers = new ConcurrentHashMap<String, KieContainerInstance>();
        this.context = new KieServerCommandContextImpl(containers);
    }
    
    @Override
    public Response getInfo() {
        return Response.ok(getInfo(context)).build();
    }

    @Override
    public Response execute(CommandScript command) {
        return Response.ok(new GenericEntity<List<ServiceResponse>>(executeScript(context, command)){}).build();
    }
    
    @Override
    public Response listContainers() {
        return Response.ok(listContainers(context)).build();
    }

    @Override
    public Response createContainer(String id, ReleaseId releaseId ) {
        return Response.status(Status.CREATED).entity(createContainer(context, id, releaseId)).build();
    }
    
    @Override
    public Response disposeContainer(String id) {
        return Response.ok(disposeContainer(context, id)).build();
    }
    
    @Override
    public Response execute(String id, String cmdPayload) {
        return Response.ok(callContainer(context, id, cmdPayload)).build();
    }
    
    private List<ServiceResponse> executeScript(KieServerCommandContext context, CommandScript commands) {
        List<ServiceResponse> response = new ArrayList<ServiceResponse>();
        for (KieServerCommand command : commands.getCommands()) {
            if( command instanceof CreateContainerCommand ) {
                response.add(createContainer(context, ((CreateContainerCommand) command).getContainerId(), ((CreateContainerCommand) command).getReleaseId()));
            } else if( command instanceof ListContainersCommand ) {
                response.add(listContainers(context));
            } else if( command instanceof CallContainerCommand ) {
                response.add(callContainer(context, ((CallContainerCommand) command).getContainerId(), ((CallContainerCommand) command).getPayload()));
            } else if( command instanceof DisposeContainerCommand ) {
                response.add(disposeContainer(context, ((DisposeContainerCommand) command).getContainerId()));
            }
        }
        return response;
    }
    
    private ServiceResponse getInfo(KieServerCommandContext context) {
        try {
            return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "Kie Server info", new KieServerInfo(KieServerEnvironment.getVersion().toString()) );
        } catch (Exception e) {
            return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Error retrieving kie server info: "+
                    e.getClass().getName()+": "+e.getMessage());
        }
    }
    
    private ServiceResponse createContainer(KieServerCommandContext context, String containerId, ReleaseId releaseId) {
        try {
            if( ! context.getContainers().containsKey(containerId) ) {
                KieServices ks = KieServices.Factory.get();
                InternalKieContainer kieContainer = (InternalKieContainer) ks.newKieContainer(releaseId);
                if( kieContainer != null ) {
                    context.getContainers().put(containerId, new KieContainerInstance(containerId, KieContainerInfo.Status.STARTED, kieContainer));
                    return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "Container "+containerId+" successfully deployed with module "+releaseId+".");
                } else {
                    return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Failed to create container "+containerId+" with module "+releaseId+".");
                }        
            } else {
                return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Container "+containerId+" already exists.");
            }
        } catch (Exception e) {
            return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Error creating container "+containerId+
                    " with module "+releaseId+": "+e.getClass().getName()+": "+e.getMessage());
        }
    }
    
    private ServiceResponse listContainers(KieServerCommandContext context) {
        try {
            List<KieContainerInfo> containers = new ArrayList<KieContainerInfo>();
            for( KieContainerInstance instance : context.getContainers().values() ) {
                containers.add(instance.getInfo() );
            }
            return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "List of created containers", containers );
        } catch (Exception e) {
            return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Error listing containers: "+
                    e.getClass().getName()+": "+e.getMessage());
        }
    }
    

    private ServiceResponse callContainer(KieServerCommandContext context, String containerId, String payload ) {
        try {
            KieContainerInstance kci = (KieContainerInstance) context.getContainers().get(containerId);
            if (kci != null && kci.getKieContainer() != null) {
                String sessionId = null;
                // this is a weak way of finding the lookup, but it is the same used in kie-camel. Will keep it for now. 
                Matcher m = LOOKUP.matcher(payload);
                if (m.find()) {
                    sessionId = m.group(1);
                }

                KieSession ks = null;
                if (sessionId != null) {
                    ks = kci.getKieContainer().getKieSession(sessionId);
                } else {
                    ks = kci.getKieContainer().getKieSession();
                }
                if (ks != null) {
                    ClassLoader moduleClassLoader = kci.getKieContainer().getClassLoader();
                    XStream xs = XStreamXml.newXStreamMarshaller(moduleClassLoader);
                    Command<?> cmd = (Command<?>) xs.fromXML(payload);

                    if (cmd == null) {
                        return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Body of in message not of the expected type '" + Command.class.getName() + "'");
                    }
                    if (!(cmd instanceof BatchExecutionCommandImpl)) {
                        cmd = new BatchExecutionCommandImpl(Arrays.asList(new GenericCommand<?>[]{(GenericCommand<?>) cmd}));
                    }

                    ExecutionResults results = ks.execute((BatchExecutionCommandImpl) cmd);
                    String result = xs.toXML(results);
                    return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "Container " + containerId + " successfully called.", result);
                } else {
                    return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Session '" + sessionId + "' not found on container '" + containerId + "'.");
                }
            } else {
                return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Container " + containerId + " is not instantiated.");
            }
        } catch (Exception e) {
            return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Error calling container " + containerId + ": " +
                    e.getClass().getName() + ": " + e.getMessage());
        }
    }
    
    private ServiceResponse disposeContainer(KieServerCommandContext context, String containerId ) {
        try {
            KieContainerInstance kci = (KieContainerInstance) context.getContainers().remove(containerId);
            if( kci != null && kci.getKieContainer() != null ) {
                kci.getKieContainer().dispose();
                return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "Container "+containerId+" successfully disposed.");
            } else {
                return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "Container "+containerId+" was not instantiated.");
            }
        } catch (Exception e) {
            return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Error disposing container "+containerId+": "+
                    e.getClass().getName()+": "+e.getMessage());
        }
    }

    public static class KieServerCommandContextImpl implements KieServerCommandContext {
        private final Map<String, KieContainerInstance> containers;

        public KieServerCommandContextImpl(Map<String, KieContainerInstance> containers) {
            this.containers = containers;
        }

        public Map<String, KieContainerInstance> getContainers() {
            return containers;
        }
    }

}
