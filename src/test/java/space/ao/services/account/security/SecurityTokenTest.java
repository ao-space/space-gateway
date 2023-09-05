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

package space.ao.services.account.security;

import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.ao.services.support.test.TestUtils;
import space.ao.services.account.member.respository.BoxInfoRepository;
import space.ao.services.account.security.dto.SecurityPasswdCheckReq;
import space.ao.services.account.security.utils.token.SecurityTokenType;
import space.ao.services.account.security.utils.token.SecurityTokenUtils;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.TokenUtils;
import space.ao.services.support.response.ResponseBase;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import static io.restassured.RestAssured.given;

@QuarkusTest
class SecurityTokenTest {
  @Inject
  SecurityTokenUtils securityTokenUtils;
  @Inject
  OperationUtils utils;

  @Inject
  BoxInfoRepository boxInfoRepository;

  @Inject
  TestUtils testUtils;

  @BeforeEach
  @Transactional
  void setUp() {
    boxInfoRepository.insertOrUpdate("boxregkey-1", utils.string2SHA256("123456"));
    testUtils.createAdmin();
  }

  @AfterEach
  @Transactional
  void tearDown() {
    testUtils.deleteAllUser();
  }
  @Test
  void testSecurityToken() {
    var clientUUid = "1234567890";
    var tokenRes = securityTokenUtils.create(SecurityTokenType.TOKEN_TYPE_VERIFIED_PWD_TOKEN,
            clientUUid);

    var token = securityTokenUtils.doVerifySecurityToken("", tokenRes.getSecurityToken(), SecurityTokenType.TOKEN_TYPE_VERIFIED_PWD_TOKEN, clientUUid);

    Assertions.assertNotEquals(token, null);
  }

  @Inject
  TokenUtils tokenUtils;
  @Test
  void test(){
    var token = tokenUtils.createDefaultTokenResult("", null,
            "1", "1234567890", null);

    tokenUtils.verifyAccessToken("", token.getAccessToken());
  }

  @Test
  void testVerify() {
    Log.info(boxInfoRepository.findAll().list());
    var clientUUID = "1234567890";
    var req = SecurityPasswdCheckReq.of();
    req.setOldPasswd("123456");
    final Response verifyResp = given().when().header("Request-Id", utils.createRandomType4UUID())
            .contentType(ContentType.JSON)
            .queryParam("userId", "1")
            .queryParam("AccessToken-clientUUID", clientUUID)
            .body(req)
            .post("/v1/api/security/passwd/verify");

    var responseBase = verifyResp.as(ResponseBase.class);
    var data = utils.objectToMap(responseBase.results());
    var token = data.get("securityToken");

    var tokenRes = securityTokenUtils.doVerifySecurityToken("", token.toString(), SecurityTokenType.TOKEN_TYPE_VERIFIED_PWD_TOKEN, clientUUID);

    Assertions.assertNotEquals(null, tokenRes);
  }
}
