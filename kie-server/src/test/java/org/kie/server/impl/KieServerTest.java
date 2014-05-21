package org.kie.server.impl;

import javax.xml.ws.Endpoint;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.KieServer;
import org.kie.server.api.ServiceResponse;


public class KieServerTest {
    private static final String ADDRESS = "http://localhost:9000/KieServer";
    
    private Endpoint endpoint;

    @Before
    public void setup() {
        KieServerImpl implementor = new KieServerImpl();
        endpoint = Endpoint.publish(ADDRESS, implementor);    
    }
    
    @After
    public void tearDown() {
        if( endpoint != null && endpoint.isPublished() ) {
            endpoint.stop();
        }
    }

    @Test
    public void testDeployModule() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(KieServer.class);
        factory.setAddress(ADDRESS);
        KieServer client = (KieServer) factory.create();
         
        ServiceResponse reply = client.deployModule(new ReleaseIdImpl("foo.bar", "baz", "1.0.1.GA"));
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>> "+reply);
        Assert.assertEquals( ServiceResponse.ResponseType.SUCCESS, reply.getType() );
    }

    @Test
    public void testUndeployModule() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(KieServer.class);
        factory.setAddress(ADDRESS);
        KieServer client = (KieServer) factory.create();
         
        ServiceResponse reply = client.undeployModule("1.0.1.GA");
        Assert.assertEquals( ServiceResponse.ResponseType.SUCCESS, reply.getType() );
    }
}
