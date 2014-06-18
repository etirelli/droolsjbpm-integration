package org.kie.server.impl;

import static org.kie.scanner.MavenRepository.getMavenRepository;

import javax.ws.rs.core.Response;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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

public class KieServerTest {

    private static final String    BASE_ADDRESS = "/rest/server";
    private static MavenRepository repository;
    private static Server          server;
    private static ReleaseId       releaseId;
    private KieServer              proxy;

    @BeforeClass
    public static void initialize() throws Exception {
        startServer();
        createAndDeployKJar();
    }

    @AfterClass
    public static void destroy() throws Exception {
        server.stop();
        server.destroy();
    }

    @Before
    public void setup() {
        JAXRSClientFactoryBean bean = new JAXRSClientFactoryBean();
        bean.setTransportId(LocalTransportFactory.TRANSPORT_ID);
        bean.setAddress(BASE_ADDRESS);
        bean.setServiceClass(KieServer.class);
        proxy = bean.create(KieServer.class);
    }

    @Test
    public void testCreateContainer() {
        // creates first container
        Response response = proxy.createContainer(new CreateContainerCommand("kie1", releaseId));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ServiceResponse reply = response.readEntity(ServiceResponse.class);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        // creates second container
        response = proxy.createContainer(new CreateContainerCommand("kie2", releaseId));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        reply = response.readEntity(ServiceResponse.class);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
    }

    @Test
    @Ignore("not keeping state in between calls. need to figure why.")
    public void testListContainers() {
        Response response = proxy.createContainer(new CreateContainerCommand("kie1", releaseId));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = proxy.listContainers();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ServiceResponse reply = response.readEntity(ServiceResponse.class);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        Assert.assertEquals(1, reply.getContainers().size());
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

    private static void startServer() throws Exception {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setTransportId(LocalTransportFactory.TRANSPORT_ID);
        sf.setAddress(BASE_ADDRESS);
        sf.setResourceClasses(KieServerImpl.class);
        server = sf.create();
    }

    private static void createAndDeployKJar() {
        String drl = "package org.pkg1\n"
                + "global java.util.List list;"
                + "declare Message\n" 
                + "    text : String\n" 
                + "end\n" 
                + "rule echo dialect \"mvel\"\n" 
                + "when\n" 
                + "    $m : Message()\n" 
                + "then\n" 
                + "    $m.text = \"echo:\" + $m.text;\n" 
                + "end\n"
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
    }
}
