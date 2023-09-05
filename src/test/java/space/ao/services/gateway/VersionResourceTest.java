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

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import space.ao.services.support.OperationUtils;

@QuarkusTest
class VersionResourceTest {

  @Inject
  OperationUtils utils;

  @Test
  void testAppVersion() {
    given()
        .header("Request-Id", utils.createRandomType4UUID())
        .queryParam("version","1.0")
        .queryParam("appName", "app-1")
        .queryParam("appType", "ios")
        .when().get("/v1/api/gateway/version/app")
        .then()
        .statusCode(200)
        .body(containsString("true"));
  }

  @Test
  void testBoxVersion() {
    given()
        .header("Request-Id", utils.createRandomType4UUID())
        .when().get("/v1/api/gateway/version/box/current")
        .then()
        .statusCode(200)
        .body(containsString("OK"));
  }

  @Test
  void testCompatible(){
    given()
        .header("Request-Id", utils.createRandomType4UUID())
        .queryParam("version","1.0")
        .queryParam("appName", "app-1")
        .queryParam("appType", "ios")
        .when().get("/v1/api/gateway/version/compatible")
        .then()
        .statusCode(200)
        .body(containsString("true"));
  }
}
