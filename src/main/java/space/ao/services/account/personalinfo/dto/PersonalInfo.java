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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import space.ao.services.account.member.dto.Const;

/**
 * 修改用户名称和签名信息
 * @author suqin
 * @date 2021-10-08 21:39:57
 **/
@Data
public class PersonalInfo {
  @Schema(description = "用户名")
  @Pattern(regexp = Const.SPACE_NAME_REG)
  private String personalName;

  @Schema(description = "用户签名")
  private String personalSign;
  private String aoId;
  private String userDomain;
  private String phoneModel;

  @JsonCreator
  public PersonalInfo(@JsonProperty("personalName") String personalName,
                      @JsonProperty("personalSign") String personalSign,
                      @JsonProperty("aoId") String aoId,
                      @JsonProperty("userDomain") String userDomain,
                      @JsonProperty("phoneModel") String phoneModel) {
    this.personalName = personalName;
    this.personalSign = personalSign;
    this.aoId = aoId;
    this.userDomain = userDomain;
    this.phoneModel = phoneModel;
  }
}
