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

package space.ao.services.gateway.auth.qrcode.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@NoArgsConstructor
@Schema(description = "创建二维码接口 需要提供的信息。")
public class CreateAuthCodeInfo {
    @Schema(description = "业务接口访问 token。")
    String accessToken;
    @Schema(description = "使用对称密钥加密后的 auth-key。")
    String authKey;
    @Schema(description = "使用对称密钥加密后的 client-uuid。")
    String clientUUID;
    @Schema(description = "使用对称密钥加密后的 box name。")
    String boxName;
    @Schema(description = "使用对称密钥加密后的 box uuid。")
    String boxUUID;
    @Schema(description = "授权码版本")
    String version;
    @JsonCreator
    public CreateAuthCodeInfo(@JsonProperty String accessToken, @JsonProperty String authKey, @JsonProperty String clientUUID,
                              @JsonProperty String boxName, @JsonProperty String boxUUID, @JsonProperty String version){
        this.accessToken = accessToken;
        this.authKey = authKey;
        this.clientUUID = clientUUID;
        this.boxName = boxName;
        this.boxUUID = boxUUID;
        this.version = version;
    }
}
