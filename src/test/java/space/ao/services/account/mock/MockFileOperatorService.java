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
import space.ao.services.account.deviceinfo.dto.UserStorageInfo;
import space.ao.services.account.deviceinfo.service.FileOperatorService;
import space.ao.services.support.response.ResponseBase;

@Mock
@ApplicationScoped
@RestClient
@SuppressWarnings("unused") // test uses this mocked class
public class MockFileOperatorService implements FileOperatorService {

  @Override
  public ResponseBase<UserStorageInfo> getUserStorageInfo(String requestId, String userid, String targetUserId) {
    return ResponseBase.of("200", "","", new UserStorageInfo());
  }

  @Override
  public ResponseBase<String> fileUserInitial(String requestId, Long userid, Long spaceLimit) {
    return ResponseBase.of("200", "", "", "");
  }

  @Override
  public ResponseBase<String> fileUserDelete(String requestId, String userid, String targetUserId) {
    return ResponseBase.of("200", "", "", "");
  }
}
