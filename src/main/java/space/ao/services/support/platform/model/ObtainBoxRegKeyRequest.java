package space.ao.services.support.platform.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;
@RegisterForReflection
public class ObtainBoxRegKeyRequest  {

    // Field for Box UUID
    private String boxUUID;

    // Field for Service IDs
    private List<String> serviceIds;

    // Field for Sign
    private String sign;


    public String getBoxUUID() {
        return this.boxUUID;
    }

    public void setBoxUUID(String boxUUID) {
        if (boxUUID == null || boxUUID.trim().isEmpty()) {
            throw new IllegalArgumentException("Box UUID cannot be null or empty.");
        }
        this.boxUUID = boxUUID;

    }

    public List<String> getServiceIds() {
        return this.serviceIds;
    }

    public void setServiceIds(List<String> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            throw new IllegalArgumentException("Service IDs cannot be null or empty.");
        }
        this.serviceIds = serviceIds;

    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}

