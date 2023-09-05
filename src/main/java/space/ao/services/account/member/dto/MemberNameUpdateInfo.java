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

import jakarta.validation.constraints.Pattern;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * 管理员修改成员信息.
 *
 * @author suqin
 * @date 2021-10-12 21:40:10
 **/
public record MemberNameUpdateInfo(@NotBlank @Schema(description = "被修改者的aoId") String aoId,
                                   @NotBlank @Pattern(regexp = Const.SPACE_NAME_REG) @Schema(description = "需要修改的昵称") String nickName) {
  public static MemberNameUpdateInfo of(String aoId, String nickName) {
    return new MemberNameUpdateInfo(aoId, nickName);
  }
}

