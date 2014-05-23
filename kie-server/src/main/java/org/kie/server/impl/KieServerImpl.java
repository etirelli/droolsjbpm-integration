package org.kie.server.impl;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jws.WebService;

import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.server.api.KieContainerInfo;
import org.kie.server.api.KieServer;
import org.kie.server.api.ServiceResponse;

@WebService(endpointInterface="org.kie.server.api.KieServer", serviceName="KieServer")
public class KieServerImpl implements KieServer {
    private final Map<String, KieContainerInfo> containers = new ConcurrentHashMap<String, KieContainerInfo>();

    @Override
    public ServiceResponse deployModule(String containerId, ReleaseId releaseId) {
        KieServices ks = KieServices.Factory.get();
        KieContainer kieContainer = ks.newKieContainer(releaseId);
        if( kieContainer != null ) {
            containers.put(containerId, new KieContainerInfoImpl(containerId, KieContainerInfo.Status.STARTED, kieContainer));
            return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "Container "+containerId+" successfully deployed with KJar "+releaseId+".");
        } else {
            return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Failed to create container "+containerId+" with KJar "+releaseId+".");
        }
    }

    @Override
    public Collection<KieContainerInfo> getModules() {
        return containers.values();
    }

    @Override
    public ServiceResponse undeployModule(String containerId) {
        if( containers.containsKey(containerId) ) {
            containers.remove(containerId);
            return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "Container "+containerId+" successfully undeployed.");
        }
        return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Container "+containerId+" was not deployed.");
    }

}
