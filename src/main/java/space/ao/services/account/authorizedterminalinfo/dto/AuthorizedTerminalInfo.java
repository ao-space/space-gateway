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

package space.ao.services.account.authorizedterminalinfo.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

public record AuthorizedTerminalInfo(@Schema(description = "用户userId") String userId,
                                     @NotBlank @Schema(description = "授权终端的uuid") String uuid,
                                     @Schema(description = "授权终端型号") String terminalMode,
                                     @Schema(description = "超时时间") long expireAt,
                                     @Schema(description = "登录地址") String address,
                                     @Schema(description = "授权终端类型") String terminalType){

  public static AuthorizedTerminalInfo of(String userId, String uuid, String terminalMode,
                                          long expireAt, String address, String terminalType) {
    return  new AuthorizedTerminalInfo(userId, uuid, terminalMode, expireAt,address,terminalType);
  }

}
