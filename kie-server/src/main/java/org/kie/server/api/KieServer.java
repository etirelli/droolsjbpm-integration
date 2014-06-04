package org.kie.server.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.kie.server.api.command.BatchExecutionCommand;

@Path("/server")
public interface KieServer {
    
    @POST
    @Path("execute")
    @Consumes("application/xml")
    @Produces("application/xml")
    public <T> Response execute( BatchExecutionCommand command );
    
} 
