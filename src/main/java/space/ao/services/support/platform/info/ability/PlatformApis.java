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

package space.ao.services.support.platform.info.ability;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import space.ao.services.support.validator.ValueOfEnum;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class PlatformApis {
  @JsonCreator
  public PlatformApis(@JsonProperty("version") String version,
                      @JsonProperty("services") Map<String, Map<String, PlatformApi>> services) {
    this.version = version;
    this.services = services;
  }

  private String version;
  private Map<String, Map<String, PlatformApi>> services;

  public record PlatformApi(@Schema(description = "http method") String method,
                            @Schema(description = "uri，如/platform/v*/api/registry/box") String uri,
                            @Schema(description = "简略uri，以四级目录开头，如/registry/box") String briefUri,
                            @Schema(description = "兼容的API版本列表，如1，2，3") List<Integer> compatibleVersions,
                            @Schema(description = "API分类（空间平台基础api、空间平台扩展api、产品服务api），取值：basic_api、extension_api、product_service_api") @ValueOfEnum(enumClass = PlatformApiTypeEnum.class, valueMethod = "getName") String type,
                            @Schema(description = "API描述") String desc) {
  }


}
