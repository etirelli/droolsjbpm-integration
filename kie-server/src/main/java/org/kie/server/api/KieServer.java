package org.kie.server.api;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;

import org.kie.api.builder.ReleaseId;

@WebService
public interface KieServer {
    
    public ServiceResponse deployModule( @WebParam(name="releaseId", mode=Mode.IN) ReleaseId releasedId );
    
    public List<String> getModules();
    
    public ServiceResponse undeployModule( @WebParam(name="deploymentId", mode=Mode.IN) String deploymentId );

}
