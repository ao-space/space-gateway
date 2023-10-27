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
}
