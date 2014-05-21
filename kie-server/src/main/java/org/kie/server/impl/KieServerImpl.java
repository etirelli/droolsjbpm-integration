package org.kie.server.impl;

import java.util.Arrays;
import java.util.List;

import javax.jws.WebService;

import org.kie.api.builder.ReleaseId;
import org.kie.server.api.KieServer;
import org.kie.server.api.ServiceResponse;

@WebService(endpointInterface="org.kie.server.api.KieServer", serviceName="KieServer")
public class KieServerImpl implements KieServer {

    @Override
    public ServiceResponse deployModule(ReleaseId releaseId) {
        System.out.println(">>> deploy "+releaseId);
        return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "KJar "+releaseId+" successfully deployed.");
    }

    @Override
    public List<String> getModules() {
        System.out.println(">>> list modules");
        return Arrays.asList("org.foo:bar:1.0.0", "bax.baz:foo:5.4.3-SNAPSHOT");
    }

    @Override
    public ServiceResponse undeployModule(String releaseId) {
        System.out.println("--- undeploy "+releaseId);
        return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "KJar "+releaseId+" successfully undeployed.");
    }

}
