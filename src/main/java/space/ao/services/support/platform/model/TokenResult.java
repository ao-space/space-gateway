package space.ao.services.support.platform.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.OffsetDateTime;
@RegisterForReflection
public class TokenResult {
    private String serviceId;
    private String boxRegKey;
    private OffsetDateTime expiresAt;

    // getters and setters

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getBoxRegKey() {
        return boxRegKey;
    }

    public void setBoxRegKey(String boxRegKey) {
        this.boxRegKey = boxRegKey;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}