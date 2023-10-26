package space.ao.services.support.platform;

import io.github.ren2003u.authentication.model.ObtainBoxRegKeyResponse;
import io.github.ren2003u.client.Client;
import io.github.ren2003u.domain.errorHandle.ApiResponse;
import io.github.ren2003u.register.model.RegisterClientResponse;
import io.github.ren2003u.register.model.RegisterUserResponse;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.platform.info.registry.ClientRegistryInfo;
import space.ao.services.support.platform.info.registry.ClientRegistryResult;
import space.ao.services.support.platform.info.registry.UserRegistryInfo;
import space.ao.services.support.platform.info.registry.UserRegistryResult;
import space.ao.services.support.platform.info.token.TokenVerifySignInfo;
import space.ao.services.support.security.SecurityUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@ApplicationScoped
public class PlatformClient {

    @Inject
    ApplicationProperties properties;

    @Inject
    SecurityUtils securityUtils;

    @Inject
    OperationUtils utils;
    private String host;
    private Client client;

    private void init() {
        if (this.client == null) {
            this.host = properties.ssplatformUrl();
            this.client = new Client(host, null);
        }
    }
    private static final Logger LOG = LoggerFactory.getLogger(PlatformClient.class);

    // Cache variables
    private String cachedBoxRegKey = null;
    private LocalDateTime lastFetchedTime = null;
    private static final Duration CACHE_DURATION = Duration.ofMinutes(5); // Cache duration of 5 minutes

//    public PlatformClient() {
//        this.client = new Client(host, null);
//    }

    public UserRegistryResult registerUser(String requestId, UserRegistryInfo userRegistryInfo) {
        init();
        try {
            LOG.info("requestId:{}",requestId);
            LOG.info("UserRegistryInfo:{}",userRegistryInfo);
            // Obtain BoxRegKey
            String boxRegKey = obtainBoxRegKey(requestId);
            if (boxRegKey == null) {
                LOG.error("Failed to obtain BoxRegKey for requestId: {}", requestId);
                return null;
            }

            // Register User
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
    public ClientRegistryResult registerClient(String requestId, ClientRegistryInfo clientRegistryInfo, String userId) {
        init();
        try {
            // Obtain BoxRegKey
            String boxRegKey = obtainBoxRegKey(requestId);
            if (boxRegKey == null) {
                LOG.error("Failed to obtain BoxRegKey for requestId: {}", requestId);
                return null;
            }

            // Register Client
            ApiResponse<RegisterClientResponse> response = client.registerClient(properties.boxUuid(), userId, clientRegistryInfo.clientUUID(), clientRegistryInfo.clientType(), requestId, boxRegKey);
            if (response.getError() != null) {
                LOG.error("Error registering client: {}", response.getError().getMessage());
                return null;
            }
            return new ClientRegistryResult(response.getData().getBoxUUID(), response.getData().getUserId(), response.getData().getClientUUID(), response.getData().getClientType());
        } catch (Exception e) {
            LOG.error("Failed to register client", e);
            return null;
        }
    }

    private String obtainBoxRegKey(String requestId) {
        init();
        try {
            // Check if the cache is still valid
            if (cachedBoxRegKey != null && lastFetchedTime != null && Duration.between(lastFetchedTime, LocalDateTime.now()).compareTo(CACHE_DURATION) <= 0) {
                return cachedBoxRegKey;
            }
            var sign = securityUtils.getSecurityProvider().signUsingBoxPrivateKey(requestId,
                    Base64.getEncoder().encodeToString(
                            utils.objectToJson(TokenVerifySignInfo.of(properties.boxUuid(), List.of("10001")))
                                    .getBytes(StandardCharsets.UTF_8)));
            ApiResponse<ObtainBoxRegKeyResponse> response = client.obtainBoxRegKey(properties.boxUuid(), List.of("10001"), requestId,sign);
            if (response.getError() != null) {
                LOG.error("Error obtaining BoxRegKey: {}", response.getError().getMessage());
                return null;
            }

            // Update cache
            cachedBoxRegKey = response.getData().getTokenResults().get(0).getBoxRegKey();
            lastFetchedTime = LocalDateTime.now();

            return cachedBoxRegKey;
        } catch (Exception e) {
            LOG.error("Failed to obtain BoxRegKey", e);
            return null;
        }
    }
    public void deleteUser(String requestId, String userId) {
        init();
        try {
            // Obtain BoxRegKey
            String boxRegKey = obtainBoxRegKey(requestId);
            if (boxRegKey == null) {
                LOG.error("Failed to obtain BoxRegKey for requestId: {}", requestId);
                return;
            }

            // Delete User
            client.deleteUser(properties.boxUuid(), userId, requestId, boxRegKey);
        } catch (Exception e) {
            LOG.error("Failed to delete user with userId: {}", userId, e);
        }
    }
    public void deleteClient(String requestId, String userId, String clientUUID) {
        init();
        try {
            // Obtain BoxRegKey
            String boxRegKey = obtainBoxRegKey(requestId);
            if (boxRegKey == null) {
                LOG.error("Failed to obtain BoxRegKey for requestId: {}", requestId);
                return;
            }

            // Delete Client
            client.deleteClient(properties.boxUuid(), userId, clientUUID, requestId, boxRegKey);
        } catch (Exception e) {
            LOG.error("Failed to delete client with clientUUID: {}", clientUUID, e);
        }
    }
}
