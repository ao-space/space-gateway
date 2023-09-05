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

package space.ao.services.support.platform.info.registry;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record UserRegistryResult(@Schema(description = "盒子的 UUID") String boxUUID,
                                 @Schema(description = "用户的 ao ID") String userId,
                                 @Schema(description = "为用户分配的用户域名，该域名可以用于后续的业务访问") String userDomain,
                                 @Schema(description = "用户类型（管理员、普通成员），取值：user_admin、user_member") String userType,
                                 @Schema(description = "客户端的 UUID") String clientUUID) {
    public static UserRegistryResult of(String boxUUID, String userId, String userDomain, String userTyped, String clientUUID) {
        return new UserRegistryResult(boxUUID, userId, userDomain, userTyped,  clientUUID);
    }
}
