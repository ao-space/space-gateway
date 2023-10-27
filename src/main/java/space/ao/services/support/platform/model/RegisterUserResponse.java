package space.ao.services.support.platform.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class RegisterUserResponse {
    private String boxUUID;
    private String userId;
    private String userDomain;
    private String userType;
    private String clientUUID;

    // Getters and setters

    public String getBoxUUID() {
        return boxUUID;
    }

    public void setBoxUUID(String boxUUID) {
        this.boxUUID = boxUUID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserDomain() {
        return userDomain;
    }

    public void setUserDomain(String userDomain) {
        this.userDomain = userDomain;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getClientUUID() {
        return clientUUID;
    }

    public void setClientUUID(String clientUUID) {
        this.clientUUID = clientUUID;
    }


}