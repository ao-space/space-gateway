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

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "授权终端免扫码登录确认所需要的信息。")
public class AuthorizedTerminalLoginConfirmInfo {

  @Schema(description = "更新业务接口访问 token 的 token。")
  private @NotBlank String accessToken;
  @Schema(description = "对称密钥加密的clientuuid。")
  private @NotBlank String encryptedClientUUID;
  @Schema(description = "是否允许登录。")
  private Boolean login;
  @Schema(description = "是否自动登录。")
  private Boolean autoLogin;


}
