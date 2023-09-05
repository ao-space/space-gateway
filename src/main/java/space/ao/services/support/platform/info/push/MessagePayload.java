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

package space.ao.services.support.platform.info.push;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import space.ao.services.support.validator.ValueOfEnum;

@Data
@AllArgsConstructor(staticName = "of")
public class MessagePayload {
  @ValueOfEnum(enumClass = DisplayTypeEnum.class, valueMethod = "getName")
  @Schema(description = "消息类型，枚举：notification-通知/message-消息")
  private String displayType;

  @Schema(description = "消息体")
  private MessagePayloadBody body;

  @Schema(description = "可选，Map格式，用户自定义key-value")
  private Map<String, String> extra;
}
