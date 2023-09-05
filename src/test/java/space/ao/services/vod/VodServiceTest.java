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

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import space.ao.services.support.FileUtils;
import space.ao.services.support.OperationUtils;

import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@QuarkusTest
class VodServiceTest {
  @Inject
  VodService vodService;
  @Inject
  OperationUtils operationUtils;
  @Test
  void test() throws IOException {
    String requestId = operationUtils.createRandomType4UUID();
    var file = new File("src/test/resources/gateway/index.m3u8");
    var resultFile = FileUtils.zipFiles(vodService.handleM3U8(requestId, Files.readString(file.toPath()), "test"), "index");
    var dir = FileUtils.unzipAppletFile(resultFile.getPath());
    var files = new File(dir);
    for (File file1 : Objects.requireNonNull(files.listFiles())) {
      var result = Files.readString(Path.of(file1.getPath()));
      Assertions.assertTrue(result.contains("#EXTM3U"));
    }
  }
}
