package org.kie.server.impl;

import java.util.List;

import org.kie.api.builder.ReleaseId;
import org.kie.server.api.KieServer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class KieServerMain {

    public static void main(String args[]) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"client-beans.xml"});

        KieServer client = (KieServer) context.getBean("client");

        List<ReleaseId> response = client.getModules();
        System.out.println("Response: " + response);
        System.exit(0);
    }

}