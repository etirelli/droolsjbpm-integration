package org.kie.server.client;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.jboss.resteasy.util.GenericType;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;


public class KieServicesClient {
    
    private final String baseURI;
    
    public KieServicesClient(String baseURI) {
        this.baseURI = baseURI;
    }

    public ServiceResponse getServerInfo() throws ClientResponseFailure {
        ClientResponse<ServiceResponse> response = null;
        try {
            ClientRequest clientRequest = new ClientRequest(baseURI);
            response = clientRequest.get(ServiceResponse.class);
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception retrieving server info.", e, response );
        }
    }

    public ServiceResponse listContainers() throws ClientResponseFailure {
        ClientResponse<ServiceResponse> response = null;
        try {
            ClientRequest clientRequest = new ClientRequest(baseURI+"/containers");
            response = clientRequest.get(ServiceResponse.class);
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception retrieving list of containers.", e, response );
        }
    }

    public ServiceResponse createContainer(String id, ReleaseId releaseId) throws ClientResponseFailure {
        ClientResponse<ServiceResponse> response = null;
        try {
            ClientRequest clientRequest = new ClientRequest(baseURI+"/containers/"+id);
            response = clientRequest.body(MediaType.APPLICATION_XML_TYPE, releaseId).put(ServiceResponse.class);
            if( response.getStatus() == Response.Status.CREATED.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception creating container: "+id+" with release-id "+releaseId, e, response );
        }
    }

    public ServiceResponse disposeContainer(String id) throws ClientResponseFailure {
        ClientResponse<ServiceResponse> response = null;
        try {
            ClientRequest clientRequest = new ClientRequest(baseURI+"/containers/"+id);
            response = clientRequest.delete(ServiceResponse.class);
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception disposing container: "+id, e, response );
        }
    }

    public ServiceResponse executeCommands(String id, String payload) throws ClientResponseFailure {
        ClientResponse<ServiceResponse> response = null;
        try {
            ClientRequest clientRequest = new ClientRequest(baseURI+"/containers/"+id);
            response = clientRequest.body(MediaType.APPLICATION_XML_TYPE, payload).post(ServiceResponse.class);
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception executing commands on container "+id, e, response );
        }
    }

    public List<ServiceResponse> executeScript(CommandScript script) throws ClientResponseFailure {
        ClientResponse<List<ServiceResponse>> response = null;
        try {
            ClientRequest clientRequest = new ClientRequest(baseURI);
            response = clientRequest.body(MediaType.APPLICATION_XML_TYPE, script).post(new GenericType<List<ServiceResponse>>() {});
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception retrieving server info.", e, response );
        }
    }
}
