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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 用户割接信息
 */
public record UserMigrationInfo(@NotBlank @Schema(description = "用户的 ID") String userId,
                                @NotBlank @Schema(description = "用户被指定的用户域名字段") String userDomain,
                                @Schema(description = "用户类型（管理员、普通成员），取值：user_admin、user_member")
                                @ValueOfEnum(enumClass = RegistryTypeEnum.class, valueMethod = "getName") String userType,
                                @Valid @Schema(description = "Client 列表") List<ClientMigrationInfo> clientInfos) {
    public static UserMigrationInfo of(String userId, String userDomain, String userType, List<ClientMigrationInfo> clientInfos) {
        return new UserMigrationInfo(userId, userDomain, userType, clientInfos);
    }
}
