package org.kie.server.api;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;

import org.kie.api.builder.ReleaseId;

@WebService
public interface KieServer {
    
    @RequestWrapper(className="org.drools.compiler.kproject.ReleaseIdImpl")
    public ServiceResponse deployModule( @WebParam(name="releaseId", mode=Mode.IN) ReleaseId releasedId );
    
    public List<ReleaseId> getModules();
    
    @RequestWrapper(className="org.drools.compiler.kproject.ReleaseIdImpl")
    public ServiceResponse undeployModule( @WebParam(name="deploymentId", mode=Mode.IN) ReleaseId deploymentId );

}
