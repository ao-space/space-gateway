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

/**
 * Plain text(Json format) API Response Body:
 * <blockquote><pre>
 * {
 *    "code": 200,
 *    "message: "OK",
 *    "requestId": "xxx_xx", 请求追踪ID
 *    "body": { # 请求响应主体内容 (BASE64 + encrypted with secret key)
 *       "list": ["/a", "/b"]
 *       "pageInfo": {
 *          "page": 0,
 *          "pageSize": 10
 *          "total": 2
 *       }
 *    }
 * }
 * </pre></blockquote>
 */
public record RealCallResult(Integer code, String message, String requestId, String body) {
  public static RealCallResult of(Integer code, String message, String requestId, String body) {
    return new RealCallResult(code, message, requestId, body);
  }
}
