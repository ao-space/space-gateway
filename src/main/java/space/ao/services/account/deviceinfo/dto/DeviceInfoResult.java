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

package space.ao.services.account.deviceinfo.dto;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;


/**
 * 盒子容量总信息
 * @author suqin
 * @date 2021-10-08 21:39:57
 **/
@Data(staticConstructor = "of")
public class DeviceInfoResult {
  @Schema(description = "requestId")
  private final String requestId;

  @Schema(description = "总容量")
  private final String spaceSizeTotal;

  @Schema(description = "已用容量")
  private final String spaceSizeUsed;

  @Schema(description = "绑定设备类型")
  private final String phoneModel;
}
