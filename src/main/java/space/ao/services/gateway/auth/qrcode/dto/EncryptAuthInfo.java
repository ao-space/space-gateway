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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
@Data
@NoArgsConstructor
public class EncryptAuthInfo {

  @JsonCreator
  public EncryptAuthInfo(
      @JsonProperty("spaceId") String spaceId,
      @JsonProperty("version") String version,
      @JsonProperty("bkey") String bkey,
      @JsonProperty("tmpEncryptedSecret") String tmpEncryptedSecret,
      @JsonProperty("authCode") String authCode,
      @JsonProperty("clientUUID") String clientUUID,
      @JsonProperty("terminalMode") String terminalMode,
      @JsonProperty("terminalType") String terminalType) {
    this.spaceId = spaceId;
    this.version = version;
    this.bkey = bkey;
    this.tmpEncryptedSecret = tmpEncryptedSecret;
    this.authCode = authCode;
    this.clientUUID = clientUUID;
    this.terminalMode = terminalMode;
    this.terminalType = terminalType;
  }


  @Schema(description = "盒子公钥加密的 空间标识（子域名或者用户昵称）")
  String spaceId;
  @Schema(description = "version")
  String version;

  @Schema(description = "盒子公钥加密的 bkey（totp 时不加密）, 局域网时为空时")
  String bkey;

  @Schema(description = "盒子公钥加密的 临时密钥")
  @NotBlank
  String tmpEncryptedSecret;

  @Schema(description = "盒子公钥加密的 authCode")
  @NotBlank
  String authCode;

  @Schema(description = "盒子公钥加密的 clientUUID")
  @NotBlank
  String clientUUID;

  @Schema(description = "客户端型号")
  @NotBlank
  String terminalMode;

  @Schema(description = "客户端类型 android/ios/web/pc")
  String terminalType;

  @JsonIgnore
  @Schema(description = "登录地址")
  String loginAddress;
}
