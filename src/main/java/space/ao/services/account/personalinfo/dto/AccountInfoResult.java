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

package space.ao.services.account.personalinfo.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * 成员基本信息
 * @author suqin
 * @date 2021-10-08 21:39:57
 **/
@Data(staticConstructor = "of")
public class AccountInfoResult {

  @Schema(description = "用户是admin还是guest")
  private final String role;

  @Schema(description = "用户昵称")
  private final String personalName;

  @Schema(description = "用户签名")
  private final String personalSign;

  @Schema(description = "用户创建时间")
  private final OffsetDateTime createAt;

  @Schema(description = "用户实际上的aoid")
  private final String aoId;

  @Schema(description = "用户clientUUID")
  private final String clientUUID;

  @Schema(description = "绑定手机型号")
  private final String phoneModel;

  @Schema(description = "用户域名")
  private final String userDomain;

  @Schema(description = "用户image的md5值")
  private final String imageMd5;

  @Schema(description = "用户已用空间")
  private final String userStorage;

  @Schema(description = "用户总空间")
  private final String totalStorage;

  @Schema(description = "didDoc 分布式身份标识信息")
  private final String did;
}
