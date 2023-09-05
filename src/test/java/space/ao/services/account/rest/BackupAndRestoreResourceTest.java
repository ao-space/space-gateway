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

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.ao.services.support.test.TestUtils;
import space.ao.services.account.member.respository.BoxInfoRepository;
import space.ao.services.support.OperationUtils;

@QuarkusTest
class BackupAndRestoreResourceTest {

  @Inject
  OperationUtils utils;
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
  void tearDown() {
    testUtils.cleanData();
  }

  @Test
  void backupMemberGetCaseOk(){
    given().when().header("Request-Id", utils.createRandomType4UUID())
            .contentType(ContentType.JSON)
            .get("/v1/api/accountinfo")
            .then()
            .body(containsString("ACC-200"),containsString("clientUUID"));
  }
}
