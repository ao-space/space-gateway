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

package space.ao.services.account.member.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Data(staticConstructor = "of")
public class PasswdTryInfo {
  @NotBlank
  @Schema(description = "boxUUID")
  private final String boxUUID;

  @NotBlank
  @Schema(description = "失败次数")
  private final int errorTimes;

  @NotBlank
  @Schema(description = "剩余尝试次数")
  private final int leftTryTimes;

  @NotBlank
  @Schema(description = "用户剩余时间")
  private final long tryAfterSeconds;
}
