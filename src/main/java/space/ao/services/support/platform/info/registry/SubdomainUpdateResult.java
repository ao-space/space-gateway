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

import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record SubdomainUpdateResult(@Schema(description = "是否成功") Boolean success,
                                    @Schema(description = "盒子的 UUID, success为true时返回") String boxUUID,
                                    @Schema(description = "用户id, success为true时返回") String userId,
                                    @Schema(description = "全局唯一的 subdomain, success为true时返回") String subdomain,
                                    @Schema(description = "错误码, success为false时返回") Integer code,
                                    @Schema(description = "错误消息, success为false时返回") String error,
                                    @Schema(description = "推荐的subdomain, success为false时返回") List<String> recommends) {
  public static SubdomainUpdateResult of(Boolean success, String boxUUID, String userId, String subdomain, Integer code, String error, List<String> recommends) {
    return new SubdomainUpdateResult(success, boxUUID, userId, subdomain, code, error, recommends);
  }
}