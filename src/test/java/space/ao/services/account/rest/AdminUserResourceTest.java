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

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import space.ao.services.account.member.dto.AdminBindInfo;
import space.ao.services.account.member.dto.AdminBindResult;
import space.ao.services.support.test.TestUtils;
import space.ao.services.account.member.respository.BoxInfoRepository;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.support.service.ServiceDefaultVar;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.security.SecurityUtils;
import jakarta.inject.Inject;
import java.io.File;
import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusTest
class AdminUserResourceTest {

  @Inject
  OperationUtils utils;

  @Inject
  ApplicationProperties properties;

  @Inject
  TestUtils testUtils;

  @Inject
  BoxInfoRepository boxInfoRepository;
  @Inject
  SecurityUtils securityUtils;
  @BeforeEach
  void setUp() {
    boxInfoRepository.insertOrUpdate("brk_11111111", utils.string2SHA256("000000"));
    securityUtils.loadAdminClientPublicFile();
  }

  @AfterEach
  void tearDown() {
    testUtils.cleanData();
  }


  @Test
  public void createAdminTest(){

    var adminBindInfo = new AdminBindInfo();
    adminBindInfo.setClientUUID(utils.createRandomType4UUID());
    adminBindInfo.setPhoneModel("HUAWEI P50");
    adminBindInfo.setSpaceName("Test");
    adminBindInfo.setPassword("12345");

    final Response createResp = given().when().header("Request-Id", utils.createRandomType4UUID())
        .contentType(ContentType.JSON)
        .body(adminBindInfo)
        .post("/v1/api/space/admin");

    var responseBase = createResp.as(ResponseBase.class);

    var adminBindResult = utils.objectToResponseBaseResult(responseBase.results(), AdminBindResult.class);
    Map<String, String> adminInfo = utils.readFromFile(new File(properties.accountDataLocation() + ServiceDefaultVar.DEFAULT_DATA_FILE));
    Assertions.assertTrue(adminInfo.containsValue(adminBindResult.authKey()));
  }
}
