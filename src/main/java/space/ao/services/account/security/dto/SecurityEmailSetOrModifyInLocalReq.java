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

package space.ao.services.account.security.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data(staticConstructor = "of")
public class SecurityEmailSetOrModifyInLocalReq {

    @Schema(description = "登录网关后授权令牌")
    @NotBlank(message = "不可为空")
    private String accessToken;

    @Schema(description = "新密保邮箱")
    @NotBlank(message = "不可为空")
    @Email(message = "邮箱格式错误")
    private String emailAccount;

    @Schema(description = "新邮箱密码,使用16进制Hex编码")
    @NotBlank(message = "不可为空")
    private String emailPasswd;

    @Schema(description = "邮箱服务器,默认使用 smtp")
    @NotBlank(message = "不可为空")
    private String host;

    @Schema(description = "邮箱端口号")
    @NotBlank(message = "不可为空")
    private String port;

    @Schema(description = "邮箱服务是否启用 ssl")
    private boolean sslEnable;
}
