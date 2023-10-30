package space.ao.services.support.platform.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class RegisterUserRequest {
    private String boxUUID;
    private String userId;
    private String subdomain;
    private String userType;
    private String clientUUID;


    public String getBoxUUID() {
        return this.boxUUID;
    }

    public void setBoxUUID(String boxUUID) {
        if (boxUUID == null || boxUUID.trim().isEmpty()) {
            throw new IllegalArgumentException("Box UUID cannot be null or empty.");
        }
        this.boxUUID = boxUUID;

    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }
        this.userId = userId;

    }

    public String getSubdomain() {
        return this.subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    public String getUserType() {
        return this.userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getClientUUID() {
        return this.clientUUID;
    }

    public void setClientUUID(String clientUUID) {
        this.clientUUID = clientUUID;
    }

}
