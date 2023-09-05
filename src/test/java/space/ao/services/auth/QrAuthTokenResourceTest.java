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

package space.ao.services.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.ao.services.gateway.RealCallRequest;
import space.ao.services.gateway.auth.CreateTokenResult;
import space.ao.services.gateway.auth.qrcode.dto.CreateBkeyInfo;
import space.ao.services.gateway.auth.qrcode.dto.EncryptAuthDTO;
import space.ao.services.gateway.auth.qrcode.dto.EncryptAuthInfo;
import space.ao.services.gateway.auth.qrcode.dto.TotpAuthCode;
import space.ao.services.gateway.auth.qrcode.dto.v2.BkeyInfo;
import space.ao.services.gateway.auth.qrcode.dto.v2.CreateAuthCodeResult;
import space.ao.services.support.test.TestUtils;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.TokenUtils;
import space.ao.services.support.response.ResponseBase;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static space.ao.services.support.RestConfiguration.REQUEST_ID;

@QuarkusTest
class QrAuthTokenResourceTest {

  @Inject
  TestUtils testUtils;
  @Inject
  OperationUtils utils;
  @Inject
  TokenUtils tokenUtils;

  @BeforeEach
  void setUp() {
    testUtils.createAdmin();
  }

  @AfterEach
  void tearDown() {
    testUtils.deleteAllUser();
  }

  /**
   * 局域网扫码授权单元测试
   * @author zhichuang
   */
  @Test
  void testQrLoginNormalCaseOk() throws JsonProcessingException {

    // 被授权端获取 bkey
    final Response bkeyResp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .post("/v1/api/gateway/totp/bkey");

    var bkey = (String) bkeyResp.body().as(ResponseBase.class).results();

    // 绑定端  校检 bkey，绑定用户
    var requestId = utils.createRandomType4UUID();
    final CreateTokenResult tokenRet = testUtils.getCreateTokenResult(utils.createRandomType4UUID());
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

    // 被授权端传递 authCode
    var tempSecret = utils.createRandomNumbers(16);
    var encryptAuthInfo = new EncryptAuthInfo(null, "v2", testUtils.encryptUsingBoxPublicKey(requestId, bkey),
            testUtils.encryptUsingBoxPublicKey(requestId, tempSecret),testUtils.encryptUsingBoxPublicKey(requestId, String.valueOf(authCode)),
            testUtils.encryptUsingBoxPublicKey(requestId, utils.createRandomType4UUID()), "HUAWEI P40", null);

    final Response verifyResp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(encryptAuthInfo)
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .post("/v1/api/gateway/totp/bkey/verify");

    var verify =  verifyResp.body().as(ResponseBase.class).results();
    var encryptAuthDTO = utils.objectToResponseBaseResult(verify, EncryptAuthDTO.class);

    Assertions.assertNotNull(encryptAuthDTO.accessToken());
  }

  @Test
  void loginByUserDomainTest() throws JsonProcessingException {
    // 被授权端获取 bkey
    final Response resp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .queryParam("spaceId", "hello")
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .get("/v1/api/gateway/totp/bkey");

    var createBkeyInfoResp =  resp.body().as(ResponseBase.class).results();
    var createBkeyInfo = utils.objectToResponseBaseResult(createBkeyInfoResp, CreateBkeyInfo.class);
    Assertions.assertNotNull(createBkeyInfo.getBoxLanInfo());
    //获取 auth code
    var requestId = utils.createRandomType4UUID();
    final CreateTokenResult tokenRet = testUtils.getCreateTokenResult(utils.createRandomType4UUID());
    var ak = Objects.requireNonNull(tokenUtils.verifyAccessToken(requestId, tokenRet.getAccessToken()));

    var real = new RealCallRequest();
    {
      real.setServiceName("eulixspace-account-service");
      real.setApiName("auth_totp_auth-code");
      real.setApiVersion("v1");
    }

    ResponseBase<TotpAuthCode> authCodeResp = testUtils.call(ak, real, requestId, new TypeReference<>() {});
    var authCode = authCodeResp.results().getAuthCode();

    // 被授权端传递 authCode
    var tempSecret = utils.createRandomNumbers(16);
    var encryptAuthInfo = new EncryptAuthInfo(testUtils.encryptUsingBoxPublicKey(requestId, "domain."+ utils.getUserDomainSuffix() ), null,
            null,
            testUtils.encryptUsingBoxPublicKey(requestId, tempSecret),testUtils.encryptUsingBoxPublicKey(requestId, String.valueOf(authCode)),
            testUtils.encryptUsingBoxPublicKey(requestId, utils.createRandomType4UUID()), "HUAWEI P40", null);

    final Response verifyResp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(encryptAuthInfo)
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .post("/v1/api/gateway/totp/bkey/verify");

    var verify =  verifyResp.body().as(ResponseBase.class).results();
    var encryptAuthDTO = utils.objectToResponseBaseResult(verify, EncryptAuthDTO.class);

    Assertions.assertNotNull(encryptAuthDTO.accessToken());

  }

  @Test
  void loginLimitTest() {

    var requestId = utils.createRandomType4UUID();
    // 被授权端传递 authCode
    var encryptAuthInfo = new EncryptAuthInfo();
    var tempSecret = utils.createRandomNumbers(16);
    encryptAuthInfo.setAuthCode(testUtils.encryptUsingBoxPublicKey(requestId, utils.createRandomNumbers(6)));
    encryptAuthInfo.setTmpEncryptedSecret(testUtils.encryptUsingBoxPublicKey(requestId, tempSecret));
    encryptAuthInfo.setSpaceId(testUtils.encryptUsingBoxPublicKey(requestId, "tempSecret"));
    encryptAuthInfo.setClientUUID(testUtils.encryptUsingBoxPublicKey(requestId, utils.createRandomType4UUID()));
    encryptAuthInfo.setTerminalMode("HUAWEI P40");


    for (int i = 0; i < 5; i++) {
      given()
              .header(REQUEST_ID, utils.createRandomType4UUID())
              .body(encryptAuthInfo)
              .when()
              .contentType(MediaType.APPLICATION_JSON)
              .post("/v1/api/gateway/totp/bkey/verify")
              .then().statusCode(200).body(containsString("GW-4023"));
    }

    given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(encryptAuthInfo)
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .post("/v1/api/gateway/totp/bkey/verify")
            .then().statusCode(200).body(containsString("GW-410"));

  }



}
