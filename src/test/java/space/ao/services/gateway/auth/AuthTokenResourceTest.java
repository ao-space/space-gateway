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

package space.ao.services.gateway.auth;

import com.google.common.io.CharStreams;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.io.IOException;
import java.util.Objects;

import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.ao.services.account.member.dto.Const;
import space.ao.services.support.security.impl.SecurityProviderByLocalImpl;
import space.ao.services.support.test.TestUtils;
import space.ao.services.account.member.respository.BoxInfoRepository;
import space.ao.services.account.member.service.MemberManageService;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.redis.RedisTokenService;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.support.security.SecurityUtils;

import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.containsString;
import static space.ao.services.support.RestConfiguration.REQUEST_ID;

@QuarkusTest
class AuthTokenResourceTest {

  @Inject
  OperationUtils utils;
  @Inject
  SecurityUtils securityUtils;
  @Inject
  TestUtils testUtils;
  @Inject
  RedisTokenService redisTokenService;
  @Inject
  BoxInfoRepository boxInfoRepository;
  @Getter
  public UserEntity adminInfo;
  @Inject
  MemberManageService memberManageService;
  @Inject
  SecurityProviderByLocalImpl securityProviderByLocal;
  @Inject
  ApplicationProperties properties;
  @BeforeEach
  void setUp() {
    boxInfoRepository.insertOrUpdate("boxregkey-1", utils.string2SHA256("123456"));
    testUtils.createAdmin();
  }

  @AfterEach
  void tearDown() {
    testUtils.cleanData();
  }

  @Test
  void testCreateNormalCaseOk() {
    var requestId = utils.createRandomType4UUID();
    final CreateTokenInfo info = testUtils.getAdminCreateTokenTestInfo(requestId);
    given()
        .header(REQUEST_ID, requestId)
        .body(info)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/gateway/auth/token/create")
        .then()
        .statusCode(OK.getStatusCode())
        .header("Cache-Control", "no-store")
        .header("Pragma", "no-cache")
        .body(containsString("accessToken"),
            containsString("refreshToken"),
            containsString("AES"));
  }

  @Test
  void testRefreshNormalCaseOk() {
    final CreateTokenInfo info = testUtils.getAdminCreateTokenTestInfo(utils.createRandomType4UUID());
    final Response tokenResp = given()
        .header(REQUEST_ID, utils.createRandomType4UUID())
        .body(info)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/gateway/auth/token/create");
    final CreateTokenResult tokenRet = tokenResp.body().as(CreateTokenResult.class);

    RefreshTokenInfo refreshTokenInfo = new RefreshTokenInfo();
    {
      refreshTokenInfo.setRefreshToken(tokenRet.getRefreshToken());
    }

    given()
        .header(REQUEST_ID, utils.createRandomType4UUID())
        .body(refreshTokenInfo)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/gateway/auth/token/refresh")
        .then()
        .statusCode(OK.getStatusCode())
        .header("Cache-Control", "no-store")
        .header("Pragma", "no-cache")
        .body(containsString("accessToken"),
            containsString("refreshToken"),
            containsString("AES"));
  }

  @Test
  void testVerifyNormalCaseOk() {
    final CreateTokenInfo info = testUtils.getAdminCreateTokenTestInfo(utils.createRandomType4UUID());
    final Response tokenResp = given()
        .header(REQUEST_ID, utils.createRandomType4UUID())
        .body(info)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/gateway/auth/token/create");
    final CreateTokenResult tokenRet = tokenResp.body().as(CreateTokenResult.class);


    var secret = testUtils.decryptUsingClientPrivateKey(tokenRet.getEncryptedSecret());
    var token = redisTokenService.get("aoid-1", secret);
    System.out.println(token);
    Assertions.assertTrue(Objects.nonNull(token));
    given()
        .header(REQUEST_ID, utils.createRandomType4UUID())
        .param("access-token", tokenRet.getAccessToken())
        .when()
        .get("/v1/api/gateway/auth/token/verify")
        .then()
        .statusCode(200)
        .body(containsString("true"));
  }

  @Test
  void testRevokeNormalCaseOk() {
    Log.info(boxInfoRepository.findAll().list().toString());
    var requestId = utils.createRandomType4UUID();
    final RevokeClientInfo revokeClientInfo = getRevokeClientTestInfo(requestId);
    given()
        .header(REQUEST_ID, utils.createRandomType4UUID())
        .body(revokeClientInfo)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/gateway/auth/revoke")
        .then()
        .statusCode(200)
        .body(containsString("true"));
  }

  @Test
  void testGetPublicKey() throws IOException {

    final Response resultResp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .contentType(ContentType.JSON)
            .when()
            .get("/v1/api/gateway/auth/public-key");
    final var result = resultResp.body().as(ResponseBase.class);

    var reader = securityProviderByLocal.getFileStreamReader(properties.boxPublicKeyLocation());
    final String key = CharStreams.toString(reader);
    final String pem = key.replaceAll("[\\n\\r]", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "");
    Assertions.assertEquals(pem, result.results());
  }

  public RevokeClientInfo getRevokeClientTestInfo(String requestId) {
    adminInfo = memberManageService.findByUserId(Const.Admin.ADMIN_ID);

    RevokeClientInfo info = new RevokeClientInfo();
    {
      info.setEncryptedAuthKey(securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(requestId, adminInfo.getAuthKey()));
      info.setEncryptedClientUUID(securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(requestId, adminInfo.getClientUUID()));
      info.setEncryptedPasscode(securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(requestId, "123456"));
    }
    return info;
  }

}