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

/*
 * Copyright (c) 2023 Institute of Software Chinese Academy of Sciences (ISCAS)
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

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import space.ao.services.gateway.auth.qrcode.dto.BoxLanInfo;

@ApplicationScoped
public class AgentService {
  @Inject
  @RestClient
  AgentServiceRestClient agentServiceRestClient;

  public BoxLanInfo getBoxLanInfo(String requestId) {
    String lanIp = "";
    int port = 80;
    int tlsPort = 443;
    try {
      var ipAddressInfo = agentServiceRestClient.getIpAddressInfo(requestId);
      if(!ipAddressInfo.results().isEmpty()){
        lanIp = ipAddressInfo.results().get(0).getIp();
        port = ipAddressInfo.results().get(0).getPort();
        tlsPort = ipAddressInfo.results().get(0).getTlsPort();
        if(tlsPort == 0){
          tlsPort = 443;
        }
      }
    } catch (Exception e){
      Log.errorv(e, "requestId: {0}, getBoxLanInfo error: {1}", requestId, e.getMessage());
    }
    return BoxLanInfo.of(null , lanIp, null, port, tlsPort);
  }
}
