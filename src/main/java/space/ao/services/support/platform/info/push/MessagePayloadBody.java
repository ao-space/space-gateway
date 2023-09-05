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

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import space.ao.services.support.validator.ValueOfEnum;
import space.ao.services.push.dto.AfterOpenAction;

@Data
@AllArgsConstructor(staticName = "of")
public class MessagePayloadBody {
    @Schema(description = "通知文字描述，当displayType=notification时必填")
    private String text;

    @Schema(description = "通知标题，当displayType=notification时必填")
    private String title;

    @ValueOfEnum(enumClass = AfterOpenAction.class, valueMethod = "getName")
    @Schema(description = "点击通知的后续行为(默认为打开app)，当displayType=notification时必填")
    private String afterOpen;

    @Schema(description = "通知栏点击后跳转的URL，当after_open=go_url时必填")
    private String url;

    @Schema(description = "通知栏点击后打开的Activity，当afterOpen=go_activity时必填")
    private String activity;

    @Schema(description = "用户自定义内容，可以为字符串或者JSON格式。当display_type=message时,或者当display_type=notification且after_open=go_custom时，必填")
    private String custom;

    @Data
    @AllArgsConstructor(staticName = "of")
    public static class Message {
        @NotBlank
        @Schema(description = "消息 id")
        private String messageId;
    }

}
