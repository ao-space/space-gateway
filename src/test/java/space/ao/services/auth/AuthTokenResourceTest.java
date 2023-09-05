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

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.ao.services.gateway.auth.CreateTokenResult;
import space.ao.services.support.test.TestUtils;
import space.ao.services.support.OperationUtils;

import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static space.ao.services.support.RestConfiguration.REQUEST_ID;

@QuarkusTest
class AuthTokenResourceTest {

  @Inject
  TestUtils testUtils;
  @Inject
  OperationUtils utils;

  @BeforeEach
  void setUp() {
    testUtils.createAdmin();
  }

  @AfterEach
  void tearDown() {
    testUtils.cleanData();
  }

  @Test
  void testVerifyNormalCaseOk() {
    final CreateTokenResult tokenRet = testUtils.getCreateTokenResult(utils.createRandomType4UUID());

    given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .param("access-token", tokenRet.getAccessToken())
            .when()
            .get("/v1/api/auth/token/verify")
            .then()
            .statusCode(200)
            .body(containsString("OK"));
  }
}
