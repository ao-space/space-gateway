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

package space.ao.services.gateway.auth;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
@Schema(description = "解绑管理员客户端请求结果。")
public class RevokeClientResult {
  @Schema(description = "是否成功标记，true：成功，false：失败。")
  private final Boolean succeed;

  @Schema(description = "boxUUID")
  private final String boxUUID;

  @Schema(description = "失败次数")
  private final Integer errorTimes;

  @Schema(description = "剩余尝试次数")
  private final Integer leftTryTimes;

  @Schema(description = "用户剩余时间")
  private final Long tryAfterSeconds;

  public static RevokeClientResult of(boolean succeed) {
    return new RevokeClientResult(succeed, null, null, null, null);
  }
}
