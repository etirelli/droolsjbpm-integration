package org.kie.server.api;

public class ServiceResponse {
    public static enum ResponseType {
        SUCCESS, FAILURE;
    }
    
    private ServiceResponse.ResponseType type;
    private String msg;
    
    public ServiceResponse() {
    }
    
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
    
    public void setType(ServiceResponse.ResponseType type) {
        this.type = type;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ServiceResponse[" + type + ", msg='" + msg + "']";
    }
}