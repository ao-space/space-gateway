package space.ao.services.support.platform;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.authentication.model.ObtainBoxRegKeyResponse;
import org.example.client.Client;
import org.example.domain.errorHandle.ApiResponse;
import org.example.register.model.RegisterUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.platform.info.registry.UserRegistryInfo;
import space.ao.services.support.platform.info.registry.UserRegistryResult;

import java.util.List;

@ApplicationScoped
public class PlatformClient {

    @Inject
    ApplicationProperties properties;

    private final String host = properties.ssplatformUrl();
    private final Client client;
    private static final Logger LOG = LoggerFactory.getLogger(PlatformClient.class);

    public PlatformClient() {
        this.client = new Client(host, null);
    }

    public String obtainBoxRegKey(String requestId) {
        try {
            ApiResponse<ObtainBoxRegKeyResponse> response = client.obtainBoxRegKey(properties.boxUuid(), List.of("10001"), requestId);
            if (response.getError() != null) {
                LOG.error("Error obtaining BoxRegKey: {}", response.getError().getMessage());
                return null;
            }
            return response.getData().getTokenResults().get(0).getBoxRegKey();
        } catch (Exception e) {
            LOG.error("Failed to obtain BoxRegKey", e);
            return null;
        }
    }

    public UserRegistryResult registerUser(String requestId, UserRegistryInfo userRegistryInfo, String boxRegKey) {
        try {
            ApiResponse<RegisterUserResponse> response = client.registerUser(properties.boxUuid(), userRegistryInfo.userId(), userRegistryInfo.subdomain(), userRegistryInfo.userType(), userRegistryInfo.clientUUID(), requestId, boxRegKey);
            if (response.getError() != null) {
                LOG.error("Error registering user: {}", response.getError().getMessage());
                return null;
            }
            return new UserRegistryResult(response.getData().getBoxUUID(), response.getData().getUserId(), response.getData().getUserDomain(), response.getData().getUserType(), response.getData().getClientUUID());
        } catch (Exception e) {
            LOG.error("Failed to register user", e);
            return null;
        }
    }
}
