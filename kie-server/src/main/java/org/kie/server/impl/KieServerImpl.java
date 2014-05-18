package org.kie.server.impl;

import java.util.Arrays;
import java.util.List;

import javax.jws.WebService;

import org.kie.server.api.KieServer;

@WebService(endpointInterface="org.kie.server.api.KieServer", serviceName="KieServer")
public class KieServerImpl implements KieServer {

    @Override
    public String deployModule(String releaseId) {
        System.out.println(">>> deploy "+releaseId);
        return "SUCCESS"; //new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "KJar "+releaseId+" successfully deployed.");
    }

    @Override
    public List<String> getModules() {
        System.out.println(">>> list modules");
        return Arrays.asList("org.foo:bar:1.0.0", "bax.baz:foo:5.4.3-SNAPSHOT");
    }

    @Override
    public String undeployModule(String releaseId) {
        System.out.println("--- undeploy "+releaseId);
        return "SUCCESS"; //new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "KJar "+releaseId+" successfully undeployed.");
    }

}
