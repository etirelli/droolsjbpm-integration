package org.kie.server.services.api.command.impl;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.server.services.api.command.KieServerCommand;
import org.kie.server.services.api.command.KieServerCommandContext;
import org.kie.server.services.api.command.ServiceResponse;
import org.kie.server.services.impl.KieContainerInfoImpl;
import org.kie.server.services.impl.XStreamXml;

import com.thoughtworks.xstream.XStream;

@XmlRootElement(name = "call-container")
@XmlAccessorType(XmlAccessType.NONE)
public class CallContainerCommand implements KieServerCommand {

    private static final long   serialVersionUID = -1803374525440238478L;

    public static final Pattern LOOKUP           = Pattern.compile("[\"']?lookup[\"']?\\s*[:=]\\s*[\"']([^\"']+)[\"']");

    @XmlAttribute(name = "container-id")
    private String              containerId;

    @XmlElement(name = "payload")
    private String              payload;

    public CallContainerCommand() {
        super();
    }

    public CallContainerCommand(String containerId, String cmdPayload) {
        this.containerId = containerId;
        this.payload = cmdPayload;
    }

    @Override
    public ServiceResponse execute(KieServerCommandContext context) {
        try {
            KieContainerInfoImpl kci = (KieContainerInfoImpl) context.getContainers().get(containerId);
            if (kci != null && kci.getKieContainer() != null) {
                String sessionId = null;
                // this is a weak way of finding the lookup, but it is the same used in kie-camel. Will keep it for now. 
                Matcher m = LOOKUP.matcher(payload);
                if (m.find()) {
                    sessionId = m.group(1);
                }

                KieSession ks = null;
                if (sessionId != null) {
                    ks = kci.getKieContainer().getKieSession(sessionId);
                } else {
                    ks = kci.getKieContainer().getKieSession();
                }
                if (ks != null) {
                    ClassLoader moduleClassLoader = kci.getKieContainer().getClassLoader();
                    XStream xs = XStreamXml.newXStreamMarshaller(moduleClassLoader);
                    Command<?> cmd = (Command<?>) xs.fromXML(payload);

                    if (cmd == null) {
                        return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Body of in message not of the expected type '" + Command.class.getName() + "'");
                    }
                    if (!(cmd instanceof BatchExecutionCommandImpl)) {
                        cmd = new BatchExecutionCommandImpl(Arrays.asList(new GenericCommand<?>[]{(GenericCommand<?>) cmd}));
                    }

                    ExecutionResults results = ks.execute((BatchExecutionCommandImpl) cmd);
                    String payload = xs.toXML(results);
                    return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "Container " + containerId + " successfully called.", payload);
                } else {
                    return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Session '" + sessionId + "' not found on container '" + containerId + "'.");
                }
            } else {
                return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Container " + containerId + " is not instantiated.");
            }
        } catch (Exception e) {
            return new ServiceResponse(ServiceResponse.ResponseType.FAILURE, "Error calling container " + containerId + ": " +
                    e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

}
