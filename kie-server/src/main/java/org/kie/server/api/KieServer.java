package org.kie.server.api;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;

@WebService
public interface KieServer {
    
    public String deployModule( @WebParam(name="releaseId", mode=Mode.IN) String releasedId );
    
    public List<String> getModules();
    
    public String undeployModule( @WebParam(name="deploymentId", mode=Mode.IN) String deploymentId );

}
