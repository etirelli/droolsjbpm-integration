package org.kie.server.services.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.kie.server.services.api.command.CommandScript;
import org.kie.server.services.api.command.impl.CreateContainerCommand;

@Path("/server")
public interface KieServer {
    
    @POST
    @Consumes("application/xml")
    @Produces("application/xml")
    public Response execute( CommandScript command );
    
    @GET
    @Path("containers")
    @Produces("application/xml")
    public Response listContainers();
    
    @POST
    @Path("containers")
    @Consumes("application/xml")
    @Produces("application/xml")
    public Response createContainer( CreateContainerCommand command );
    
    @DELETE
    @Path("containers/{id}")
    @Consumes("application/xml")
    @Produces("application/xml")
    public Response disposeContainer( @PathParam("id") String id );
    
    @POST
    @Path("containers/{id}")
    @Consumes("application/xml")
    @Produces("application/xml")
    public Response execute( @PathParam("id") String id, String cmdPayload );
    
} 
