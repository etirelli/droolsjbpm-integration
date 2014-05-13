package org.kie.server.api;

public class ServiceResponse {
    public static enum ResponseType {
        SUCCESS, FAILURE;
    }
    
    private final ServiceResponse.ResponseType type;
    private final String msg;
    
    public ServiceResponse(ServiceResponse.ResponseType type, String msg) {
        super();
        this.type = type;
        this.msg = msg;
    }
    
    public ServiceResponse.ResponseType getType() {
        return type;
    }
    
    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "ServiceResponse[" + type + ", msg='" + msg + "']";
    }
}