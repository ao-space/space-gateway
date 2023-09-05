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

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import space.ao.services.support.validator.ValueOfEnum;

@Data
@AllArgsConstructor(staticName = "of")
@RegisterForReflection
public class PushMessage {

  @ValueOfEnum(enumClass = MessageTypeEnum.class, valueMethod = "getName")
  @Schema(description = "消息发送类型,枚举值：clientcast-推送目标设备/broadcast-广播")
  private String type;

  @Schema(description = "盒子的 UUID")
  private String boxUUID;

  @Schema(description = "用户的 ID列表,当type=user_cast时,必填")
  private List<String> userIds;

  @Schema(description = "客户端的 UUID列表,当type=client_cast时,必填")
  private List<UserIdAndClientUUID> clientUUIDs;

  @Schema(description = "发送消息描述,可选")
  private String description;

  @Schema(description = "消息")
  private MessagePayload payload;

  @Schema(description = "发送策略")
  private MessagePolicy policy;

  @Schema(description = "厂商通道相关的特殊配置")
  private ChannelProperties channelProperties;

}
