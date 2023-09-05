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

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.Map;

/**
 * 成员所占用盒子空间信息
 *
 * @author suqin
 * @date 2021-10-08 21:39:57
 **/
public record MemberUsedStorageResult(@Schema(description = "返回码") String code,
                                      @Schema(description = "message信息") String message,
                                      @Schema(description = "requestId") String requestId,
                                      @Schema(description = "用户") Map<String, String> results) {
    public static MemberUsedStorageResult of(String code, String message, String requestId, Map<String, String> results) {
        return new MemberUsedStorageResult(code, message, requestId, results);
    }
}
