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

package space.ao.services.account.member.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record AdminBindResult(@Schema(description = "随机32位密钥") String authKey,
                              @Schema(description = "userdomain") String userDomain,
                              @Schema(description = "boxuuid") String boxUuid,
                              @Schema(description = "用户clientUUID") String clientUUID,
                              @Schema(description = "boxname") String boxName,
                              @Schema(description = "用户aoId") String aoId,
                              @Schema(description = "空间标识") String spaceName,
                              @Schema(description = "头像 url") String avatarUrl) {
  public static AdminBindResult of(String authKey, String userDomain, String boxUuid, String clientUUID, String boxName, String aoId, String spaceName, String avatarUrl) {
    return new AdminBindResult(authKey, userDomain, boxUuid, clientUUID, boxName, aoId, spaceName, avatarUrl);
  }
}
