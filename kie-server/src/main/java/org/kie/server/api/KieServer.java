package org.kie.server.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.drools.core.command.impl.GenericCommand;

@Path("/server")
public interface KieServer {
    
    @POST
    @Path("execute")
    @Consumes("application/xml")
    @Produces("application/xml")
    public <T> T execute( GenericCommand<T> command );
    
} 
