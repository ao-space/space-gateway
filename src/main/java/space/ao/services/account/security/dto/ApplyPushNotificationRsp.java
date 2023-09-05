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
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Data(staticConstructor = "of")
@RegisterForReflection
public class ApplyPushNotificationRsp {
    @NotBlank
    @Schema(description = "允许/拒绝时请求参数 token")
    private final SecurityTokenRes securityTokenRes;

    @NotBlank
    @Schema(description = "授权端 userId")
    private final String authUserId;

    @NotBlank
    @Schema(description = "授权端 clientUUid")
    private final String authClientUUid;

    @NotBlank
    @Schema(description = "授权端设备信息")
    private final String authDeviceInfo;

    @NotBlank
    @Schema(description = "追溯请求 id")
    private final String requestId;

    @NotBlank
    @Schema(description = "本次申请的id")
    private final String applyId;
}
