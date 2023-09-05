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

package space.ao.services.gateway;

import com.google.common.collect.ImmutableMap;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.ao.services.support.test.TestUtils;
import space.ao.services.account.member.respository.BoxInfoRepository;
import space.ao.services.gateway.auth.CreateTokenInfo;
import space.ao.services.gateway.auth.CreateTokenResult;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.TokenUtils;
import space.ao.services.support.model.AccessToken;
import space.ao.services.support.security.SecurityUtils;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;

import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static space.ao.services.support.RestConfiguration.REQUEST_ID;

@QuarkusTest
class GatewayResourceTest {

  @Inject
  OperationUtils utils;
  @Inject
  SecurityUtils securityUtils;
  @Inject
  TokenUtils tokenUtils;
  @Inject
  TestUtils testUtils;
  @Inject
  BoxInfoRepository boxInfoRepository;

  @BeforeEach
  void setUp() {
    boxInfoRepository.insertOrUpdate("boxregkey-1", utils.string2SHA256("000000"));
    testUtils.createAdmin();
  }

  @AfterEach
  @Transactional
  void tearDown() {
    testUtils.cleanData();
  }

  @Test
  void testCallNormalCaseOk() {
    final CreateTokenInfo info = testUtils.getAdminCreateTokenTestInfo(utils.createRandomType4UUID());
    var requestId = utils.createRandomType4UUID();
    final Response tokenResp = given()
        .header(REQUEST_ID, requestId)
        .body(info)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/gateway/auth/token/create");

    final CreateTokenResult tokenRet = tokenResp.body().as(CreateTokenResult.class);
    final AccessToken ak = Objects.requireNonNull(tokenUtils.verifyAccessToken(requestId, tokenRet.getAccessToken()));
    RealCallRequest real = new RealCallRequest();
    {
      real.setServiceName("test-service");
      real.setApiName("greeting");
      real.setApiVersion("v1");
    }

    CallRequest call = new CallRequest(securityUtils.encryptWithSecret(
            utils.objectToJson(real), ak.getSharedSecret(), ak.getSharedInitializationVector()), ak.getToken());

    final Response callResp = given()
        .header(REQUEST_ID, utils.createRandomType4UUID())
        .body(call)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/gateway/call");

    final RealCallResult callRet = callResp.body().as(RealCallResult.class);
    String greeting = securityUtils.decryptWithSecret(callRet.body(), ak.getSharedSecret(), ak.getSharedInitializationVector());
    assertEquals("Hello", greeting);
  }

  @Test
  void testDownloadNormalCaseOk() {
    var requestId = utils.createRandomType4UUID();
    final CreateTokenInfo info = testUtils.getAdminCreateTokenTestInfo(requestId);
    final Response tokenResp = given()
        .header(REQUEST_ID, requestId)
        .body(info)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/gateway/auth/token/create");

    final CreateTokenResult tokenRet = tokenResp.body().as(CreateTokenResult.class);
    final AccessToken ak = Objects.requireNonNull(tokenUtils.verifyAccessToken(requestId, tokenRet.getAccessToken()));
    RealCallRequest real = new RealCallRequest();
    {
      real.setServiceName("test-service");
      real.setApiName("download");
      real.setApiVersion("v1");
      real.setQueries(ImmutableMap.of("file", "hello.txt", "content","hello"));
    }

    CallRequest call = new CallRequest(securityUtils.encryptWithSecret(
            utils.objectToJson(real), ak.getSharedSecret(), ak.getSharedInitializationVector()), ak.getToken());

    final Response downloadResp = given()
        .header(REQUEST_ID, utils.createRandomType4UUID())
        .body(call)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/gateway/download");

    final byte[] content = downloadResp.body().asByteArray();
    String greeting = securityUtils.decryptWithSecret(
        Base64.getEncoder().encodeToString(content), ak.getSharedSecret(), ak.getSharedInitializationVector());
    assertEquals("hello", greeting);
    assertEquals("16", downloadResp.header("File-Size"));
    assertEquals("16", downloadResp.header("Content-Length"));
    assertEquals("attachment;filename=hello.txt",  downloadResp.header("Content-Disposition"));
  }

  @Test
  void testUploadNormalCaseOk() {
    var requestId = utils.createRandomType4UUID();
    final CreateTokenInfo info = testUtils.getAdminCreateTokenTestInfo(requestId);
    final Response tokenResp = given()
        .header(REQUEST_ID, requestId)
        .body(info)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/gateway/auth/token/create");

    final CreateTokenResult tokenRet = tokenResp.body().as(CreateTokenResult.class);
    final AccessToken ak = Objects.requireNonNull(tokenUtils.verifyAccessToken(requestId, tokenRet.getAccessToken()));
    RealCallRequest real = new RealCallRequest();
    {
      var ue = new UploadEntity();
      ue.setFilename("hello.txt");
      ue.setMediaType("text/plain;charset=UTF-8");

      real.setServiceName("test-service");
      real.setApiName("upload");
      real.setApiVersion("v1");
      real.setEntity(ue);
    }

    CallRequest call = new CallRequest(securityUtils.encryptWithSecret(
            utils.objectToJson(real), ak.getSharedSecret(), ak.getSharedInitializationVector()), ak.getToken());

    final Response uploadResp = given()
        .header(REQUEST_ID, utils.createRandomType4UUID())
        .multiPart("callRequest", call, MediaType.APPLICATION_JSON)
        .multiPart("file", "hello.txt",
            Base64.getDecoder().decode(
                    securityUtils.encryptWithSecret("hello", ak.getSharedSecret(), ak.getSharedInitializationVector())))
        .when()
        .post("/v1/api/gateway/upload");

    final RealCallResult callRet = uploadResp.body().as(RealCallResult.class);
    String greeting = securityUtils.decryptWithSecret(callRet.body(), ak.getSharedSecret(), ak.getSharedInitializationVector());
    assertEquals("hello", greeting);
  }

  @Test
  void testUploadOneNormalCaseOk() {
    var requestId = utils.createRandomType4UUID();
    final CreateTokenInfo info = testUtils.getAdminCreateTokenTestInfo(requestId);
    final Response tokenResp = given()
        .header(REQUEST_ID, requestId)
        .body(info)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/gateway/auth/token/create");

    final CreateTokenResult tokenRet = tokenResp.body().as(CreateTokenResult.class);
    final AccessToken ak = Objects.requireNonNull(tokenUtils.verifyAccessToken(requestId, tokenRet.getAccessToken()));
    RealCallRequest real = new RealCallRequest();
    {
      var queries = new HashMap<String, String>();
      queries.put("FileName", "hello.txt");
      queries.put("MediaType", "text/plain;charset=UTF-8");

      real.setServiceName("test-service");
      real.setApiName("upload-one");
      real.setApiVersion("v1");
      real.setQueries(queries);
    }

    CallRequest call = new CallRequest(securityUtils.encryptWithSecret(
            utils.objectToJson(real), ak.getSharedSecret(), ak.getSharedInitializationVector()), ak.getToken());

    final Response uploadResp = given()
        .header(REQUEST_ID, utils.createRandomType4UUID())
        .multiPart("callRequest", call, MediaType.APPLICATION_JSON)
        .multiPart("file", "hello.txt",
            Base64.getDecoder().decode(
                    securityUtils.encryptWithSecret("hello", ak.getSharedSecret(), ak.getSharedInitializationVector())))
        .when()
        .post("/v1/api/gateway/upload");

    final RealCallResult callRet = uploadResp.body().as(RealCallResult.class);
    String greeting = securityUtils.decryptWithSecret(callRet.body(), ak.getSharedSecret(), ak.getSharedInitializationVector());
    assertEquals("hello.txt, hello", greeting);
  }

  @Test
  void testCallRequestIdAbsentFailedOk() {
    final CreateTokenInfo info = testUtils.getAdminCreateTokenTestInfo(utils.createRandomType4UUID());
    given()
        .body(info)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/gateway/auth/token/create")
        .then()
        .statusCode(BAD_REQUEST.getStatusCode());
  }

  @Test
  void testSseNormalCaseOk() {
    final CreateTokenInfo info = testUtils.getAdminCreateTokenTestInfo(utils.createRandomType4UUID());
    var requestId = utils.createRandomType4UUID();
    final Response tokenResp = given()
        .header(REQUEST_ID, requestId)
        .body(info)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/gateway/auth/token/create");

    final CreateTokenResult tokenRet = tokenResp.body().as(CreateTokenResult.class);
    final AccessToken ak = Objects.requireNonNull(tokenUtils.verifyAccessToken(requestId, tokenRet.getAccessToken()));

    given()
        .header("Authorization", "Bearer " + ak.getToken())
        .header(REQUEST_ID, utils.createRandomType4UUID())
        .queryParams("topic","test")
        .when()
        .get("/v1/api/gateway/sse")
        .then()
        .statusCode(200)
        .contentType(MediaType.SERVER_SENT_EVENTS);
  }

  @Test
  void testSseUnAuthorizationErrorOk() {
    given()
        .header("Authorization", "Bearer <wrong token>")
        .header(REQUEST_ID, utils.createRandomType4UUID())
        .queryParams("topic","test")
        .when()
        .get("/v1/api/gateway/sse")
        .then()
        .statusCode(401);
  }
}