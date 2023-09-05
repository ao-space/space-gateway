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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 成员创建后的返回信息.
 * @author suqin
 * @date 2021-10-12 21:40:10
 **/
public record MemberCreateResult(@Schema(description = "随机32位密钥") String authKey,
                                 @Schema(description = "用户userid") String userid,
                                 @Schema(description = "用户clientUUID") String clientUUID,
                                 @Schema(description = "用户aoId") String aoId,
                                 @Schema(description = "用户userdomain") String userDomain,
                                 @JsonIgnore String phoneModel, @JsonIgnore String phoneType) {

  public static MemberCreateResult of(String authKey, String userid, String clientUUID, String aoId, String userDomain) {
    return new MemberCreateResult(authKey, userid, clientUUID, aoId, userDomain, null, null);
  }

  public MemberCreateResult setPhoneModel(String phoneModel) {
    return new MemberCreateResult(authKey(), userid(), clientUUID(), aoId(), userDomain(), phoneModel, phoneType());
  }
}