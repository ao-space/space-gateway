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

package space.ao.services.account.mock;

import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import space.ao.services.account.deviceinfo.dto.DeviceStorageInfo;
import space.ao.services.account.deviceinfo.service.DeviceStorageService;
import space.ao.services.support.response.ResponseBase;
@Mock
@ApplicationScoped
@RestClient
@SuppressWarnings("unused") // test uses this mocked class
public class MockSystemAgentService implements DeviceStorageService {

  @Override
  public ResponseBase<DeviceStorageInfo> getStorageInfo(String requestId) {
    return ResponseBase.of("200", "", requestId, DeviceStorageInfo.of("18", "18", "36"));
  }
}
