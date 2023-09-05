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

package space.ao.services.gateway.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import space.ao.services.support.model.AlgorithmConfig;

@Data(staticConstructor = "of")
@Schema(description = "创建、更新业务接口访问 token 成功返回的结果，同时返回对应的更新 token。")
public class CreateTokenResult {
  @Schema(description = "业务接口访问 token。")
  private final String accessToken;
  @Schema(description = "用于更新业务接口 token 的 token，该 token 本身也会被更新。")
  private final String refreshToken;
  private final AlgorithmConfig algorithmConfig;
  @Schema(description = "用于业务数据传输的对等密钥，该字段管理员通过客户端公钥/成员通过临时密钥加密，然后转换为 Base64。")
  private final String encryptedSecret; // symmetric key (BASE64 + encrypted with client public key)
  @Schema(description = "业务接口访问 token 的到期时间，该字段是一个字符串，格式为：2007-12-03T10:15:30+01:00[Europe/Paris]。")
  private final String expiresAt; // date-time string formatted like - 2007-12-03T10:15:30+01:00[Europe/Paris]
  @Schema(description = "业务接口访问 token 的到期时间，该字段是一个长整型，具体表示从 unix 纪元（1970-01-01T00:00:00Z）开始的秒数。")
  private final Long expiresAtEpochSeconds; // seconds from unix epoch - 1970-01-01T00:00:00Z
  @Schema(description = "请求标识，用于跟踪业务请求过程。")
  private final String requestId;
  @Schema(description = "自动登录的到期时间。")
  private String autoLoginExpiresAt;

  @Schema(description = "扩展信息")
  private String exContext;

  public CreateTokenResult(String accessToken, String refreshToken, AlgorithmConfig algorithmConfig, String encryptedSecret,
                           String expiresAt, Long expiresAtEpochSeconds, String requestId) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.algorithmConfig = algorithmConfig;
    this.encryptedSecret = encryptedSecret;
    this.expiresAt = expiresAt;
    this.expiresAtEpochSeconds = expiresAtEpochSeconds;
    this.requestId = requestId;
  }

  @JsonCreator
  public CreateTokenResult(@JsonProperty("accessToken") String accessToken,
                           @JsonProperty("refreshToken") String refreshToken,
                           @JsonProperty("algorithmConfig") AlgorithmConfig algorithmConfig,
                           @JsonProperty("encryptedSecret") String encryptedSecret,
                           @JsonProperty("expiresAt") String expiresAt,
                           @JsonProperty("expiresAtEpochSeconds") Long expiresAtEpochSeconds,
                           @JsonProperty("requestId") String requestId,
                           @JsonProperty("autoLoginExpiresAt") String autoLoginExpiresAt) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.algorithmConfig = algorithmConfig;
    this.encryptedSecret = encryptedSecret;
    this.expiresAt = expiresAt;
    this.expiresAtEpochSeconds = expiresAtEpochSeconds;
    this.requestId = requestId;
    this.autoLoginExpiresAt = autoLoginExpiresAt;
  }
}
