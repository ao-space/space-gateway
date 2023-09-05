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

package space.ao.services.support.agent;

import io.quarkus.test.Mock;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import space.ao.services.support.agent.info.DeviceInfo;
import space.ao.services.support.agent.info.DidDocResult;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.support.agent.info.IpAddressInfo;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@Mock
@ApplicationScoped
@RestClient
@SuppressWarnings("unused") // test uses this mocked class
public class MockSystemAgentServiceRestClient implements AgentServiceRestClient{
  @Override
  public ResponseBase<List<IpAddressInfo>> getIpAddressInfo(String requestId) {
    List<IpAddressInfo> list = new ArrayList<>();
    list.add(new IpAddressInfo("192.168.10.1", false, "eth0", 80, 443));
    return ResponseBase.ok("xx", list).build();
  }

  @Override
  public ResponseBase<DeviceInfo> getDeviceVersion() {
    var deviceInfo = new DeviceInfo();
    return ResponseBase.<DeviceInfo>builder().code("AG-200").results(deviceInfo).build();
  }

  @Override
  public ResponseBase<DidDocResult> getDidDocument(String requestId, String did, String aoid) {
    return null;
  }

  @Override
  public ResponseBase<DidDocResult> changePasswordDidDocument(String requestId, String did, String aoid, String newPassword) {
    return null;
  }
}
