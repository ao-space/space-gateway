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

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;


@Data
@AllArgsConstructor
@RegisterForReflection
public class SecurityTokenRes {
    @Schema(description = "安全密码/邮箱验证/绑定端确认/修改端提交 等等token。")
    @NotBlank(message = "不可为空")
    private String securityToken;

    @Schema(description = "该 token 失效时间. ")
    @NotBlank(message = "不可为空")
    private String expiredAt;

    public static SecurityTokenRes of(String securityToken, String expiredAt) {
        return new SecurityTokenRes(securityToken, expiredAt);
    }
}
