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

package space.ao.services.account.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Objects;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.gateway.RealCallRequest;
import space.ao.services.gateway.auth.qrcode.dto.AuthorizedTerminalLoginInfo;
import space.ao.services.gateway.auth.CreateTokenInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.ao.services.gateway.auth.CreateTokenResult;
import static space.ao.services.support.RestConfiguration.REQUEST_ID;
import space.ao.services.account.member.respository.BoxInfoRepository;
import space.ao.services.gateway.auth.qrcode.dto.EncryptAuthDTO;
import space.ao.services.gateway.auth.qrcode.dto.EncryptAuthInfo;
import space.ao.services.gateway.auth.qrcode.dto.TotpAuthCode;
import space.ao.services.gateway.auth.qrcode.dto.v2.BkeyInfo;
import space.ao.services.gateway.auth.qrcode.dto.v2.CreateAuthCodeResult;
import space.ao.services.push.services.RedisService;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.TokenUtils;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.support.security.SecurityUtils;
import space.ao.services.support.test.TestUtils;

@QuarkusTest
class AuthorizedTerminalInfoResourceTest {
  @Inject
  OperationUtils utils;

  @Inject
  SecurityUtils securityUtils;
  @Inject
  BoxInfoRepository boxInfoRepository;
  @Inject
  TestUtils testUtils;
  @Inject
  TokenUtils tokenUtils;
  UserEntity userEntity;
  @BeforeEach
  @Transactional
  void setUp() {
    userEntity = testUtils.createAdmin();
    boxInfoRepository.insertOrUpdate("boxregkey-1", utils.string2SHA256("000000"));
  }


  @AfterEach
  void tearDown() {
    testUtils.deleteAllUser();
  }

  @Test
  void getTerminalInfoCaseOk(){
    String clientUUID = userEntity.getClientUUID();
    String userId = userEntity.getId().toString();
    CreateTokenInfo info = new CreateTokenInfo();
    {
      info.setEncryptedAuthKey(securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(utils.createRandomType4UUID(), userEntity.getAuthKey()));
      info.setEncryptedClientUUID(securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(utils.createRandomType4UUID(), userEntity.getClientUUID()));
    }

    given().header(REQUEST_ID, utils.createRandomType4UUID())
            .body(info)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/gateway/auth/token/create");
    RedisService.setClientStatus(clientUUID+userId, OffsetDateTime.now());
    given().when().header("Request-Id", utils.createRandomType4UUID())
            .contentType(ContentType.JSON)
            .queryParam("userId", userId)
            .queryParam("aoid", userEntity.getAoId())
            .queryParam("clientUUID", clientUUID)
            .get("/v1/api/terminal/info")
            .then()
            .body(containsString("ACC-200"), containsString(clientUUID));
  }

  @Test
  void getTerminalInfoCaseNotFound(){
    String clientUUID = "2222-2222-3333-3331";
    String userId = userEntity.getId().toString();
    String aoid = userEntity.getAoId();
    RedisService.setClientStatus(clientUUID+userId, OffsetDateTime.now());
    given().when().header("Request-Id", utils.createRandomType4UUID())
            .contentType(ContentType.JSON)
            .queryParam("userId", userId)
            .queryParam("aoid", aoid)
            .queryParam("clientUUID", clientUUID)
            .get("/v1/api/terminal/info")
            .then()
            .body(containsString("ACC-404"));
  }
  @Test
  void getTerminalAllInfoCaseOk(){
    String clientUUID = userEntity.getClientUUID();
    String userId = userEntity.getId().toString();
    String aoid = userEntity.getAoId();
    CreateTokenInfo info = new CreateTokenInfo();
    {
      info.setEncryptedAuthKey(securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(utils.createRandomType4UUID(), userEntity.getAuthKey()));
      info.setEncryptedClientUUID(securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(utils.createRandomType4UUID(), userEntity.getClientUUID()));
    }

    given().header(REQUEST_ID, utils.createRandomType4UUID())
            .body(info)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/gateway/auth/token/create");

    RedisService.setClientStatus(clientUUID+userId, OffsetDateTime.now());
    given().when().header("Request-Id", utils.createRandomType4UUID())
            .contentType(ContentType.JSON)
            .queryParam("aoid", aoid)
            .queryParam("userId", userId)
            .get("/v1/api/terminal/all/info")
            .then()
            .body(containsString("ACC-200"), containsString(clientUUID));
  }
  @Test
  void getTerminalAllInfoCaseAoidIsNull(){
    String clientUUID = userEntity.getClientUUID();
    String userId = userEntity.getId().toString();

    CreateTokenInfo info = new CreateTokenInfo();
    {
      info.setEncryptedAuthKey(securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(utils.createRandomType4UUID(), userEntity.getAuthKey()));
      info.setEncryptedClientUUID(securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(utils.createRandomType4UUID(), userEntity.getClientUUID()));
    }

    given().header(REQUEST_ID, utils.createRandomType4UUID())
            .body(info)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/gateway/auth/token/create");

    RedisService.setClientStatus(clientUUID+userId, OffsetDateTime.now());
    given().when().header("Request-Id", utils.createRandomType4UUID())
            .contentType(ContentType.JSON)
            .queryParam("userId", userId)
            .get("/v1/api/terminal/all/info")
            .then()
            .body(containsString("ACC-200"), containsString(clientUUID));
  }

  @Test
  void logoutTerminalInfoCaseOk() throws JsonProcessingException {
    // 被授权端获取 bkey
    final Response bkeyResp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .post("/v1/api/gateway/totp/bkey");

    var bkey = (String) bkeyResp.body().as(ResponseBase.class).results();

    // 绑定端  校检 bkey，绑定用户
    var requestId = utils.createRandomType4UUID();
    CreateTokenResult tokenRet = testUtils.getCreateTokenResult(utils.createRandomType4UUID());
    var ak = Objects.requireNonNull(tokenUtils.verifyAccessToken(requestId, tokenRet.getAccessToken()));

    RealCallRequest real = new RealCallRequest();
    {
      real.setServiceName("eulixspace-account-service");
      real.setApiName("auth_totp_bkey_verify");
      real.setApiVersion("v1");
      var queries = new HashMap<String, String>();
      queries.put("bkey", bkey);
      real.setQueries(queries);
    }

    ResponseBase<Boolean> resp1 = testUtils.call(ak, real, requestId, new TypeReference<>() {});

    Assertions.assertTrue(resp1.results());

    // 被授权端根据 bkey poll 接口
    final Response bkeyPollResp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .body(new BkeyInfo(bkey))
            .post("/v1/api/gateway/totp/bkey/poll");

    var results = bkeyPollResp.body().as(ResponseBase.class).results();
    var createAuthCodeResult = utils.objectToResponseBaseResult(results, CreateAuthCodeResult.class);
    Assertions.assertTrue(createAuthCodeResult.getResult());
//    var boxLanInfo = createAuthCodeResult.getBoxLanInfo();

    // 绑定端传递 authCode
    requestId = utils.createRandomType4UUID();

    real = new RealCallRequest();
    {
      real.setServiceName("eulixspace-account-service");
      real.setApiName("auth_totp_auth-code");
      real.setApiVersion("v1");
    }

    ResponseBase<TotpAuthCode> authCodeResp = testUtils.call(ak, real, requestId, new TypeReference<>() {});
    var authCode = authCodeResp.results().getAuthCode();
    var clientUUID = utils.createRandomType4UUID();
    // 被授权端传递 authCode
    var tempSecret = utils.createRandomNumbers(16);
    var encryptAuthInfo = new EncryptAuthInfo(null, "v2", testUtils.encryptUsingBoxPublicKey(requestId, bkey),
            testUtils.encryptUsingBoxPublicKey(requestId, tempSecret),testUtils.encryptUsingBoxPublicKey(requestId, String.valueOf(authCode)),
            testUtils.encryptUsingBoxPublicKey(requestId, clientUUID), "HUAWEI P40", null);

    final Response verifyResp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(encryptAuthInfo)
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .post("/v1/api/gateway/totp/bkey/verify");

    var verify =  verifyResp.body().as(ResponseBase.class).results();
    var encryptAuthDTO = utils.objectToResponseBaseResult(verify, EncryptAuthDTO.class);

    Assertions.assertNotNull(encryptAuthDTO.accessToken());
    String userId = userEntity.getId().toString();
    String aoid = userEntity.getAoId();
    given().when().header("Request-Id", utils.createRandomType4UUID())
            .contentType(ContentType.JSON)
            .queryParam("aoid", aoid)
            .queryParam("userId", userId)
            .queryParam("clientUUID", clientUUID)
            .queryParam("AccessToken-clientUUID", "")
            .delete("/v1/api/terminal/info/delete")
            .then()
            .body(containsString("ACC-200"));


    var body = new AuthorizedTerminalLoginInfo();
    String tmpEncryptedSecret = "1234567890123456";
    body.setRefreshToken(encryptAuthDTO.refreshToken());
    body.setTmpEncryptedSecret(securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(utils.createRandomType4UUID(), tmpEncryptedSecret));
    //查无数据
    given().when().header("Request-Id", utils.createRandomType4UUID())
            .contentType(ContentType.JSON)
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(body)
            .post("/v1/api/auth/auto/login/poll")
            .then()
            .statusCode(200)
            .body(containsString("GW-4044"));
  }
}
