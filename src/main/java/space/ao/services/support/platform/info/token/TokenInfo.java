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

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record TokenInfo(@NotBlank @Schema(description = "盒子的 UUID") String boxUUID,
                        @NotEmpty @Schema(description = "平台id：空间平台（serviceId=10001）、产品服务平台（serviceId=10002）") List<String> serviceIds,
                        @Schema(description = "签名，使用公钥验证盒子身份时必传") String sign) {
  public static TokenInfo of(String boxUUID, List<String> serviceIds, String sign) {
    return new TokenInfo(boxUUID, serviceIds, sign);
  }
}
