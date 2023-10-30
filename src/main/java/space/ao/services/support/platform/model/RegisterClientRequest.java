package space.ao.services.support.platform.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class RegisterClientRequest  {

    private String clientUUID;
    private String clientType;

    public String getClientUUID() {
        return this.clientUUID;
    }

    public void setClientUUID(String clientUUID) {
        if (clientUUID == null || clientUUID.trim().isEmpty()) {
            throw new IllegalArgumentException("Client UUID cannot be null or empty.");
        }
        this.clientUUID = clientUUID;
    }

    public String getClientType() {
        return this.clientType;
    }

    public void setClientType(String clientType) {
        if (clientType == null || clientType.trim().isEmpty()) {
            throw new IllegalArgumentException("Client Type cannot be null or empty.");
        }
        this.clientType = clientType;
    }
}
