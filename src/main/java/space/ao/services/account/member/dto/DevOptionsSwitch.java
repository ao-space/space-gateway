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

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

public class DevOptionsSwitch {
  public static DevOptionsSwitch of(String status) {
    if (!"on".equalsIgnoreCase(status) && !"off".equalsIgnoreCase(status)) {
      throw new IllegalArgumentException("status can only be 'on' or 'off'");
    }
    var s = new DevOptionsSwitch();
    {
      s.status = status.toLowerCase();
    }
    return s;
  }

  @NotBlank @Schema(enumeration = {"on", "off"}, description = "开关状态: on, off") public String status;
}
