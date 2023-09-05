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

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class AdminBindInfo {
  @NotBlank
  @Schema(description = "clientUUID")
  private String clientUUID;

  @NotBlank
  @Schema(description = "phoneModel")
  private String phoneModel;

  @Schema(description = "applyEmail")
  private String applyEmail;

  @Schema(description = "spaceName")
  @Pattern(regexp = Const.SPACE_NAME_REG)
  private String spaceName;

  @NotBlank
  @Schema(description = "管理员密码")
  private String password;

  @Schema(description = "enableInternetAccess")
  private Boolean enableInternetAccess;
}
