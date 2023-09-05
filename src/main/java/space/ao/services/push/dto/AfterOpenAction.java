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

package space.ao.services.push.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum AfterOpenAction {
  GO_APP("go_app", "打开应用"),
  GO_URL("go_url", "跳转到URL"),
  GO_ACTIVITY("go_activity", "打开特定的activity"),
  GO_CUSTOM("go_custom", "用户自定义内容"),
  ;

  @Getter
  private final String name;

  @Getter
  private final String desc;

}