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

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import space.ao.services.gateway.auth.CreateTokenResult;
import space.ao.services.support.model.AlgorithmConfig;

/**
 * @param expiresAt             date-time string formatted like - 2007-12-03T10:15:30+01:00[Europe/Paris]
 * @param expiresAtEpochSeconds seconds from unix epoch - 1970-01-01T00:00:00Z
 */
public record EncryptAuthDTO(@Schema(description = "临时密钥(tmpEncryptedSecret) 加密的 box name") String boxName,
                             @Schema(description = "临时密钥(tmpEncryptedSecret) 加密的 box uuid") String boxUUID,
                             @Schema(description = "临时密钥(tmpEncryptedSecret) 加密的 aoid") String aoid,
                             @Schema(description = "业务接口访问 token。") String accessToken,
                             @Schema(description = "用于更新业务接口 token 的 token，该 token 本身也会被更新。") String refreshToken,
                             AlgorithmConfig algorithmConfig,
                             @Schema(description = "用于业务数据传输的对等密钥，该字段临时对称密钥加密，解密时使用请求时的临时密钥加上algorithmConfig里的动态iv") String encryptedSecret,
                             @Schema(description = "业务接口访问 token 的到期时间，该字段是一个字符串，格式为：2007-12-03T10:15:30+01:00[Europe/Paris]。") String expiresAt,
                             @Schema(description = "业务接口访问 token 的到期时间，该字段是一个长整型，具体表示从 unix 纪元（1970-01-01T00:00:00Z）开始的秒数。") Long expiresAtEpochSeconds,
                             @Schema(description = "请求标识，用于跟踪业务请求过程。") String requestId,
                             @Schema(description = "是否自动登录。") Boolean autoLogin,
                             @Schema(description = "自动登录的到期时间。") String autoLoginExpiresAt,
                             @Schema(description = "盒子局域网信息") BoxLanInfo boxLanInfo,
                             @Schema(description = "扩展信息")  String exContext) {
    public static EncryptAuthDTO of(String boxName, String boxUUID, String aoid, CreateTokenResult createTokenResult,
                                    Boolean autoLogin, String autoLoginExpiresAt,BoxLanInfo boxLanInfo, String exContext) {

        return new EncryptAuthDTO(boxName, boxUUID, aoid, createTokenResult.getAccessToken(),
                createTokenResult.getRefreshToken(), createTokenResult.getAlgorithmConfig(),
                createTokenResult.getEncryptedSecret(), createTokenResult.getExpiresAt(),
                createTokenResult.getExpiresAtEpochSeconds(), createTokenResult.getRequestId(),
                autoLogin, autoLoginExpiresAt, boxLanInfo, exContext);
    }
}

