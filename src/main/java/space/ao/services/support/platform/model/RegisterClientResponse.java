package space.ao.services.support.platform.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class RegisterClientResponse  {

    private String boxUUID;
    private String userId;
    private String clientUUID;
    private String clientType;

    // Getters and setters with encapsulation

    public String getBoxUUID() {
        return this.boxUUID;
    }

    public void setBoxUUID(String boxUUID) {
        this.boxUUID = boxUUID;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getClientUUID() {
        return this.clientUUID;
    }

    public void setClientUUID(String clientUUID) {
        this.clientUUID = clientUUID;
    }

    public String getClientType() {
        return this.clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }
}
