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

package space.ao.services.account.security.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import space.ao.services.account.security.service.SecurityMessageService;
import space.ao.services.account.security.utils.SignUtil;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.security.dto.SecurityMessagePollReq;
import space.ao.services.account.security.dto.SecurityMessageRsp;
import space.ao.services.support.log.Logged;
import space.ao.services.support.model.AccessToken;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 * 密码 http 接口类。
 */
@Path("/v1/api/security/message")
@Tag(name = "Security Message Service (since 1.0.7)", description = "安全密码相关的消息处理接口。")
public class SecurityMessagePoll {

    @Inject
    SecurityMessageService securityMessageService;

    @Inject
    SignUtil signUtil;
    private static final String INVALID_SIGN = "invalid_sign !";

    /**
     * 消息轮询接口.
     */
    @POST
    @Logged
    @Path("/poll")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "消息轮询接口.")
    public ResponseBase<List<SecurityMessageRsp>> poll(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                       @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam("userId") String userId,
                                                       @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam(AccessToken.AK_CLIENT_UUID)  String clientUUid,
                                                       @Valid SecurityMessagePollReq req) {
        return securityMessageService.poll(requestId, userId, clientUUid, req);
    }

    /**
     * 消息轮询接口(called by system-agent).
     */
    @POST
    @Logged
    @Path("/poll/local")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "消息轮询接口(called by system-agent).")
    public ResponseBase<List<SecurityMessageRsp>> pollInLocal(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                              @Schema(description = "调用者的 clientUuid") @NotBlank(message = "不可为空") @HeaderParam("clientUuid") String clientUuid,
                                                              @Schema(description = "使用盒子私钥对 clientUuid 签名, 签名算法用 sha256.") @NotBlank(message = "不可为空") @HeaderParam("clientUuidSign") String clientUuidSign,
                                                       @Valid SecurityMessagePollReq req) {

        if (signUtil.verifySign(requestId, clientUuid, clientUuidSign)) {
            return securityMessageService.pollInLocal(requestId, req);
        }
        return ResponseBase.forbidden(INVALID_SIGN, requestId);
    }
}
