/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.services.optaplanner;

import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.*;
import org.kie.server.services.impl.KieServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public class OptaplannerKieServerExtension
        implements KieServerExtension {

    private static final Logger logger = LoggerFactory.getLogger( OptaplannerKieServerExtension.class );

    public static final String EXTENSION_NAME = "Optaplanner";

    private static final Boolean disabled = Boolean.parseBoolean( System.getProperty( KieServerConstants.KIE_OPTAPLANNER_SERVER_EXT_DISABLED, "false" ) );

    private KieServerRegistry registry;
    private SolverServiceBase solverServiceBase;

    private List<Object> services = new ArrayList<Object>();

    @Override
    public boolean isActive() {
        return disabled == false;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        KieServerExtension droolsExtension = registry.getServerExtension( "Drools" );
        if ( droolsExtension == null ) {
            logger.warn( "No Drools extension available, quiting..." );
            return;
        }
        this.registry = registry;
        this.solverServiceBase = new SolverServiceBase( registry );
        this.services.add( solverServiceBase );
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        // no-op?
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // creates solver factory?
        // needs to inspect kjar for solvers?
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {

    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
                = ServiceLoader.load( KieServerApplicationComponentsService.class );
        List<Object> appComponentsList = new ArrayList<Object>();
        Object[] services = {solverServiceBase, registry};
        for ( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) {
            appComponentsList.addAll( appComponentsService.getAppComponents( EXTENSION_NAME, type, services ) );
        }
        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        if ( serviceType.isAssignableFrom( solverServiceBase.getClass() ) ) {
            return (T) solverServiceBase;
        }
        return null;
    }

    @Override
    public String getImplementedCapability() {
        return "Optaplanner";
    }

    @Override
    public List<Object> getServices() {
        return services;
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public Integer getStartOrder() {
        return 25;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }

}
