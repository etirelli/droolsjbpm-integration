package org.kie.server.impl;

import static org.kie.scanner.MavenRepository.getMavenRepository;

import javax.xml.ws.Endpoint;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.kie.scanner.MavenRepository;
import org.kie.server.api.KieServer;
import org.kie.server.api.command.ServiceResponse;
import org.kie.server.api.command.impl.CreateContainerCommand;

@Ignore
public class KieServerTest {

    private static final String ADDRESS = "http://localhost:9000/KieServer";

    private Endpoint            endpoint;

    private ReleaseId           releaseId;

    private static MavenRepository repository;

    @Before
    public void setup() {
        String drl = "package org.pkg1\n"
                + "global java.util.List list;"
                + "rule X when\n"
                + "    msg : String()\n"
                + "then\n"
                + "    list.add(msg);\n"
                + "end\n";
        KieServices ks = KieServices.Factory.get();
        releaseId = ks.newReleaseId("foo.bar", "baz", "2.1.0.GA");
        createAndDeployJar(ks, releaseId, drl);

        // make sure it is not deployed in the in-memory repository
        ks.getRepository().removeKieModule(releaseId);

        KieServerImpl implementor = new KieServerImpl();
        endpoint = Endpoint.publish(ADDRESS, implementor);
    }

    @After
    public void tearDown() {
        if (endpoint != null && endpoint.isPublished()) {
            endpoint.stop();
        }
    }

    @Test
    public void testDeployModule() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(KieServer.class);
        factory.setAddress(ADDRESS);
        KieServer client = (KieServer) factory.create();

//        ServiceResponse reply = client.execute(new DeployModuleCommand("kie1", releaseId));
//        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>> " + reply);
//        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
    }

    public static byte[] createAndDeployJar(KieServices ks,
            ReleaseId releaseId,
            String... drls) {
        KieFileSystem kfs = ks.newKieFileSystem().generateAndWritePomXML(
                releaseId);
        for (int i = 0; i < drls.length; i++) {
            if (drls[i] != null) {
                kfs.write("src/main/resources/org/pkg1/r" + i + ".drl", drls[i]);
            }
        }
        byte[] pom = kfs.read("pom.xml");
        KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
        Assert.assertFalse(kb.getResults().getMessages(org.kie.api.builder.Message.Level.ERROR).toString(),
                kb.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR));
        InternalKieModule kieModule = (InternalKieModule) ks.getRepository().getKieModule(releaseId);
        byte[] jar = kieModule.getBytes();

        repository = getMavenRepository();
        repository.deployArtifact(releaseId, jar, pom);
        return jar;
    }

}
