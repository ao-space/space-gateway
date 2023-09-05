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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Plain text(Json format) API Request Body:
 * <blockquote><pre>
 * {
 *    "requestId": "xxx_xx", 请求追踪 ID
 *    "serviceName": "file_system", # 空间应用服务名称
 *    "apiName": "list_folders", # API 名称
 *    "apiVersion": "v1", # API 版本
 *    "entity": {} # 调用的涉及的实体内容（JSON格式）
 *    "queries": { # 方法调用参数（以 HTTP 为例，作为 URL 的 query 部分）
 *       "current": "/",
 *       "page": 0,
 *       "pageSize": 10
 *    },
 *    "headers": {}
 * }
 * </pre></blockquote>
 */
@Data
@NoArgsConstructor
public class RealCallRequest {
  @JsonCreator
  public RealCallRequest(@JsonProperty("requestId") String requestId,
                         @JsonProperty("serviceName") String serviceName,
                         @JsonProperty("apiName") String apiName,
                         @JsonProperty("apiVersion") String apiVersion,
                         @JsonProperty("headers") Map<String, String> headers,
                         @JsonProperty("queries") Map<String, String> queries,
                         @JsonProperty("entity") Object entity) {
    this.requestId = requestId;
    this.serviceName = serviceName;
    this.apiName = apiName;
    this.apiVersion = apiVersion;
    this.headers = headers;
    this.queries = queries;
    this.entity = entity;
  }

  @JsonSerialize
  private String requestId;
  @JsonSerialize
  private String serviceName;
  @JsonSerialize
  private String apiName;
  @JsonSerialize
  private String apiVersion;
  @JsonSerialize
  private Map<String, String> headers;
  @JsonSerialize
  private Map<String, String> queries;
  @JsonSerialize
  private Object entity;
}
