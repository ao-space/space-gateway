package space.ao.services.support.platform.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
@RegisterForReflection
public class ObtainBoxRegKeyResponse  {

    private String boxUUID;
    private List<TokenResult> tokenResults = new ArrayList<>();

    // Getters and setters with encapsulation

    public String getBoxUUID() {
        return this.boxUUID;
    }

    public void setBoxUUID(String boxUUID) {
        this.boxUUID = boxUUID;
    }

    public List<TokenResult> getTokenResults() {
        return new ArrayList<>(this.tokenResults);
    }

    public void setTokenResults(List<TokenResult> tokenResults) {
        if (tokenResults != null) {
            this.tokenResults = new ArrayList<>(tokenResults);
        }
    }
    // Inner class for TokenResult
    public static class TokenResult {
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
}
