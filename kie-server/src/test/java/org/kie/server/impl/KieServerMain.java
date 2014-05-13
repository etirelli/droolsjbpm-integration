package org.kie.server.impl;

import javax.xml.ws.Endpoint;


public class KieServerMain {
    
    protected KieServerMain() throws Exception {
        // START SNIPPET: publish
        System.out.println("Starting Server");
        KieServerImpl implementor = new KieServerImpl();
        String address = "http://localhost:9000/KieServer";
        Endpoint.publish(address, implementor);
        // END SNIPPET: publish
    }

    public static void main(String args[]) throws Exception {
        new KieServerMain();
        System.out.println("Server ready...");

        Thread.sleep(10 * 60 * 1000);
        System.out.println("Server exiting");
        System.exit(0);
    }
}
