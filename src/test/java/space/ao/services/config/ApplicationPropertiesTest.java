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

package space.ao.services.config;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ApplicationPropertiesTest {

  @Inject
  ApplicationProperties properties;

  @Test
  void accountUrl() {
    assertEquals("http://localhost:8080", properties.accountUrl());
  }

  @Test
  void platformUrl() {
    assertEquals("https://services.eulix.xyz", properties.psplatformUrl());
    assertEquals("https://services.eulix.xyz", properties.ssplatformUrl());

  }

  @Test
  void fileapiUrl() {
    assertEquals("http://aospace-fileapi:2001/space/v1/api", properties.fileapiUrl());
  }

  @Test
  void agentUrl() {
    assertEquals("http://172.17.0.1:5680/agent/v1/api", properties.systemAgentUrlBase());
  }

  @Test
  void vodUrl() {
    assertEquals("http://aospace-media-vod:3001/space/v1/api/vod", properties.appVodUrl());
  }

  @Test
  void securityAgentUrl() {
    assertEquals("http://172.17.0.1:9200/security/v1/api", properties.securityApiUrl());
  }

}