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

package space.ao.services.gateway;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class Routers {
  @JsonCreator
  public Routers(@JsonProperty("version") String version,
                 @JsonProperty("services") Map<String, Map<String, Router>> services) {
    this.version = version;
    this.services = services;
  }

  private String version;
  private Map<String, Map<String, Router>> services;

  @Data
  @NoArgsConstructor
  public static class Router {
    @JsonCreator
    public Router(@JsonProperty("type") String type, @JsonProperty("method") String method,
                  @JsonProperty("protocol") String protocol, @JsonProperty("url") String url,
                  @JsonProperty("ext-protocol") String extProtocol,
                  @JsonProperty("open-api") OpenApi openApi) {
      this.type = type;
      this.method = method;
      this.protocol = protocol;
      this.url = url;
      this.extProtocol = extProtocol;
      this.openApi = openApi;
    }

    private String type;
    private String method;
    private String protocol;
    private String url;
    private String extProtocol;
    private OpenApi openApi;
  }

  @Data
  @NoArgsConstructor
  public static class OpenApi {
    @JsonCreator
    public OpenApi(@JsonProperty("scope") String scope, @JsonProperty("rate_limit") Integer rateLimit) {
      this.scope = scope;
      this.rateLimit = rateLimit;
    }

    private String scope;
    private Integer rateLimit;
  }
}
