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

package space.ao.services.support.platform.info.token;

import java.time.OffsetDateTime;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record TokenResult(@Schema(description = "平台id") String serviceId,
                          @Schema(description = "盒子在当前平台的注册码") String boxRegKey,
                          @Schema(description = "注册码 token 有效时间, OffsetDateTime 类型") OffsetDateTime expiresAt) {
  public static TokenResult of(String serviceId, String boxRegKey, OffsetDateTime expiresAt) {
    return new TokenResult(serviceId, boxRegKey, expiresAt);
  }
}
