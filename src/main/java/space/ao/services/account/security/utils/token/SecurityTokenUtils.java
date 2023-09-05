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

package space.ao.services.account.security.utils.token;

import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import space.ao.services.support.security.SecurityUtils;
import space.ao.services.account.security.dto.SecurityTokenRes;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.TokenUtils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * token 工具类
 */
@Singleton
public class SecurityTokenUtils {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    ApplicationProperties properties;

    @Inject
    SecurityUtils securityUtils;
    @Inject
    OperationUtils operationUtils;
    @Inject
    TokenUtils tokenUtils;

    @Inject
    JWTParser jwtParser;

    private static final String TOKEN_TYPE_KEY = "tokenType";
    private static final String CLIENT_UUID_KEY = "clientUUID";


    /**
     * 创建 token
     */
    public SecurityTokenRes createWithExpiresAt(SecurityTokenType tokenType,
                                                String clientUUID,
                                                ZonedDateTime expiresAt) {
        String token = createToken(tokenType, clientUUID, expiresAt);
        return SecurityTokenRes.of(token, expiresAt.toString());
    }
    public SecurityTokenRes create(SecurityTokenType tokenType,
                                   String clientUUID) {
        final ZonedDateTime expiresAt = ZonedDateTime.now().plusSeconds(
                Duration.parse(properties.gatewayTimeOfSecurityPasswdAkLife()).toSeconds());
        String token = createToken(tokenType, clientUUID, expiresAt);
        return SecurityTokenRes.of(token, expiresAt.toString());
    }

    /**
     * 创建 jwt
     */
    public String createToken(SecurityTokenType tokenType, String clientUUID, ZonedDateTime expiresAt) {
        var builder = Jwt.upn(properties.boxUserName())
                .issuer(properties.boxEndpoint())
                .issuedAt(ZonedDateTime.now().toInstant())
                .expiresAt(expiresAt.toInstant())
                .claim(TOKEN_TYPE_KEY, securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(operationUtils.createRandomType4UUID(), String.valueOf(tokenType)))
                .claim(CLIENT_UUID_KEY, securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(operationUtils.createRandomType4UUID(), clientUUID))
                ;

        return tokenUtils.tokenSign("", builder);
    }

    public boolean verifyEmailToken(String requestId, String token, String clientUUid) {
        SecurityToken securityToken = verifySecurityToken(requestId, token,
                SecurityTokenType.TOKEN_TYPE_VERIFIED_EMAIL_TOKEN, clientUUid);
        return securityToken != null;
    }
    public boolean verifyPwdToken(String requestId, String token, String clientUUid) {
        SecurityToken securityToken = verifySecurityToken(requestId, token,
                SecurityTokenType.TOKEN_TYPE_VERIFIED_PWD_TOKEN, clientUUid);
        return securityToken != null;
    }


    /**
     * 验证 token
     * Used to verify security token and returns the result or null if failed.
     */
    public SecurityToken verifySecurityToken(String requestId, String token,
                                             SecurityTokenType tokenType,
                                             String clientUUID) {

        return doVerifySecurityToken(requestId, token,
                tokenType,
                clientUUID);
    }

    public SecurityToken doVerifySecurityToken(String requestId, String token,
                                               SecurityTokenType tokenType,
                                               String clientUUID) {

        try {
            if (Objects.isNull(token) || Objects.isNull(clientUUID)) {
                return null;
            }

            final JsonWebToken jwt = jwtParser.verify(Objects.requireNonNull(token), securityUtils.getSecurityProvider().getBoxPublicKey(requestId));

            SecurityToken.SecurityTokenBuilder builder = SecurityToken.builder();

            var tokenTypeInJwt = securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, jwt.getClaim(TOKEN_TYPE_KEY));
            if (!Objects.equals(String.valueOf(tokenType), tokenTypeInJwt)) {
                LOG.warn("tokenType="+tokenType+", tokenTypeInJwt="+tokenTypeInJwt);
                return null;
            }
            builder.tokenType(tokenType);

            var clientUUIDInJwt = securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, jwt.getClaim(CLIENT_UUID_KEY));
            if (!Objects.equals(clientUUID, clientUUIDInJwt)) {
                LOG.warn("clientUUID="+clientUUID+", clientUUIDInJwt="+clientUUIDInJwt);
                return null;
            }
            builder.clientUuid(clientUUID);

            if (!properties.boxEndpoint().equalsIgnoreCase(jwt.getIssuer())) {
                return null;
            }

            if (ZonedDateTime.now().toInstant().getEpochSecond() > jwt.getExpirationTime()) {
                LOG.warn("jwt.getExpirationTime()="+jwt.getExpirationTime());
                return null;
            }
            builder.expiredAt(ZonedDateTime.ofInstant(Instant.ofEpochSecond(jwt.getExpirationTime()),
                            ZoneId.of("UTC")));

            return builder.build();

        } catch (Exception e) {
            LOG.error("verifyAccessToken failed", e);
            return null;
        }
    }
}
