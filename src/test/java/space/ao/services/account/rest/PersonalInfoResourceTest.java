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
import io.restassured.response.Response;
import java.io.InputStream;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.ao.services.account.member.dto.MemberCreateResult;
import space.ao.services.support.test.TestUtils;
import space.ao.services.account.personalinfo.dto.PersonalInfo;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.support.service.ServiceDefaultVar;

@QuarkusTest
class PersonalInfoResourceTest {
  @Inject
  TestUtils testUtils;
  @Inject
  OperationUtils utils;

  MemberCreateResult memberCreateResult;


  @BeforeEach
  void setUp() {
    testUtils.createAdmin();

    final Response resp = given().when().header("Request-Id", utils.unifiedRandomCharters(8))
            .queryParam("client_uuid", "clientUUID")
            .contentType(ContentType.JSON)
            .get("/v1/api/user");
    memberCreateResult = utils.jsonToObject(utils.objectToJson(resp.body().as(ResponseBase.class).results()), MemberCreateResult.class);
  }

  @AfterEach
  @Transactional
  void tearDown() {
    testUtils.cleanData();
  }

  @Test
  void getPersonalInfoCaseOk(){
    given().when().header("Request-Id", utils.unifiedRandomCharters(8))
            .queryParam("userId", 1)
            .get("/v1/api/personal/info")
            .then()
            .statusCode(200)
            .body(containsString("ACC-200"), containsString("aoid-1"));
  }

  @Test
  void getMemberListCaseOk(){
    given().when().header("Request-Id", utils.unifiedRandomCharters(8))
            .queryParam("userId", memberCreateResult.userid())
            .get("/v1/api/member/list")
            .then()
            .statusCode(200)
            .body(containsString("ACC-200"), containsString("aoid-1"));
  }

  @Test
  void updatePersonalNameCaseOk(){
    given().when().header("Request-Id", utils.unifiedRandomCharters(8))
            .queryParam("userId", 1)
            .contentType(ContentType.JSON)
            .body("{\"personalName\": \"eulixosName\" }")
            .post("/v1/api/personal/info/update")
            .then()
            .statusCode(200)
            .body(containsString("ACC-201"));
    //查询数据是否修改成功
    given().when().header("Request-Id", utils.unifiedRandomCharters(8))
            .queryParam("userId", 1)
            .get("/v1/api/personal/info")
            .then()
            .statusCode(200)
            .body(containsString("ACC-200"), containsString("eulixosName"));
  }

  @Test
  void updatePersonalSignCaseOk(){
    given().when().header("Request-Id", utils.unifiedRandomCharters(8))
            .queryParam("userId", 1)
            .contentType(ContentType.JSON)
            .body("{\"personalSign\": \"HelloWorld\" }")
            .post("/v1/api/personal/info/update")
            .then()
            .statusCode(200)
            .body(containsString("ACC-201"));
    //查询数据是否修改成功
    given().when().header("Request-Id", utils.unifiedRandomCharters(8))
            .queryParam("userId", 1)
            .get("/v1/api/personal/info")
            .then()
            .statusCode(200)
            .body(containsString("ACC-200"), containsString("HelloWorld"));
  }

  @Test
  void updateMemberInfoCaseOk(){
    given().when().header("Request-Id", utils.createRandomType4UUID())
            .queryParam("userId", 1)
            .contentType(ContentType.JSON)
            .body("{\"aoId\": \"aoid-1\", \"nickName\":\"eulixosNewName\" }")
            .post("/v1/api/member/name/update")
            .then()
            .statusCode(200)
            .body(containsString("ACC-201"));
    //查询数据是否修改成功
    given().when().header("Request-Id", utils.unifiedRandomCharters(8))
            .queryParam("userId", 1)
            .get("/v1/api/personal/info")
            .then()
            .statusCode(200)
            .body(containsString("ACC-200"), containsString("eulixosNewName"));
  }

  @Test
  void personalImageUpdateCaseOk() {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
            ServiceDefaultVar.DEFAULT_IMAGE_PATH +
                    ServiceDefaultVar.DEFAULT_IMAGE_FILE.toString());
    given().when().header("Request-Id", utils.unifiedRandomCharters(8))
            .queryParam("userId", memberCreateResult.userid())
            .multiPart("param", "{\"filename\":\"file.png\"}",  MediaType.APPLICATION_JSON)
            .multiPart("file", "file.png", inputStream)
            .post("/v1/api/personal/image/update")
            .then()
            .body(containsString("201"));

    //查询图像是否被修改成功
    given().when().header("Request-Id", utils.unifiedRandomCharters(8))
              .queryParam("userId", memberCreateResult.userid())
              .get("/v1/api/personal/image")
              .then()
              .header("Content-Disposition", containsString("file.png"));
    given().when().header("Request-Id", utils.createRandomType4UUID())
            .queryParam("userId", 1)
            .queryParam("aoid", memberCreateResult.aoId())
            .get("/v1/api/personal/image")
            .then()
            .header("Content-Disposition", containsString("file.png"));
  }

  @Test
  void personalInfoUpdateCaseFailed(){
    given().when().header("Request-Id", utils.unifiedRandomCharters(8))
        .queryParam("userId", 1)
        .contentType(ContentType.JSON)
        .body(new PersonalInfo("xx", "xx", "aoid-1", "sxjfcqnk", "xx"))
        .put("/v1/api/personal/info")
        .then()
        .statusCode(200)
        .body(containsString("ACC-4022"));

    given().when().header("Request-Id", utils.unifiedRandomCharters(8))
            .queryParam("userId", 1)
            .contentType(ContentType.JSON)
            .body(new PersonalInfo("xx", "xx", "aoid-1", null, "xx"))
            .put("/v1/api/personal/info")
            .then()
            .statusCode(200)
            .body(containsString("ACC-201"));

    given().when().header("Request-Id", utils.unifiedRandomCharters(8))
        .queryParam("userId", 1)
        .contentType(ContentType.JSON)
        .body("{\"aoId\": \"aoid-1\", \"userDomain\":\"xxccxxx\" }")
        .put("/v1/api/personal/info")
        .then()
        .statusCode(200)
        .body(containsString("ACC-4022"));
  }
}
