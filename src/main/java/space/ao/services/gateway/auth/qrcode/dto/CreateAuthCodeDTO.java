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

@Data
@NoArgsConstructor
@Schema(description = "新设备需要的授权信息")
public class CreateAuthCodeDTO {

  @Schema(description = "使用对称密钥加密的授权码 (4或6 位数字)。")
  private String authCode;
  @Schema(description = "验证码剩余时间 authCode 6位是不为空。单位：毫秒")
  private Long authCodeExpiresAt;
  @Schema(description = "验证码总有效时间。单位：毫秒")
  private Long authCodeTotalExpiresAt;
  @Schema(description = "使用对称密钥加密的 bkey, 盒子侧用的 key， 用来对应手机端和跳转后的盒子页面前端")
  private String bkey;
  @Schema(description = "使用对称密钥加密的 userDomain。")
  private String userDomain;
  @Schema(description = "使用对称密钥加密的 lanDomain。")
  private String lanDomain;
  @Schema(description = "使用对称密钥加密的 lanIp。")
  private String lanIp;

  @JsonIgnore
  private String clientUUID;
  @JsonIgnore
  private boolean authResult;
  @JsonIgnore
  private long createTime;
  @JsonIgnore
  private String userId;
  @JsonIgnore
  private String aoId;
  @JsonIgnore
  private Boolean autoLogin;
  @JsonIgnore
  private long autoLoginExpiresAt;

  @JsonCreator
  public CreateAuthCodeDTO(@JsonProperty("authCode") String authCode, @JsonProperty("bkey") String bkey) {
    this.authCode = authCode;
    this.bkey = bkey;
  }

  public CreateAuthCodeDTO(String aoId, String userId, String clientUUID, String authCode,
      String bkey, long createTime,Boolean autoLogin, long autoLoginExpiresAt) {
    this.aoId = aoId;
    this.userId = userId;
    this.clientUUID = clientUUID;
    this.authCode = authCode;
    this.bkey = bkey;
    this.createTime = createTime;
    this.autoLogin = autoLogin;
    this.autoLoginExpiresAt = autoLoginExpiresAt;
  }

  public CreateAuthCodeDTO(String bkey, long createTime, Boolean autoLogin, long autoLoginExpiresAt) {
    this.bkey = bkey;
    this.createTime = createTime;
    this.autoLogin = autoLogin;
    this.autoLoginExpiresAt = autoLoginExpiresAt;
  }
}
