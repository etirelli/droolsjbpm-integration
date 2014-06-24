package org.kie.server.impl;

import static org.kie.scanner.MavenRepository.getMavenRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.internal.runtime.helper.BatchExecutionHelper;
import org.kie.scanner.MavenRepository;
import org.kie.server.api.KieServer;
import org.kie.server.api.command.CommandScript;
import org.kie.server.api.command.KieServerCommand;
import org.kie.server.api.command.ServiceResponse;
import org.kie.server.api.command.impl.CallContainerCommand;
import org.kie.server.api.command.impl.CommandScriptImpl;
import org.kie.server.api.command.impl.CreateContainerCommand;
import org.kie.server.api.command.impl.DisposeContainerCommand;

import com.thoughtworks.xstream.XStream;

public class KieServerTest {

    private static final String    BASE_ADDRESS = "/rest/server";
    private static MavenRepository repository;
    private static Server          server;
    private static ReleaseId       releaseId;
    private KieServer              proxy;

    @BeforeClass
    public static void initialize() throws Exception {
        createAndDeployKJar();
    }

    @AfterClass
    public static void destroy() throws Exception {
    }

    @Before
    public void setup() throws Exception {
        startServer();
        startClient();
    }

    @After
    public void tearDown() {
        server.stop();
        server.destroy();
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
    public void testListContainers() {
        Response response = proxy.createContainer(new CreateContainerCommand("kie1", releaseId));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = proxy.listContainers();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ServiceResponse reply = response.readEntity(ServiceResponse.class);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        Assert.assertEquals(1, reply.getContainers().size());
    }

    @Test
    public void testCallContainer() {
        Response response = proxy.createContainer(new CreateContainerCommand("kie1", releaseId));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String payload = "<batch-execution lookup=\"defaultKieSession\">\n" + 
                "  <insert out-identifier=\"message\">\n" + 
                "    <org.pkg1.Message>\n" + 
                "      <text>Hello World</text>\n" + 
                "    </org.pkg1.Message>\n" + 
                "  </insert>\n" +
                "  <fire-all-rules/>\n"+
                "</batch-execution>";

        response = proxy.execute("kie1", payload);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ServiceResponse reply = response.readEntity(ServiceResponse.class);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        
        System.out.println(reply.getMsg());
        System.out.println(reply.getResult());
    }

    @Test
    public void testCallContainerMarshallCommands() throws Exception {
        Response response = proxy.createContainer(new CreateContainerCommand("kie1", releaseId));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        KieServices ks = KieServices.Factory.get();
        File jar = MavenRepository.getMavenRepository().resolveArtifact(releaseId).getFile();
        URLClassLoader cl = new URLClassLoader(new URL[] { jar.toURI().toURL() } );
        Class<?> messageClass = cl.loadClass("org.pkg1.Message");
        Object message = messageClass.newInstance();
        Method setter = messageClass.getMethod("setText", String.class);
        Method getter = messageClass.getMethod("getText");
        setter.invoke(message, "HelloWorld");
        
        KieCommands kcmd = ks.getCommands();
        Command insert = kcmd.newInsert(message, "message");
        Command fire = kcmd.newFireAllRules();
        BatchExecutionCommand batch = kcmd.newBatchExecution(Arrays.asList(insert, fire), "defaultKieSession");
        
        String payload = BatchExecutionHelper.newXStreamMarshaller().toXML(batch);

        response = proxy.execute("kie1", payload);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ServiceResponse reply = response.readEntity(ServiceResponse.class);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        
        XStream xs = BatchExecutionHelper.newXStreamMarshaller();
        xs.setClassLoader(cl);
        ExecutionResults results = (ExecutionResults) xs.fromXML((String) reply.getResult());
        Object value = results.getValue("message");
        Assert.assertEquals("echo:HelloWorld", getter.invoke(value));
    }
    
    @Test
    public void testCommandScript() throws Exception {
        KieServices ks = KieServices.Factory.get();
        File jar = MavenRepository.getMavenRepository().resolveArtifact(releaseId).getFile();
        URLClassLoader cl = new URLClassLoader(new URL[] { jar.toURI().toURL() } );
        Class<?> messageClass = cl.loadClass("org.pkg1.Message");
        Object message = messageClass.newInstance();
        Method setter = messageClass.getMethod("setText", String.class);
        Method getter = messageClass.getMethod("getText");
        setter.invoke(message, "HelloWorld");
        
        KieCommands kcmd = ks.getCommands();
        Command insert = kcmd.newInsert(message, "message");
        Command fire = kcmd.newFireAllRules();
        BatchExecutionCommand batch = kcmd.newBatchExecution(Arrays.asList(insert, fire), "defaultKieSession");
        
        String payload = BatchExecutionHelper.newXStreamMarshaller().toXML(batch);

        KieServerCommand create = new CreateContainerCommand("kie1", releaseId);
        KieServerCommand call = new CallContainerCommand("kie1", payload);
        KieServerCommand dispose = new DisposeContainerCommand("kie1");
        
        List<KieServerCommand> cmds = Arrays.asList(create, call, dispose);
        CommandScript script = new CommandScriptImpl(cmds);
        
        Response response = proxy.execute(script);
        
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        List<ServiceResponse> reply = response.readEntity(new GenericType<List<ServiceResponse>>(){});
        for( ServiceResponse r : reply ) {
            System.out.println(">>>> Message: "+r.getMsg());
            System.out.println(">>>> Result : "+r.getResult());
            Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, r.getType());
        }
    }
    
    @Test
    public void testCallContainerLookupError() {
        Response response = proxy.createContainer(new CreateContainerCommand("kie1", releaseId));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        String payload = "<batch-execution lookup=\"xyz\">\n" + 
                "  <insert out-identifier=\"message\">\n" + 
                "    <org.pkg1.Message>\n" + 
                "      <text>Hello World</text>\n" + 
                "    </org.pkg1.Message>\n" + 
                "  </insert>\n" + 
                "</batch-execution>";

        response = proxy.execute("kie1", payload);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ServiceResponse reply = response.readEntity(ServiceResponse.class);
        Assert.assertEquals(ServiceResponse.ResponseType.FAILURE, reply.getType());
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
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setTransportId(LocalTransportFactory.TRANSPORT_ID);
        sf.setAddress(BASE_ADDRESS);
        sf.setResourceClasses(KieServerImpl.class);
        sf.setResourceProvider(new SingletonResourceProvider(new KieServerImpl()));
        server = sf.create();
    }

    private void startClient() {
        JAXRSClientFactoryBean bean = new JAXRSClientFactoryBean();
        bean.setTransportId(LocalTransportFactory.TRANSPORT_ID);
        bean.setAddress(BASE_ADDRESS);
        bean.setServiceClass(KieServer.class);
        proxy = bean.create(KieServer.class);
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
