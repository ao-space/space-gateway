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

import io.quarkus.test.Mock;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import space.ao.services.support.OperationUtils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Mock
@ApplicationScoped
@RestClient
@SuppressWarnings("unused") // test uses this mocked class
public class MockVodServiceRestClient implements VodServiceRestClient {
  @Inject
  OperationUtils utils;
  @Override
  public Response getM3U8(String requestId, String uuid) {
    var file = new File("src/test/resources/gateway/index.m3u8");

    byte[] bytes;
    try {
      bytes = Files.readAllBytes(file.toPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return Response.ok(new ByteArrayInputStream(bytes)).header("Content-Disposition", "attachment;filename=" + file).build();
  }
}
