/*
 * Copyright (c) 2022 Institute of Software Chinese Academy of Sciences (ISCAS)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.ao.services.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.gateway.auth.CreateTokenResult;
import space.ao.services.support.jwt.JwtUtils;
import space.ao.services.support.log.Logged;
import space.ao.services.support.model.AccessToken;
import space.ao.services.support.model.AlgorithmConfig;
import space.ao.services.support.model.RefreshToken;
import space.ao.services.support.redis.RedisTokenService;
import space.ao.services.support.security.SecurityUtils;
import space.ao.services.support.service.ServiceError;
import space.ao.services.support.service.ServiceOperationException;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Singleton
public class TokenUtils {
  private static final Logger LOG = Logger.getLogger("app.log");
  private static final String USER_ID = "userId";
  private static final String CLIENT_UUID = "clientUUID";
  private static final String TOKEN_TYPE_KEY = "tokenType";
  private static final String ACCESS_TOKEN_TYPE = "access";
  private static final String REFRESH_TOKEN_TYPE = "refresh";
  private static final String INFO = "info";

  private static final String OPEN_API_ACCESS_TOKEN_TYPE = "open-api-access";
  private static final String OPEN_API_REFRESH_TOKEN_TYPE = "open-api-refresh";
  private static final String OPEN_API_APPLET_ID = "open-api-appletID";
  private static final String OPEN_API_APPLET_VER = "open-api-appletVER";
  private static final String OPEN_API_SCOPES = "open-api-scopes";
  
  @Inject
  SecurityUtils securityUtils;
  @Inject
  ApplicationProperties properties;
  @Inject
  OperationUtils operationUtils;
  @Inject
  JwtUtils jwtUtils;
  @Inject
  RedisTokenService redisTokenService;
  @Inject
  JWTParser jwtParser;
  @Inject
  ObjectMapper objectMapper;

  private static final Map<String, AccessToken> accessTokenMap = new HashMap<>();

  @Logged
  public String createAccessToken(String requestId, String userId, ZonedDateTime expiresAt, String secret,
                                  String iv, String clientUUID, @Nullable OpenApiArg openApiArg) {

    var builder = Jwt.upn(properties.boxUserName())
            .issuer(properties.boxEndpoint())
            .issuedAt(ZonedDateTime.now().toInstant())
            .expiresAt(expiresAt.toInstant())
            .claim(INFO, securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(requestId,
                    userId + ","+ clientUUID +","+ secret))
            .claim(AccessToken.SHARED_IV_KEY, iv);

    return claimOpenApiAndSign(requestId, openApiArg, builder, ACCESS_TOKEN_TYPE, OPEN_API_ACCESS_TOKEN_TYPE);
  }


  public String createRefreshToken(String requestId, String userId, ZonedDateTime expiresAt, String clientUUID, @Nullable OpenApiArg openApiArg) {
    final JwtClaimsBuilder builder = Jwt.upn(properties.boxUserName())
            .issuer(properties.boxEndpoint())
            .issuedAt(ZonedDateTime.now().toInstant())
            .expiresAt(expiresAt.toInstant())
            .claim(USER_ID, securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(requestId, userId))
            .claim(CLIENT_UUID, securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(requestId, clientUUID))
            .claim(AccessToken.JWT_ID, operationUtils.createRandomType4UUID());

    return claimOpenApiAndSign(requestId, openApiArg, builder, REFRESH_TOKEN_TYPE, OPEN_API_REFRESH_TOKEN_TYPE);
  }

  /**
   * claim OpenApi
   * @param requestId requestId
   * @param openApiArg OpenApiArg
   * @param builder JwtClaimsBuilder
   * @param tokenType tokenType
   * @param openApiTokenType openApiTokenType
   * @return jwt token
   */
  private String claimOpenApiAndSign(String requestId, @Nullable OpenApiArg openApiArg, JwtClaimsBuilder builder, String tokenType, String openApiTokenType) {
    if (openApiArg == null) {
      builder.claim(TOKEN_TYPE_KEY, tokenType);
    } else {
      builder.claim(TOKEN_TYPE_KEY, openApiTokenType)
              .claim(OPEN_API_APPLET_ID, openApiArg.appletId)
              .claim(OPEN_API_APPLET_VER, openApiArg.appletVersion)
              .claim(OPEN_API_SCOPES, operationUtils.objectToJson(openApiArg.scopes));
    }
    return tokenSign(requestId, builder);
  }

  /**
   * token signature
   * @param requestId requestId
   * @param builder JwtClaimsBuilder
   * @return jwt token
   */
  public String tokenSign(String requestId, JwtClaimsBuilder builder){
    builder.claim(AccessToken.JWT_ID, operationUtils.createRandomType4UUID());
    var unsignedJwt = jwtUtils.privateKeySignPre(builder);
    return unsignedJwt + "." + securityUtils.getSecurityProvider().signByUrlEncodeUsingBoxPrivateKey(requestId,
            Base64.getEncoder().encodeToString(unsignedJwt.getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * Used to verify access token and returns the result or null if failed.
   */
  public @Nullable
  AccessToken verifyAccessToken(String requestId, String token) {

    if(accessTokenMap.containsKey(token)){
      return accessTokenMap.get(token);
    }

    try {
      final JsonWebToken jwt = jwtParser.verify(Objects.requireNonNull(token), securityUtils.getSecurityProvider().getBoxPublicKey(requestId));

      if (!Objects.equals(ACCESS_TOKEN_TYPE, jwt.getClaim(TOKEN_TYPE_KEY)) &&
              !Objects.equals(OPEN_API_ACCESS_TOKEN_TYPE, jwt.getClaim(TOKEN_TYPE_KEY))) {
        LOG.infov("requestId:{0}, verifyAccessToken jwt.getClaim(TOKEN_TYPE_KEY): {1}", requestId, jwt.getClaim(TOKEN_TYPE_KEY));
        return null;
      }

      AccessToken.AccessTokenBuilder builder = AccessToken.builder().token(token);

      if (ZonedDateTime.now().toInstant().getEpochSecond() > jwt.getExpirationTime()) {
        LOG.infov("requestId:{0}, verifyAccessToken jwt.getExpirationTime(): {1}", requestId, jwt.getExpirationTime());
        return null;
      } else {
        builder.expiresAt(
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(jwt.getExpirationTime()),
                        ZoneId.of("UTC")));
      }
      if (!jwt.containsClaim(INFO)) {
        LOG.infov("requestId:{0}, verifyAccessToken jwt.containsClaim(INFO): {1}", requestId, jwt.containsClaim(INFO));
        return null;
      } else {
        var info = securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, jwt.getClaim(INFO));
        builder.userId(info.split(",")[0])
                .clientUUID(info.split(",")[1])
                .sharedSecret(info.split(",")[2]);
        builder.sharedInitializationVector(
                new IvParameterSpec(Base64.getDecoder().decode(jwt.<String>getClaim(AccessToken.SHARED_IV_KEY))));
      }

      if (!jwt.getIssuer().equalsIgnoreCase(properties.boxEndpoint())) {
        LOG.infov("requestId:{0}, verifyAccessToken jwt.getIssuer(): {1}", requestId, jwt.getIssuer());
        return null;
      } else {
        builder.endpoint(jwt.getIssuer());
      }

      // verify open api relevant claims
      if (Objects.equals(OPEN_API_ACCESS_TOKEN_TYPE, jwt.getClaim(TOKEN_TYPE_KEY))) {
        builder.openApi(true);
        if (!jwt.containsClaim(OPEN_API_APPLET_ID)) {
          LOG.infov("requestId:{0}, verifyAccessToken jwt.getClaim(OPEN_API_APPLET_ID): {1}", requestId, jwt.getClaim(OPEN_API_APPLET_ID));
          return null;
        } else {
          builder.openApiAppletId(jwt.getClaim(OPEN_API_APPLET_ID));
        }

        if (!jwt.containsClaim(OPEN_API_APPLET_VER)) {
          LOG.infov("requestId:{0}, verifyAccessToken jwt.getClaim(OPEN_API_APPLET_VER): {1}", requestId, jwt.getClaim(OPEN_API_APPLET_VER));
          return null;
        } else {
          builder.openApiAppletVersion(jwt.getClaim(OPEN_API_APPLET_VER));
        }

        if (!jwt.containsClaim(OPEN_API_SCOPES)) {
          LOG.infov("requestId:{0}, verifyAccessToken jwt.getClaim(OPEN_API_SCOPES): {1}", requestId, jwt.getClaim(OPEN_API_SCOPES));
          return null;
        } else {
          final Set<String> scopes = objectMapper.readValue(jwt.<String>getClaim(OPEN_API_SCOPES), new TypeReference<>() {});
          builder.openApiScopes(scopes);
        }
      }

      var result = builder.build();
      accessTokenMap.put(token, result);
      return result;

    } catch (Exception e) {
      LOG.error("verifyAccessToken failed", e);
      return null;
    }
  }
  public AccessToken checkAccessToken(String requestId, String accessToken) {
    return Optional.ofNullable(verifyAccessToken(requestId, accessToken))
            .orElseThrow(
                    () -> new WebApplicationException("Invalid access token", Response.Status.FORBIDDEN)
            );
  }
  public RefreshToken verifyRefreshToken(String requestId, String token) {

    try {
      final JsonWebToken jwt = jwtParser.verify(token, securityUtils.getSecurityProvider().getBoxPublicKey(requestId));

      if (!Objects.equals(REFRESH_TOKEN_TYPE, jwt.getClaim(TOKEN_TYPE_KEY)) &&
              !Objects.equals(OPEN_API_REFRESH_TOKEN_TYPE, jwt.getClaim(TOKEN_TYPE_KEY))) {
        return null;
      }

      RefreshToken.RefreshTokenBuilder builder = RefreshToken.builder();

      if (ZonedDateTime.now().toInstant().getEpochSecond() > jwt.getExpirationTime()) {
        throw new ServiceOperationException(ServiceError.REFRESH_TOKEN_TIMEOUT);
      } else {
        builder.expiresAt(
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(jwt.getExpirationTime()),
                        ZoneId.of("UTC")));
      }
      String userId;
      if (jwt.getClaim(USER_ID) == null) {
        return null;
      } else {
        userId = securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, jwt.getClaim(USER_ID));
        builder.userId(userId);
      }

      if (!properties.boxEndpoint().equalsIgnoreCase(jwt.getIssuer())) {
        return null;
      } else {
        builder.endpoint(jwt.getIssuer());
      }
      if (jwt.getClaim(CLIENT_UUID) == null) {
        return null;
      } else {
        builder.clientUUID(securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, jwt.getClaim(CLIENT_UUID)));
      }

      // verify open api relevant claims
      if (Objects.equals(OPEN_API_REFRESH_TOKEN_TYPE, jwt.getClaim(TOKEN_TYPE_KEY))) {
        builder.openApi(true);
        if (!jwt.containsClaim(OPEN_API_APPLET_ID)) {
          return null;
        } else {
          builder.openApiAppletId(jwt.getClaim(OPEN_API_APPLET_ID));
        }

        if (!jwt.containsClaim(OPEN_API_APPLET_VER)) {
          return null;
        } else {
          builder.openApiAppletVersion(jwt.getClaim(OPEN_API_APPLET_VER));
        }

        if (!jwt.containsClaim(OPEN_API_SCOPES)) {
          return null;
        } else {
          final Set<String> scopes = objectMapper.readValue(jwt.<String>getClaim(OPEN_API_SCOPES), new TypeReference<>() {});
          builder.openApiScopes(scopes);
        }
      }

      return builder.build();
    } catch (Exception e) {
      LOG.error("verifyRefreshToken failed", e);
      return null;
    }
  }

  @SneakyThrows
  public Cipher createAndInitCipherWithAccessToken(AccessToken accessToken, int mode) {
    var secretKey = new SecretKeySpec(
            accessToken.getSharedSecret().getBytes(StandardCharsets.UTF_8),
            properties.gatewayAlgInfoTransportationAlgorithm()
    );
    var cipher = Cipher.getInstance(properties.gatewayAlgInfoTransportationTransformation());
    cipher.init(mode, secretKey, accessToken.getSharedInitializationVector());
    return cipher;
  }

  @Data
  @Builder
  public static class OpenApiArg {
    private String appletId;
    private String appletVersion;
    private Set<String> scopes;
  }

  SecureRandom random = new SecureRandom();

  public CreateTokenResult createDefaultTokenResult(String requestId,
                                                    @Nullable String tempEncryptedSecret, String userId, String clientUUID, @Nullable OpenApiArg openApiArg) {
    final String secret = operationUtils.unifiedRandomCharters(properties.gatewayAlgInfoTransportationKeySize());
    String sharedSecret;
    byte[] ivBytes = new byte[16];
    random.nextBytes(ivBytes);
    var iv = new IvParameterSpec(ivBytes);

    if (tempEncryptedSecret == null) {
      sharedSecret = securityUtils.encryptUsingClientPublicKey(secret);
      ivBytes = operationUtils.createRandomBytes(16);
    } else {
      String swapSecret = securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, tempEncryptedSecret);
      sharedSecret = securityUtils.encryptWithSecret(secret, swapSecret, iv);
    }

    final String initializationVector = Base64.getEncoder().encodeToString(ivBytes);
    final ZonedDateTime expiresAt = ZonedDateTime.now().plusSeconds(
            Duration.parse(properties.gatewayTimeOfAkLife()).toSeconds());
    final String ak = createAccessToken(requestId, userId, expiresAt, secret, initializationVector, clientUUID, openApiArg);
    final String rft = createRefreshToken(requestId, userId,
            ZonedDateTime.now()
                    .plusSeconds(Duration.parse(properties.gatewayTimeOfRakLife()).getSeconds()),clientUUID, openApiArg);

    final AlgorithmConfig algorithmConfig = AlgorithmConfig.of(
            properties.gatewayAlgInfoPublicKeyAlgorithm(),
            properties.gatewayAlgInfoPublicKeyKeySize(),
            properties.gatewayAlgInfoTransportationAlgorithm(),
            properties.gatewayAlgInfoTransportationKeySize(),
            properties.gatewayAlgInfoTransportationTransformation(),
            initializationVector
    );

    redisTokenService.set("aoid-" + userId, secret, checkAccessToken(requestId, ak));

    return CreateTokenResult.of(ak, rft, algorithmConfig,
            sharedSecret, expiresAt.toString(), expiresAt.toEpochSecond(), requestId);
  }

  public AlgorithmConfig createDefaultAlgorithmConfig() {

    final String initializationVector = Base64.getEncoder().encodeToString(operationUtils.createRandomBytes(16));

    return AlgorithmConfig.of(
            properties.gatewayAlgInfoPublicKeyAlgorithm(),
            properties.gatewayAlgInfoPublicKeyKeySize(),
            properties.gatewayAlgInfoTransportationAlgorithm(),
            properties.gatewayAlgInfoTransportationKeySize(),
            properties.gatewayAlgInfoTransportationTransformation(),
            initializationVector
    );
  }

  // 清理过期了的授权信息
  // 每七天执行一次
  @Scheduled(every = "{app.gateway.cron.cache-clean.clean-expired-ak}")
  @SuppressWarnings("unused") // Executing a Scheduled Task
  void cleanupCacheTokenData() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    accessTokenMap.values().forEach(
            accessToken -> accessTokenMap.entrySet().removeIf(d ->  accessToken.getExpiresAt().toEpochSecond() < ZonedDateTime.now().toEpochSecond()));
    LOG.info("regularly clean token cache completed - " + stopwatch.elapsed(TimeUnit.SECONDS));
  }
}
