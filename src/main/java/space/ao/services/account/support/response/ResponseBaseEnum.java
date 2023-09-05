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

/*
 * Copyright (c) 2023 Institute of Software Chinese Academy of Sciences (ISCAS)
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

package space.ao.services.account.support.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import space.ao.services.support.response.ResponseBase;

@AllArgsConstructor
public enum ResponseBaseEnum {

  PERSONAL_NAME_IS_USED("ACC-400", "personal name is used"),
  SPACE_NAME_IS_USED("ACC-400", "space name is used"),

  ;
  @Getter
  private final String code;
  @Getter
  private final String message;

  public <T> space.ao.services.support.response.ResponseBase<T> getResponseBase(String requestId){
    return ResponseBase.of(this.code, this.message, requestId, null);
  }

}
