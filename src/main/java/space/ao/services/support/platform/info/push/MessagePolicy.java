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

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@AllArgsConstructor(staticName = "of")
public class MessagePolicy {
    @Schema(description = "可选，定时发送时，若不填写表示立即发送。格式: YYYY-MM-DD hh:mm:ss")
    private String startTime;

    @Schema(description = "可选，消息过期时间，其值不可小于发送时间或者startTime。如果不填写此参数，默认为3天后过期。格式: YYYY-MM-DD hh:mm:ss")
    private String expireTime;
}
