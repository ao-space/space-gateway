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

package space.ao.services.vod;

import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.support.FileUtils;
import space.ao.services.support.OperationUtils;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static space.ao.services.support.RestConfiguration.REQUEST_ID;

@QuarkusTest
class VodResourceTest {
  @Inject
  OperationUtils utils;
  @Inject
  UserInfoRepository userInfoRepository;
  private static Long userId;
  @BeforeEach
  @Transactional
  void setUp() {
    var userInfo = new UserEntity(UserEntity.Role.ADMINISTRATOR, "","test.user-domain.xyz", "aoid-1");
    userId = userInfoRepository.insertAdminUser(userInfo).getId();
  }
  @AfterEach
  @Transactional
  void tearDown() {
    userInfoRepository.deleteAll();
  }
  @Test
  void testGetM3U8() {
    var response = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .queryParam("userId", userId)
            .queryParam("uuid", "uuid")
            .when().get("/v1/api/vod/m3u8");

    try(var zip = response.getBody().asInputStream()){
      var tempFilePath = Files.createTempFile("test",  ".zip");
      var tempFile = tempFilePath.toFile();
      FileUtils.getFileFromInputStream(zip, tempFile);
      var dir = FileUtils.unzipAppletFile(tempFile.getPath());
      var files = new File(dir);
      for (File file1 : Objects.requireNonNull(files.listFiles())) {
        var result = Files.readString(Path.of(file1.getPath()));
        Log.info(result);
        Assertions.assertTrue(result.contains("test.user-domain.xyz") || result.contains("test.lan.user-domain.xyz") || result.contains("192.168.10.1") || result.contains("localhost"));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
