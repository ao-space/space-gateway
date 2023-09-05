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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 盒子割接信息
 */
public record BoxMigrationInfo(
        @Schema(description = "network client id；传参为空时将由平台重新生成") String networkClientId,
        @Valid @NotEmpty @Schema(description = "用户列表") List<UserMigrationInfo> userInfos) {
    public static BoxMigrationInfo of(String networkClientId, List<UserMigrationInfo> userInfos) {
        return new BoxMigrationInfo(networkClientId, userInfos);
    }
}
