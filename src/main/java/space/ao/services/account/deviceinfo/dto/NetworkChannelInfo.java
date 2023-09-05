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
package space.ao.services.account.deviceinfo.dto;

public class NetworkChannelInfo {
  private Boolean lan;
  private Boolean p2p;
  private Boolean wan;
  private String userDomain;

  public static NetworkChannelInfo of(Boolean lan, Boolean p2p, Boolean wan) {
    NetworkChannelInfo networkChannelInfo = new NetworkChannelInfo();
    networkChannelInfo.lan = lan;
    networkChannelInfo.p2p = p2p;
    networkChannelInfo.wan = wan;
    return networkChannelInfo;
  }

  public void setUserDomain(String userDomain) {
    this.userDomain = userDomain;
  }

  @Override
  public String toString() {
    return "NetworkChannelInfo{" +
            "lan=" + lan +
            ", p2p=" + p2p +
            ", wan=" + wan +
            ", userDomain=" + userDomain +
            '}';
  }
}
