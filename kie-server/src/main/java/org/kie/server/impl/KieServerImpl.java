package org.kie.server.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jws.WebService;

import org.drools.core.command.impl.GenericCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.CommandExecutor;
import org.kie.server.api.KieContainerInfo;

//@WebService(endpointInterface="org.kie.server.api.KieServer", serviceName="KieServer")
public class KieServerImpl implements CommandExecutor {
    private final Map<String, KieContainerInfo> containers = new ConcurrentHashMap<String, KieContainerInfo>();

//    @POST
//    @Path("/execute")
//    @Produces({"application/xml","application/json"})
//    @Consumes({"application/xml","application/json","application/x-www-form-urlencoded"})
//    public <T> T execute(GenericCommand<T> command) {
//        return ;
//    }

    @Override
    public <T> T execute(Command<T> command) {
        return ((GenericCommand<T>)command).execute(null);
    }

}
