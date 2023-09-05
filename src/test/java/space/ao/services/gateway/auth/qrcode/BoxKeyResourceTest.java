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

package space.ao.services.gateway.auth.qrcode;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.ao.services.account.member.dto.Const;
import space.ao.services.gateway.auth.qrcode.dto.*;
import space.ao.services.support.test.TestUtils;
import space.ao.services.account.member.respository.BoxInfoRepository;
import space.ao.services.account.member.service.MemberManageService;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.gateway.CallRequest;
import space.ao.services.gateway.RealCallRequest;
import space.ao.services.gateway.RealCallResult;
import space.ao.services.gateway.auth.CreateTokenInfo;
import space.ao.services.gateway.auth.CreateTokenResult;
import space.ao.services.gateway.auth.RefreshTokenInfo;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.TokenUtils;
import space.ao.services.support.limit.LimitReqInterceptorUtils;
import space.ao.services.support.log.Logged;
import space.ao.services.support.model.AccessToken;
import space.ao.services.support.security.SecurityUtils;

import jakarta.inject.Inject;

import java.time.ZonedDateTime;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static space.ao.services.support.RestConfiguration.REQUEST_ID;

@QuarkusTest
class BoxKeyResourceTest {

  @Inject
  OperationUtils utils;
  @Inject
  SecurityUtils securityUtils;
  @Inject
  TokenUtils tokenUtils;

  @Inject
  TestUtils testUtils;
  Integer userid;

  @Getter
  public UserEntity adminInfo;
  @Inject
  MemberManageService memberManageService;
  @Inject
  BoxInfoRepository boxInfoRepository;

  @Inject
  LimitReqInterceptorUtils limitReqInterceptorUtils;
  String clientUUID;

  @BeforeEach
  void setUp() {
    boxInfoRepository.insertOrUpdate("boxregkey-1", utils.string2SHA256("000000"));
    testUtils.createAdmin();
    securityUtils.loadAdminClientPublicFile();
  }

  @AfterEach
  void tearDown() {
    testUtils.cleanData();
    limitReqInterceptorUtils.resetCounter("ACREQRATE-/v1/api/auth/bkey/verify-" + clientUUID);
  }

  @Test
  void testCreateAuthCode() {

    // 成员端
    var requestId = utils.createRandomType4UUID();
    final CreateTokenInfo info = testUtils.getAdminCreateTokenTestInfo(requestId);
    final Response tokenResp = given()
            .header(REQUEST_ID, requestId)
            .body(info)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/gateway/auth/token/create");
    final CreateTokenResult tokenRet = tokenResp.body().as(CreateTokenResult.class);

    final CreateAuthCodeInfo createAuthCodeInfo = getCreateAuthCodeInfo(requestId,
            tokenRet.getAccessToken());

    final Response authResp = given()
            .header(REQUEST_ID, requestId)
            .body(createAuthCodeInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/bkey/create");

    CreateAuthCodeResult authCodeResult = authResp.body().as(CreateAuthCodeResult.class);

    AccessToken ak = Objects.requireNonNull(tokenUtils.verifyAccessToken(requestId, tokenRet.getAccessToken()));
    String authCode = securityUtils.decryptWithSecret(
            authCodeResult.authCodeInfo().getAuthCode(), ak.getSharedSecret(),
            ak.getSharedInitializationVector());
    String tmpEncryptedSecret = "1234567890123456";
    String bkey = securityUtils.decryptWithSecret(
            authCodeResult.authCodeInfo().getBkey(), ak.getSharedSecret(),
            ak.getSharedInitializationVector());

    // 被授权端
    var clientUUID2 = utils.createRandomType4UUID();
    final EncryptAuthInfo encryptAuthInfo = new EncryptAuthInfo(null, null,
            securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(requestId, bkey),
            securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(requestId, tmpEncryptedSecret),
            testUtils.encryptUsingBoxPublicKey(requestId, String.valueOf(authCode)),
            clientUUID2, "web", null);

    final Response verityResp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .header("X-Forwarded-For", "123.169.206.200, 127.0.0.1")
            .body(encryptAuthInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/bkey/verify");

    EncryptAuthResult encryptAuthResult = verityResp.body().as(EncryptAuthResult.class);

    given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .queryParam("bkey", bkey)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/bkey/poll")
            .then()
            .body(containsString("requestId"), containsString("true"));

    RefreshTokenInfo refreshTokenInfo = new RefreshTokenInfo(
            encryptAuthResult.encryptAuthResult().refreshToken());
    given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .queryParam("tmpEncryptedSecret",
                    securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(requestId, utils.createRandomNumbers(16)))
            .body(refreshTokenInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/bkey/refresh")
            .then()
            .body(containsString("encryptedSecret"));

    RealCallRequest real = new RealCallRequest();
    {
      real.setServiceName("test-service");
      real.setApiName("greeting");
      real.setApiVersion("v1");
    }

    var encryptAuthDTO = encryptAuthResult.encryptAuthResult();
    ak = Objects.requireNonNull(tokenUtils.verifyAccessToken(requestId, encryptAuthDTO.accessToken()));

    CallRequest call = new CallRequest(securityUtils.encryptWithSecret(
            utils.objectToJson(real), ak.getSharedSecret(), ak.getSharedInitializationVector()), ak.getToken());

    final Response callResp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(call)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/gateway/call");

    final RealCallResult callRet = callResp.body().as(RealCallResult.class);
    String greeting = securityUtils.decryptWithSecret(callRet.body(), ak.getSharedSecret(),
            ak.getSharedInitializationVector());
    assertEquals("Hello", greeting);

  }

  @Test
  void testAuthKeyNotMatch() {
    var requestId = utils.createRandomType4UUID();
    final CreateTokenInfo info = testUtils.getAdminCreateTokenTestInfo(requestId);
    final Response tokenResp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(info)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/gateway/auth/token/create");
    final CreateTokenResult tokenRet = tokenResp.body().as(CreateTokenResult.class);

    final CreateAuthCodeInfo createAuthCodeInfo = getCreateAuthCodeInfo(requestId,
            tokenRet.getAccessToken());
    final AccessToken ak = tokenUtils.verifyAccessToken(requestId, tokenRet.getAccessToken());
    assertNotNull(ak);
    createAuthCodeInfo.setAuthKey(
            securityUtils.encryptWithSecret("r2VTgYrvX2ujIsAddnsHD7H3xMPHFp8w", ak.getSharedSecret(),
                    ak.getSharedInitializationVector()));

    given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(createAuthCodeInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/bkey/create")
            .then()
            .body(containsString("auth key was not matched"));
  }

  @Test
  void testNotAutoLogin() {
    var requestId = utils.createRandomType4UUID();
    // 绑定端 创建授权码
    var createTokenResult = getCreateTokenResult(userid);
    var createAuthCodeResult = getCreateAuthCodeResult(requestId, createTokenResult);
    var encryptAuthInfo = getEncryptAuthInfo(createAuthCodeResult, createTokenResult);

    // 不允许 自动登录
    given()
            .header(REQUEST_ID, requestId)
            .queryParam("bkey", securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, encryptAuthInfo.getBkey()))
            .queryParam("autoLogin", false)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/bkey/poll")
            .then()
            .body(containsString("requestId"), containsString("false"));

    // 输入授权码
    final Response verityResp = given()
            .header(REQUEST_ID, requestId)
            .header("X-Forwarded-For", "123.169.206.200, 127.0.0.1")
            .body(encryptAuthInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/bkey/verify");

    // 得到授权
    var encryptAuthResult = verityResp.body().as(EncryptAuthResult.class);
    Assertions.assertFalse(encryptAuthResult.encryptAuthResult().autoLogin());
    Assertions.assertTrue(
            ZonedDateTime.parse(encryptAuthResult.encryptAuthResult().autoLoginExpiresAt())
                    .isBefore(ZonedDateTime.now().plusDays(1)));
    var access = tokenUtils.verifyAccessToken(requestId, encryptAuthResult.encryptAuthResult().accessToken());
    // 下线
    assert access != null;
    given().when().header("Request-Id", utils.createRandomType4UUID())
            .contentType(ContentType.JSON)
            .queryParam("aoid", "aoid-" + access.getUserId())
            .queryParam("userId", 1)
            .queryParam("clientUUID", access.getClientUUID())
            .queryParam("AccessToken-clientUUID", "")
            .delete("/v1/api/terminal/info/delete")
            .then()
            .body(containsString("ACC-200"));
    // 自动登录接口 - 免扫码登录
    var authorizedTerminalLoginInfo = new AuthorizedTerminalLoginInfo();
    authorizedTerminalLoginInfo.setRefreshToken(
            encryptAuthResult.encryptAuthResult().refreshToken());
    authorizedTerminalLoginInfo.setTmpEncryptedSecret(encryptAuthInfo.getTmpEncryptedSecret());
    given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(authorizedTerminalLoginInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/auto/login/poll")
            .then()
            .statusCode(200)
            .body(containsString("GW-4044"));
  }

  @Test
  void testNotScanningCodeLogin() {
    var requestId = utils.createRandomType4UUID();

    // 绑定端 创建授权码
    var createTokenResult = getCreateTokenResult(requestId);
    var createAuthCodeResult = getCreateAuthCodeResult(requestId, createTokenResult);
    var encryptAuthInfo = getEncryptAuthInfo(createAuthCodeResult, createTokenResult);

    // 不允许 自动登录
    given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .queryParam("bkey", securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, encryptAuthInfo.getBkey()))
            .queryParam("autoLogin", false)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/bkey/poll")
            .then()
            .body(containsString("requestId"), containsString("false"));

    // 输入授权码
    final Response verityResp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .header("X-Forwarded-For", "123.169.206.200, 127.0.0.1")
            .body(encryptAuthInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/bkey/verify");

    // 得到授权
    var encryptAuthResult = verityResp.body().as(EncryptAuthResult.class);
    Assertions.assertFalse(encryptAuthResult.encryptAuthResult().autoLogin());
    Assertions.assertTrue(
            ZonedDateTime.parse(encryptAuthResult.encryptAuthResult().autoLoginExpiresAt())
                    .isBefore(ZonedDateTime.now().plusDays(1)));

    // 自动登录接口 - 免扫码登录
    var authorizedTerminalLoginInfo = new AuthorizedTerminalLoginInfo();
    authorizedTerminalLoginInfo.setRefreshToken(
            encryptAuthResult.encryptAuthResult().refreshToken());
    authorizedTerminalLoginInfo.setTmpEncryptedSecret(encryptAuthInfo.getTmpEncryptedSecret());
    var access = tokenUtils.verifyAccessToken(requestId, encryptAuthResult.encryptAuthResult().accessToken());
    // 下线
    assert access != null;
    given().when().header("Request-Id", utils.createRandomType4UUID())
            .contentType(ContentType.JSON)
            .queryParam("aoid", "aoid-" + access.getUserId())
            .queryParam("userId", 1)
            .queryParam("clientUUID", access.getClientUUID())
            .queryParam("AccessToken-clientUUID", "")
            .delete("/v1/api/terminal/info/delete")
            .then()
            .body(containsString("ACC-200"));

    given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(authorizedTerminalLoginInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/auto/login/poll")
            .then()
            .statusCode(200)
            .body(containsString("GW-4044"));

    AccessToken accessToken = tokenUtils.verifyAccessToken(requestId, createTokenResult.getAccessToken());
    assert accessToken != null;
    AuthorizedTerminalLoginConfirmInfo authorizedTerminalLoginConfirmInfo = new AuthorizedTerminalLoginConfirmInfo(
            createTokenResult.getAccessToken(),
            securityUtils.encryptWithSecret(encryptAuthInfo.getClientUUID(), accessToken.getSharedSecret(),
                    accessToken.getSharedInitializationVector()), true, false
    );
    given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(authorizedTerminalLoginConfirmInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/auto/login/confirm")
            .then()
            .statusCode(200)
            .body(containsString("GW-200"));

    given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(authorizedTerminalLoginInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/auto/login/poll")
            .then()
            .statusCode(200)
            .body(containsString("GW-200"));

  }

  @Test
  void testAutoLogin() {
    var requestId = utils.createRandomType4UUID();

    // 绑定端 创建授权码
    var createTokenResult = getCreateTokenResult(requestId);
    var createAuthCodeResult = getCreateAuthCodeResult(requestId, createTokenResult);
    var encryptAuthInfo = getEncryptAuthInfo(createAuthCodeResult, createTokenResult);

    // 允许 自动登录
    given()
            .header(REQUEST_ID, requestId)
            .queryParam("bkey", securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, encryptAuthInfo.getBkey()))
            .queryParam("autoLogin", true)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/bkey/poll")
            .then()
            .body(containsString("requestId"), containsString("false"));

    // 输入授权码
    final Response verityResp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .header("X-Forwarded-For", "123.169.206.200, 127.0.0.1")
            .body(encryptAuthInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/bkey/verify");

    // 得到授权
    var encryptAuthResult = verityResp.body().as(EncryptAuthResult.class);
    Assertions.assertTrue(encryptAuthResult.encryptAuthResult().autoLogin());
    Assertions.assertTrue(
            ZonedDateTime.parse(encryptAuthResult.encryptAuthResult().autoLoginExpiresAt())
                    .isAfter(ZonedDateTime.now().plusDays(14)));

    // 自动登录接口
    var authorizedTerminalLoginInfo = new AuthorizedTerminalLoginInfo();
    authorizedTerminalLoginInfo.setRefreshToken(
            encryptAuthResult.encryptAuthResult().refreshToken());
    authorizedTerminalLoginInfo.setTmpEncryptedSecret(encryptAuthInfo.getTmpEncryptedSecret());

    given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(authorizedTerminalLoginInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/auto/login")
            .then()
            .statusCode(200)
            .body(containsString("GW-200"));


  }

  @Test
  void testLoginFailedCounter() {
    var requestId = utils.createRandomType4UUID();
    // 绑定端 创建授权码
    var createTokenResult = getCreateTokenResult(requestId);
    var createAuthCodeResult = getCreateAuthCodeResult(requestId, createTokenResult);
    var encryptAuthInfo = getEncryptAuthInfo(createAuthCodeResult, createTokenResult);

    // 不允许 自动登录
    given()
            .header(REQUEST_ID, requestId)
            .queryParam("bkey", securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, encryptAuthInfo.getBkey()))
            .queryParam("autoLogin", false)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/bkey/poll")
            .then()
            .body(containsString("requestId"), containsString("false"));

    // 输入授权码
    final Response verityResp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .header("X-Forwarded-For", "123.169.206.200, 127.0.0.1")
            .body(encryptAuthInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/bkey/verify");

    // 得到授权
    var encryptAuthResult = verityResp.body().as(EncryptAuthResult.class);
    Assertions.assertFalse(encryptAuthResult.encryptAuthResult().autoLogin());
    Assertions.assertTrue(
            ZonedDateTime.parse(encryptAuthResult.encryptAuthResult().autoLoginExpiresAt())
                    .isBefore(ZonedDateTime.now().plusDays(1)));

    // 自动登录接口 - 免扫码登录
    var authorizedTerminalLoginInfo = new AuthorizedTerminalLoginInfo();
    authorizedTerminalLoginInfo.setRefreshToken(
            encryptAuthResult.encryptAuthResult().refreshToken());
    authorizedTerminalLoginInfo.setTmpEncryptedSecret(encryptAuthInfo.getTmpEncryptedSecret());
    var access = tokenUtils.verifyAccessToken(requestId, encryptAuthResult.encryptAuthResult().accessToken());
    // 下线
    assert access != null;
    given().when().header("Request-Id", utils.createRandomType4UUID())
            .contentType(ContentType.JSON)
            .queryParam("aoid", "aoid-" + access.getUserId())
            .queryParam("userId", 1)
            .queryParam("clientUUID", access.getClientUUID())
            .queryParam("AccessToken-clientUUID", "")
            .delete("/v1/api/terminal/info/delete")
            .then()
            .body(containsString("ACC-200"));

    // 自动登录接口
    given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(authorizedTerminalLoginInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/auto/login")
            .then()
            .statusCode(200)
            .body(containsString("GW-4044"));

    given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(authorizedTerminalLoginInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/auto/login")
            .then()
            .statusCode(200)
            .body(containsString("GW-4044"));
    given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(authorizedTerminalLoginInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/auto/login")
            .then()
            .statusCode(200)
            .body(containsString("GW-4044"));

    given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(authorizedTerminalLoginInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/auto/login")
            .then()
            .statusCode(200)
            .body(containsString("GW-4045"));
  }

  @Logged
  public CreateTokenResult getCreateTokenResult(String requestId) {
    final CreateTokenInfo info = testUtils.getAdminCreateTokenTestInfo(requestId);
    final Response tokenResp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(info)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/gateway/auth/token/create");
    return tokenResp.body().as(CreateTokenResult.class);
  }

  private CreateTokenResult getCreateTokenResult(Integer userid) {
    final CreateTokenInfo info = testUtils.getAdminCreateTokenTestInfo(String.valueOf(userid));
    final Response tokenResp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(info)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/gateway/auth/token/create");
    return tokenResp.body().as(CreateTokenResult.class);
  }

  private CreateAuthCodeResult getCreateAuthCodeResult(String requestId, CreateTokenResult tokenRet) {

    final CreateAuthCodeInfo createAuthCodeInfo = getCreateAuthCodeInfo(requestId,
            tokenRet.getAccessToken());

    final Response authResp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(createAuthCodeInfo)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/auth/bkey/create");

    return authResp.body().as(CreateAuthCodeResult.class);
  }

  private EncryptAuthInfo getEncryptAuthInfo(CreateAuthCodeResult createAuthCodeResult,
                                             CreateTokenResult createTokenResult) {
    var requestId = utils.createRandomType4UUID();

    AccessToken ak = Objects.requireNonNull(
            tokenUtils.verifyAccessToken(requestId, createTokenResult.getAccessToken()));
    String authCode = securityUtils.decryptWithSecret(
            createAuthCodeResult.authCodeInfo().getAuthCode(), ak.getSharedSecret(),
            ak.getSharedInitializationVector());
    String tmpEncryptedSecret = "1234567890123456";
    String bkey = securityUtils.decryptWithSecret(
            createAuthCodeResult.authCodeInfo().getBkey(), ak.getSharedSecret(),
            ak.getSharedInitializationVector());

    return new EncryptAuthInfo(null, null,
            securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(requestId, bkey),
            securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(requestId, tmpEncryptedSecret),
            securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(requestId, authCode),
            utils.createRandomType4UUID(), "web", null);
  }

  public CreateAuthCodeInfo getCreateAuthCodeInfo(String requestId, String accessToken) {
    adminInfo = memberManageService.findByUserId(Const.Admin.ADMIN_ID);

    final AccessToken ak = Objects.requireNonNull(tokenUtils.verifyAccessToken(requestId, accessToken));
    CreateAuthCodeInfo info = new CreateAuthCodeInfo();
    {
      info.setAccessToken(accessToken);
      info.setAuthKey(securityUtils.encryptWithSecret(
              adminInfo.getAuthKey(), ak.getSharedSecret(), ak.getSharedInitializationVector()));
      info.setClientUUID(securityUtils.encryptWithSecret(
              adminInfo.getClientUUID(), ak.getSharedSecret(), ak.getSharedInitializationVector()));
      info.setBoxName(securityUtils.encryptWithSecret(
              "boxName-abc", ak.getSharedSecret(), ak.getSharedInitializationVector()));
      info.setBoxUUID(securityUtils.encryptWithSecret(
              "boxUUID-abc", ak.getSharedSecret(), ak.getSharedInitializationVector()));
    }
    return info;
  }
}
