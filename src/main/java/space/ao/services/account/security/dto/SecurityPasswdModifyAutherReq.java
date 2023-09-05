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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data(staticConstructor = "of")
public class SecurityPasswdModifyAutherReq {
    @Schema(description = "获取到的 securityToken")
    @NotBlank(message = "不可为空")
    private String securityToken;

    @Schema(description = "绑定端的 clientUuid")
    @NotBlank(message = "不可为空")
    private String clientUuid;

    @Schema(description = "原密码")
    @NotBlank(message = "不可为空")
    private String oldPasswd;

    @Schema(description = "新密码")
    @Size(min = 6, message = "长度必须是6位或以上")
    private String newPasswd;

    @Schema(description = "本次申请的id")
    private String applyId;
}
