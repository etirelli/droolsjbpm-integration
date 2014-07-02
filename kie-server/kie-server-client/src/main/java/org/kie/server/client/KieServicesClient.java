package org.kie.server.client;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.jboss.resteasy.util.GenericType;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.KieContainerInfo;
import org.kie.server.api.model.KieContainerInfoList;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;


public class KieServicesClient {
    
    private final String baseURI;
    
    public KieServicesClient(String baseURI) {
        this.baseURI = baseURI;
    }

    public ServiceResponse<KieServerInfo> getServerInfo() throws ClientResponseFailure {
        ClientResponse<ServiceResponse<KieServerInfo>> response = null;
        try {
            ClientRequest clientRequest = new ClientRequest(baseURI);
            response = clientRequest.get(new GenericType<ServiceResponse<KieServerInfo>>(){});
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception retrieving server info.", e, response );
        }
    }

    public ServiceResponse<KieContainerInfoList> listContainers() throws ClientResponseFailure {
        ClientResponse<ServiceResponse<KieContainerInfoList>> response = null;
        try {
            ClientRequest clientRequest = new ClientRequest(baseURI+"/containers");
            response = clientRequest.get(new GenericType<ServiceResponse<KieContainerInfoList>>(){});
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception retrieving list of containers.", e, response );
        }
    }

    public ServiceResponse<KieContainerInfo> createContainer(String id, ReleaseId releaseId) throws ClientResponseFailure {
        ClientResponse<ServiceResponse<KieContainerInfo>> response = null;
        try {
            ClientRequest clientRequest = new ClientRequest(baseURI+"/containers/"+id);
            response = clientRequest.body(MediaType.APPLICATION_XML_TYPE, releaseId).put(new GenericType<ServiceResponse<KieContainerInfo>>(){});
            if( response.getStatus() == Response.Status.CREATED.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception creating container: "+id+" with release-id "+releaseId, e, response );
        }
    }

    public ServiceResponse<KieContainerInfo> getContainerInfo(String id) throws ClientResponseFailure {
        ClientResponse<ServiceResponse<KieContainerInfo>> response = null;
        try {
            ClientRequest clientRequest = new ClientRequest(baseURI+"/containers/"+id);
            response = clientRequest.get(new GenericType<ServiceResponse<KieContainerInfo>>(){});
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception retrieving container info.", e, response );
        }
    }

    public ServiceResponse<Void> disposeContainer(String id) throws ClientResponseFailure {
        ClientResponse<ServiceResponse<Void>> response = null;
        try {
            ClientRequest clientRequest = new ClientRequest(baseURI+"/containers/"+id);
            response = clientRequest.delete(new GenericType<ServiceResponse<Void>>(){});
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception disposing container: "+id, e, response );
        }
    }

    public ServiceResponse<String> executeCommands(String id, String payload) throws ClientResponseFailure {
        ClientResponse<ServiceResponse<String>> response = null;
        try {
            ClientRequest clientRequest = new ClientRequest(baseURI+"/containers/"+id);
            response = clientRequest.body(MediaType.APPLICATION_XML_TYPE, payload).post(new GenericType<ServiceResponse<String>>(){});
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception executing commands on container "+id, e, response );
        }
    }

    public List<ServiceResponse<? extends Object>> executeScript(CommandScript script) throws ClientResponseFailure {
        ClientResponse<List<ServiceResponse<? extends Object>>> response = null;
        try {
            ClientRequest clientRequest = new ClientRequest(baseURI);
            response = clientRequest.body(MediaType.APPLICATION_XML_TYPE, script).post(new GenericType<List<ServiceResponse<? extends Object>>>() {});
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception retrieving server info.", e, response );
        }
    }
}
