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

package space.ao.services.account.member.dto.migration;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import space.ao.services.support.platform.info.registry.RegistryTypeEnum;
import space.ao.services.support.validator.ValueOfEnum;

import jakarta.validation.constraints.NotBlank;

/**
 * 客户端割接信息
 */
public record ClientMigrationInfo(@NotBlank @Schema(description = "客户端的 UUID") String clientUUID,
                                  @NotBlank @Schema(description = "客户端类型（绑定、扫码授权），取值：client_bind、client_auth") @ValueOfEnum(enumClass = RegistryTypeEnum.class, valueMethod = "getName") String clientType) {
    public static ClientMigrationInfo of(String clientUUID, String clientType) {
        return new ClientMigrationInfo(clientUUID, clientType);
    }
}
