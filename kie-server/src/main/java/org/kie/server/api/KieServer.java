package org.kie.server.api;

import java.util.Collection;

import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;

import org.kie.api.builder.ReleaseId;

@WebService
public interface KieServer {
    
    public ServiceResponse deployModule( @WebParam(name="containerId", mode=Mode.IN) String containerId, 
                                          @WebParam(name="releaseId", mode=Mode.IN) ReleaseId releasedId );
    
    public Collection<KieContainerInfo> getModules();
    
    public ServiceResponse undeployModule( @WebParam(name="containerId", mode=Mode.IN) String containerId );

} 
