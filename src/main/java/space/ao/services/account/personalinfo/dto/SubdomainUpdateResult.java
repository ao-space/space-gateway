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

import java.util.List;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
public class SubdomainUpdateResult {
  @Schema(description = "是否成功")
  private Boolean success;

  @Schema(description = "盒子的 UUID")
  private String boxUUID;
  @Schema(description = "用户id")
  private String userId;
  @Schema(description = "全局唯一的 subdomain")
  private  String subdomain;
  @Schema(description = "错误码, success为false时返回")
  private Integer code;
  @Schema(description = "错误消息, success为false时返回")
  private String error;
  @Schema(description = "推荐的subdomain, success为false时返回")
  private List<String> recommends;
}
