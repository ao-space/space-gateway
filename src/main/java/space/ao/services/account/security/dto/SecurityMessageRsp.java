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
public class SecurityMessageRsp {
    @Schema(description = "消息类型")
    @NotBlank(message = "不可为空")
    private String msgType;

    @Schema(description = "获取到的 securityToken")
    @NotBlank(message = "不可为空")
    private SecurityTokenRes securityTokenRes;

    @Schema(description = "绑定端的 clientUuid")
    @NotBlank(message = "不可为空")
    private String clientUuid;

    @Schema(description = "true 允许, false 拒绝")
    @NotBlank(message = "不可为空")
    private boolean accept;

    @Schema(description = "追溯请求 id")
    @NotBlank(message = "不可为空")
    private String requestId;

    @Schema(description = "本次申请的id")
    @NotBlank(message = "不可为空")
    private String applyId;

    public static SecurityMessageRsp of(String msgType,
                                        SecurityTokenRes securityTokenRes,
                                        String clientUuid,
                                        boolean accept,
                                        String requestId,
                                        String applyId) {
        return new SecurityMessageRsp(msgType, securityTokenRes, clientUuid, accept, requestId,
                applyId);
    }
}
