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

package space.ao.services.gateway.auth.qrcode.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BoxLanInfo{
  private String publicKey;
  private String lanIp;
  private String userDomain;
  private int port;
  private int tlsPort;

  public static BoxLanInfo of(String publicKey, String lanIp, String userDomain, int port, int tlsPort){
    BoxLanInfo boxLanInfo = new BoxLanInfo();
    boxLanInfo.publicKey = publicKey;
    boxLanInfo.lanIp = lanIp;
    boxLanInfo.userDomain = userDomain;
    boxLanInfo.port = port;
    boxLanInfo.tlsPort = tlsPort;
    return boxLanInfo;
  }
}