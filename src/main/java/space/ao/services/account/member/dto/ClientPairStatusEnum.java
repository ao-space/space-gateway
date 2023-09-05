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

/**
 * @author zhichuang
 **/
public enum ClientPairStatusEnum {

  CLIENT_PAIRED("0", "已经配对"),
  CLIENT_UNPAIRED_NEW_BOX("1", "新盒子"),
  CLIENT_UNPAIRED_UNBIND("2", "已解绑");

  private final String status;
  private final String description;

  ClientPairStatusEnum(String status, String description) {
    this.status = status;
    this.description = description;
  }

  public String getStatus() {
    return status;
  }

  public String getDescription() {
    return description;
  }

}
