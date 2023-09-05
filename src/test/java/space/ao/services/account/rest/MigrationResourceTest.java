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

import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.ao.services.support.test.TestUtils;
import space.ao.services.account.member.dto.migration.BoxMigrationInfo;
import space.ao.services.account.member.dto.migration.UserMigrationInfo;
import space.ao.services.support.OperationUtils;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class MigrationResourceTest {

    @Inject
    TestUtils testUtils;

    @BeforeEach
    void setUp() {
        testUtils.createAdmin();
        Log.infov("setUp: createAdmin");
    }

    @AfterEach
    void tearDown() {
        testUtils.cleanData();
    }
    @Inject
    OperationUtils utils;
    @Test
    void getMigrationInfo(){
        given().when().header("Request-Id", utils.createRandomType4UUID())
                .get("/v1/api/user/migration")
                .then()
                .statusCode(200)
                .body(containsString("ACC-200"), containsString("aoid-1"));
    }

    @Test
    void updateUserInfos(){
        List<UserMigrationInfo> userMigrationInfos = new ArrayList<>();
        userMigrationInfos.add(UserMigrationInfo.of("aoid-1", "aoid-1-userdomain", null, null));
        given().when().header("Request-Id", utils.createRandomType4UUID())
                .contentType(ContentType.JSON)
                .body(BoxMigrationInfo.of("", userMigrationInfos))
                .put("/v1/api/user/migration")
                .then()
                .statusCode(200)
                .body(containsString("ACC-200"), containsString("aoid-1"), containsString("aoid-1-userdomain"));

        given().when().header("Request-Id", utils.createRandomType4UUID())
                .get("/v1/api/user/migration")
                .then()
                .statusCode(200)
                .body(containsString("ACC-200"), containsString("aoid-1"), containsString("aoid-1-userdomain"));
    }
}
