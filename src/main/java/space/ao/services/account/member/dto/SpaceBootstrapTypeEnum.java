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

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SpaceBootstrapTypeEnum {
  SpaceBootstrapTypeBox("box"),
  SpaceBootstrapTypePc("pc"),
  SpaceBootstrapTypeOnline("online");

  private final String name;

  /**
   * -100 到 -199 当前 -100 虚拟机版本
   * -200 到 -299 当前 -200 云试用容器版本
   * -300 到 -399 当前 -300 PC容器版本
   * 100 到 199 当前 100 一代树莓派
   * 200 到 209 当前 200 二代开发板
   * 210 到 299当 前 210 二代正式板
   * @param boxDeviceModelNumber 设备种类编号
   */
  public static SpaceBootstrapTypeEnum getSpaceBootstrapType(Long boxDeviceModelNumber){
    if(boxDeviceModelNumber > 0){
      return SpaceBootstrapTypeBox;
    }

    if(boxDeviceModelNumber <= -200 && boxDeviceModelNumber >= -299){
      return SpaceBootstrapTypeOnline;
    }

    return SpaceBootstrapTypeBox;
  }

}
