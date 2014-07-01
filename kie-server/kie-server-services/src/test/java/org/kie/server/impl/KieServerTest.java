package org.kie.server.impl;

import static org.kie.scanner.MavenRepository.getMavenRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.GenericType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.internal.runtime.helper.BatchExecutionHelper;
import org.kie.scanner.MavenRepository;
import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.CreateContainerCommand;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.entity.KieServerCommand;
import org.kie.server.api.entity.ReleaseId;
import org.kie.server.api.entity.ServiceResponse;
import org.kie.server.services.impl.KieServerImpl;

import com.thoughtworks.xstream.XStream;

public class KieServerTest {

    private static final int       PORT         = 8756;
    private static final String    BASE_URI = "http://localhost:"+PORT+"/server";
    private static MavenRepository repository;
    private static ReleaseId       releaseId;
    private TJWSEmbeddedJaxrsServer server;

    @BeforeClass
    public static void initialize() throws Exception {
        createAndDeployKJar();
        // this initialization only needs to be done once per VM
        RegisterBuiltin.register(ResteasyProviderFactory.getInstance());        
    }

    @AfterClass
    public static void destroy() throws Exception {
    }

    @Before
    public void setup() throws Exception {
        startServer();
        //startClient();
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    public void testCreateContainer() throws Exception {
        ClientResponse<ServiceResponse> response = createContainer("kie1");
        
        ServiceResponse reply = response.getEntity();
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
    }

    @Test
    public void testListContainers() throws Exception{
        createContainer("kie1");
        createContainer("kie2");

        ClientRequest clientRequest = new ClientRequest(BASE_URI+"/containers");
        ClientResponse<ServiceResponse> response = clientRequest.get(ServiceResponse.class);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ServiceResponse reply = response.getEntity();
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        Assert.assertEquals(2, reply.getContainers().size());
    }

    @Test
    public void testDisposeContainer() throws Exception{
        createContainer("kie1");

        ClientRequest clientRequest = new ClientRequest(BASE_URI+"/containers/kie1");
        ClientResponse<ServiceResponse> response = clientRequest.delete(ServiceResponse.class);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ServiceResponse reply = response.getEntity();
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
    }

    @Test
    public void testCallContainer() throws Exception {
        createContainer("kie1");

        String payload = "<batch-execution lookup=\"defaultKieSession\">\n" +
                "  <insert out-identifier=\"message\">\n" +
                "    <org.pkg1.Message>\n" +
                "      <text>Hello World</text>\n" +
                "    </org.pkg1.Message>\n" +
                "  </insert>\n" +
                "  <fire-all-rules/>\n" +
                "</batch-execution>";

        ServiceResponse reply = executeCommands("kie1", payload);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
    }

    @Test
    public void testCallContainerMarshallCommands() throws Exception {
        createContainer("kie1");

        KieServices ks = KieServices.Factory.get();
        File jar = MavenRepository.getMavenRepository().resolveArtifact(releaseId).getFile();
        URLClassLoader cl = new URLClassLoader(new URL[]{jar.toURI().toURL()});
        Class<?> messageClass = cl.loadClass("org.pkg1.Message");
        Object message = messageClass.newInstance();
        Method setter = messageClass.getMethod("setText", String.class);
        Method getter = messageClass.getMethod("getText");
        setter.invoke(message, "HelloWorld");

        KieCommands kcmd = ks.getCommands();
        Command<?> insert = kcmd.newInsert(message, "message");
        Command<?> fire = kcmd.newFireAllRules();
        BatchExecutionCommand batch = kcmd.newBatchExecution(Arrays.asList(insert, fire), "defaultKieSession");

        String payload = BatchExecutionHelper.newXStreamMarshaller().toXML(batch);

        ServiceResponse reply = executeCommands("kie1", payload);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        XStream xs = BatchExecutionHelper.newXStreamMarshaller();
        xs.setClassLoader(cl);
        ExecutionResults results = (ExecutionResults) xs.fromXML((String) reply.getResult());
        Object value = results.getValue("message");
        Assert.assertEquals("echo:HelloWorld", getter.invoke(value));

        cl.close();
    }

    @Test
    public void testCommandScript() throws Exception {
        KieServices ks = KieServices.Factory.get();
        File jar = MavenRepository.getMavenRepository().resolveArtifact(releaseId).getFile();
        URLClassLoader cl = new URLClassLoader(new URL[]{jar.toURI().toURL()});
        Class<?> messageClass = cl.loadClass("org.pkg1.Message");
        Object message = messageClass.newInstance();
        Method setter = messageClass.getMethod("setText", String.class);
        setter.invoke(message, "HelloWorld");

        KieCommands kcmd = ks.getCommands();
        Command<?> insert = kcmd.newInsert(message, "message");
        Command<?> fire = kcmd.newFireAllRules();
        BatchExecutionCommand batch = kcmd.newBatchExecution(Arrays.asList(insert, fire), "defaultKieSession");

        String payload = BatchExecutionHelper.newXStreamMarshaller().toXML(batch);

        KieServerCommand create = new CreateContainerCommand("kie1", releaseId);
        KieServerCommand call = new CallContainerCommand("kie1", payload);
        KieServerCommand dispose = new DisposeContainerCommand("kie1");

        List<KieServerCommand> cmds = Arrays.asList(create, call, dispose);
        CommandScript script = new CommandScript(cmds);

        List<ServiceResponse> reply = executeScript(script);

        for (ServiceResponse r : reply) {
            Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, r.getType());
        }

        cl.close();
    }

    @Test
    public void testCallContainerLookupError() throws Exception {
        createContainer("kie1");

        String payload = "<batch-execution lookup=\"xyz\">\n" +
                "  <insert out-identifier=\"message\">\n" +
                "    <org.pkg1.Message>\n" +
                "      <text>Hello World</text>\n" +
                "    </org.pkg1.Message>\n" +
                "  </insert>\n" +
                "</batch-execution>";

        ServiceResponse reply = executeCommands("kie1", payload);
        Assert.assertEquals(ServiceResponse.ResponseType.FAILURE, reply.getType());
    }

    private ClientResponse<ServiceResponse> createContainer(String id) throws Exception {
        ClientRequest clientRequest = new ClientRequest(BASE_URI+"/containers/"+id);
        ClientResponse<ServiceResponse> response = clientRequest.body(MediaType.APPLICATION_XML_TYPE, releaseId).put(ServiceResponse.class);
        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        return response;
    }

    private ServiceResponse executeCommands(String id, String payload) throws Exception {
        ClientRequest clientRequest = new ClientRequest(BASE_URI+"/containers/"+id);
        ClientResponse<ServiceResponse> response = clientRequest.body(MediaType.APPLICATION_XML_TYPE, payload).post(ServiceResponse.class);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ServiceResponse reply = response.getEntity();
        return reply;
    }

    private List<ServiceResponse> executeScript(CommandScript script) throws Exception {
        ClientRequest clientRequest = new ClientRequest(BASE_URI);
        ClientResponse<List<ServiceResponse>> response = clientRequest.body(MediaType.APPLICATION_XML_TYPE, script).post(new GenericType<List<ServiceResponse>>() {});
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        List<ServiceResponse> reply = response.getEntity();
        return reply;
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

        try {
            FileOutputStream fos = new FileOutputStream("target/baz-2.1.0.GA.jar");
            fos.write(jar);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        repository = getMavenRepository();
        repository.deployArtifact(releaseId, jar, pom);
        return jar;
    }

    private void startServer() throws Exception {
        server = new TJWSEmbeddedJaxrsServer();
        server.setPort(PORT);
        server.start();
        server.getDeployment().getRegistry().addSingletonResource(new KieServerImpl());
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
        releaseId = new ReleaseId("foo.bar", "baz", "2.1.0.GA");
        createAndDeployJar(ks, releaseId, drl);

        // make sure it is not deployed in the in-memory repository
        ks.getRepository().removeKieModule(releaseId);
    }

    public static int findFreePort()
            throws IOException {
        ServerSocket server =
                new ServerSocket(0);
        int port = server.getLocalPort();
        server.close();
        return port;
    }

}
