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
import space.ao.services.account.security.dto.*;
import space.ao.services.account.security.service.SecurityPasswordAuthorService;
import space.ao.services.account.security.service.SecurityPasswordBinderService;
import space.ao.services.account.security.service.SecurityPasswordNewDeviceService;
import space.ao.services.account.security.utils.SignUtil;
import space.ao.services.account.security.utils.UserRoleUtil;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.support.TokenUtils;
import space.ao.services.support.limit.LimitReq;
import space.ao.services.support.log.Logged;
import space.ao.services.support.model.AccessToken;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 * 密码 http 接口类。
 */
@Path("/v1/api/security/passwd")
@Tag(name = "Security Password Service (since 1.0.7)", description = "密码 http 接口类。提供修改/重置等接口。")
public class SecurityPasswordResource {
    @Inject
    SecurityPasswordBinderService securityPasswordBinderService;

    @Inject
    SecurityPasswordAuthorService securityPasswordAuthorService;

    @Inject
    SecurityPasswordNewDeviceService securityPasswordNewDeviceService;

    @Inject
    UserRoleUtil userRoleUtil;

    @Inject
    SignUtil signUtil;

    @Inject
    TokenUtils tokenUtils;

    private static final String USER_ROLE_ERROR = "user role error";
    private static final String INVALID_CLIENTUUID = "invalid_clientuuid !";
    private static final String INVALID_SIGN = "invalid_sign !";
    private static final String ACCESS_TOKEN_ERROR = "access token error";

    /**
     * 验证安全密码(获取 SecurityTokenRes). 其他两步验证需要先调用本接口作为第一步验证.
     * @param requestId 请求 id
     * @return SecurityToken。
     * @since 1.0.7
     */
    @POST
    @Logged
    @Path("/verify")
    @LimitReq(keyPrefix="SCREQRATE-")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "验证安全密码(获取 SecurityTokenRes). 其他两步验证需要先调用本接口作为第一步验证")
    public ResponseBase<SecurityTokenRes> securityPasswdVerify(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                               @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam("userId") String userId,
                                                               @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam(AccessToken.AK_CLIENT_UUID)  String clientUUid,
                                                               @Valid SecurityPasswdCheckReq req) {
        return securityPasswordBinderService.securityPasswdVerify(requestId, clientUUid, req);
    }

    /**
     * 绑定端直接修改(使用原密码)
     * @param requestId 请求 id
     * @return ResponseBase。
     * @since 1.0.7
     */
    @POST
    @Logged
    @Path("/modify/binder")
    @LimitReq(keyPrefix="SCREQRATE-")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "绑定端直接修改(使用原密码)")
    public ResponseBase<Object> securityPasswdModifyByBinder(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                     @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam("userId") String userId,
                                                     @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam(AccessToken.AK_CLIENT_UUID)  String clientUUid,
                                              @Valid SecurityPasswdModifyByBinderReq req) {
        if (!userRoleUtil.adminBindRole(clientUUid)) {
            return ResponseBase.forbidden("user role error", requestId);
        }

        return securityPasswordBinderService.securityPasswdModifyByBinder(requestId, req);
    }

    /**
     * 绑定端直接重置(called by system-agent)
     * @param requestId 请求 id
     * @return ResponseBase。
     * @since 1.0.7
     */
    @POST
    @Logged
    @Path("/reset/binder/local")
    @LimitReq(keyPrefix="SCREQRATE-")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "绑定端直接重置(called by system-agent)")
    public ResponseBase<Object> securityPasswdResetByBinderInLocal(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                           @Schema(description = "配对的 clientUuid") @NotBlank(message = "不可为空") @HeaderParam("clientUuid") String clientUuid,
                                                           @Schema(description = "使用盒子私钥对 clientUuid 签名, 签名算法用 sha256.") @NotBlank(message = "不可为空") @HeaderParam("clientUuidSign") String clientUuidSign,
                                                            @Valid SecurityPasswdResetByBinderInLocalReq req) {
        if (!signUtil.verifySign(requestId, clientUuid, clientUuidSign)) {
            return ResponseBase.forbidden(INVALID_SIGN, requestId);
        }
        if (!userRoleUtil.adminBindRole(clientUuid)) {
            return ResponseBase.forbidden(INVALID_CLIENTUUID, requestId);
        }
        var ak = tokenUtils.verifyAccessToken(requestId, req.getAccessToken());
        if (null==ak) {
            return ResponseBase.forbidden(ACCESS_TOKEN_ERROR, requestId);
        }
        if (!userRoleUtil.adminBindRole(ak.getClientUUID())) {
            return ResponseBase.forbidden(USER_ROLE_ERROR, requestId);
        }
        return securityPasswordBinderService.securityPasswdResetByBinderInLocal(requestId, req);
    }

    /**
     * 授权端请求修改
     * @param requestId 请求 id
     * @return ResponseBase。
     * @since 1.0.7
     */
    @POST
    @Logged
    @Path("/modify/auther/apply")
    @LimitReq(keyPrefix="SCREQRATE-", max = 3) // 上线前改成 3 次! 临时测试方便可以改大.
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "授权端请求修改")
    public ResponseBase<Object>  securityPasswdModifyAutherApply(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                    @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam("userId") String userId,
                                                    @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam(AccessToken.AK_CLIENT_UUID)  String clientUUid,
                                                        @Valid ApplyInfoReq req) {
        if (!userRoleUtil.adminAuthRole(userId, clientUUid)) {
            return ResponseBase.forbidden("user role error", requestId);
        }
        return securityPasswordAuthorService.securityPasswdModifyAuthorApply(requestId, userId, clientUUid, req);
    }

    /**
     * 绑定端允许修改
     * @param requestId requestId
     * @param userId userId
     * @param clientUUid clientUUid
     * @param req SecurityPasswdModifyBinderAcceptReq
     * @return 修改结果
     */
    @POST
    @Logged
    @Path("/modify/binder/accept")
    @LimitReq(keyPrefix="SCREQRATE-")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "绑定端允许修改")
    public ResponseBase<Object> securityPasswdModifyBinderAccept(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                        @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam("userId") String userId,
                                                        @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam(AccessToken.AK_CLIENT_UUID)  String clientUUid,
                                                         @Valid SecurityPasswdModifyBinderAcceptReq req) {
        if (!userRoleUtil.adminBindRole(clientUUid)) {
            return ResponseBase.forbidden("user role error", requestId);
        }
        return securityPasswordAuthorService.securityPasswdModifyBinderAccept(requestId, userId, clientUUid, req);
    }

    /**
     * 授权端提交修改
     * @param requestId requestId
     * @param userId userId
     * @param clientUUid clientUUid
     * @param req SecurityPasswdModifyAutherReq
     * @return 提交结果
     */
    @POST
    @Logged
    @Path("/modify/auther")
    @LimitReq(keyPrefix="SCREQRATE-")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "授权端提交修改")
    public ResponseBase<Object> securityPasswdModifyAuther(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                        @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam("userId") String userId,
                                                        @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam(AccessToken.AK_CLIENT_UUID)  String clientUUid,
                                                        @Valid SecurityPasswdModifyAutherReq req) {
        if (!userRoleUtil.adminAuthRole(userId, clientUUid)) {
            return ResponseBase.forbidden("user role error", requestId);
        }
        return securityPasswordAuthorService.securityPasswdModifyAuthor(requestId, userId, clientUUid, req);
    }

    /**
     * 授权端请求重置
     * @param requestId 请求 id
     * @return ResponseBase。
     * @since 1.0.7
     */
    @POST
    @Logged
    @Path("/reset/auther/apply")
    @LimitReq(keyPrefix="SCREQRATE-", max = 3) // 上线前改成 3 次! 临时测试方便可以改大.
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "授权端请求重置")
    public ResponseBase<Object> securityPasswdResetAutherApply(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                        @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam("userId") String userId,
                                                        @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam(AccessToken.AK_CLIENT_UUID)  String clientUUid,
                                                       @Valid ApplyInfoReq req) {
        if (!userRoleUtil.adminAuthRole(userId, clientUUid)) {
            return ResponseBase.forbidden("user role error", requestId);
        }
        return securityPasswordAuthorService.securityPasswdResetAuthorApply(requestId, userId, clientUUid, req);
    }

    /**
     * 绑定端允许重置
     * @param requestId requestId
     * @param userId userId
     * @param clientUUid clientUUid
     * @param req SecurityPasswdResetBinderAcceptReq
     * @return 重置结果
     */
    @POST
    @Logged
    @Path("/reset/binder/accept")
    @LimitReq(keyPrefix="SCREQRATE-")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "绑定端允许重置")
    public ResponseBase<Object> securityPasswdResetBinderAccept(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                         @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam("userId") String userId,
                                                         @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam(AccessToken.AK_CLIENT_UUID)  String clientUUid,
                                                         @Valid SecurityPasswdResetBinderAcceptReq req) {
        if (!userRoleUtil.adminBindRole(clientUUid)) {
            return ResponseBase.forbidden("user role error", requestId);
        }
        return securityPasswordAuthorService.securityPasswdResetBinderAccept(requestId, clientUUid, req);
    }

    /**
     * 授权端提交重置(called by system-agent)
     * @param requestId requestId
     * @param clientUuid clientUuid
     * @param clientUuidSign clientUuidSign
     * @param req SecurityPasswdResetAutherInLocalReq
     * @return 重置结果
     */
    @POST
    @Logged
    @Path("/reset/auther/local")
    @LimitReq(keyPrefix="SCREQRATE-")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "授权端提交重置(called by system-agent)")
    public ResponseBase<Object> securityPasswdResetAutherInLocal(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                         @Schema(description = "配对的 clientUuid") @NotBlank(message = "不可为空") @HeaderParam("clientUuid") String clientUuid,
                                                         @Schema(description = "使用盒子私钥对 clientUuid 签名, 签名算法用 sha256.") @NotBlank(message = "不可为空") @HeaderParam("clientUuidSign") String clientUuidSign,
                                                  @Valid SecurityPasswdResetAutherInLocalReq req) {
        if (!signUtil.verifySign(requestId, clientUuid, clientUuidSign)) {
            return ResponseBase.forbidden(INVALID_SIGN, requestId);
        }
        if (!userRoleUtil.adminBindRole(clientUuid)) {
            return ResponseBase.forbidden(INVALID_CLIENTUUID, requestId);
        }
        var ak = tokenUtils.verifyAccessToken(requestId, req.getAccessToken());
        if (null==ak) {
            return ResponseBase.forbidden(ACCESS_TOKEN_ERROR, requestId);
        }
        if (!userRoleUtil.adminAuthRole("1", ak.getClientUUID())) {
            return ResponseBase.forbidden(USER_ROLE_ERROR, requestId);
        }
        return securityPasswordAuthorService.securityPasswdResetAuthorInLocal(requestId, clientUuid, req);
    }

    /**
     * 新设备申请重置(called by system-agent)
     * @param requestId requestId
     * @param clientUuid clientUuid
     * @param clientUuidSign clientUuidSign
     * @param req ApplyInfoNewDeviceReq
     * @return 申请结果
     */
    @POST
    @Logged
    @Path("/reset/newdevice/apply/local")
    @LimitReq(keyPrefix="SCREQRATE-", max = 3) // 上线前改成 3 次! 临时测试方便可以改大.
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "新设备申请重置(called by system-agent)")
    public ResponseBase<Object> securityPasswdResetNewDeviceApplyInLocal(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                                 @Schema(description = "配对的 clientUuid") @NotBlank(message = "不可为空") @HeaderParam("clientUuid") String clientUuid,
                                                                 @Schema(description = "使用盒子私钥对 clientUuid 签名, 签名算法用 sha256.") @NotBlank(message = "不可为空") @HeaderParam("clientUuidSign") String clientUuidSign,
                                                                 @Valid ApplyInfoNewDeviceReq req) {
        if (!signUtil.verifySign(requestId, clientUuid, clientUuidSign)) {
            return ResponseBase.forbidden(INVALID_SIGN, requestId);
        }
        if (!userRoleUtil.adminBindRole(clientUuid)) {
            return ResponseBase.forbidden(INVALID_CLIENTUUID, requestId);
        }
        return securityPasswordNewDeviceService.securityPasswdResetNewDeviceApplyInLocal(requestId, clientUuid, req);
    }

    /**
     * 新设备提交重置(called by system-agent)
     * @param requestId requestId
     * @param btid btid 蓝牙
     * @param btidSign btid 签名
     * @param req SecurityPasswdResetNewDeviceReq
     * @return 重置结果
     */
    @POST
    @Logged
    @Path("/reset/newdevice/local")
    @LimitReq(keyPrefix="SCREQRATE-")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "新设备提交重置(called by system-agent)")
    public ResponseBase<Object> securityPasswdResetNewDeviceInLocal(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
        @Schema(description = "配对的 btid")  @HeaderParam("btid") String btid,
        @Schema(description = "使用盒子私钥对 btid 签名, 签名算法用 sha256.")  @HeaderParam("btidSign") String btidSign,
        @Schema(description = "配对的 clientUuid")  @HeaderParam("clientUuid") String clientUuid,
        @Schema(description = "使用盒子私钥对 clientUuid 签名, 签名算法用 sha256.")  @HeaderParam("clientUuidSign") String clientUuidSign,
        @Valid SecurityPasswdResetNewDeviceReq req) {
        if (!signUtil.verifySign(requestId, btid, btidSign) && !signUtil.verifySign(requestId, clientUuid,clientUuidSign)) {
            return ResponseBase.forbidden(INVALID_SIGN, requestId);
        }
        return securityPasswordNewDeviceService.securityPasswdResetNewDeviceInLocal(requestId,
                req.getApplyId(),
                req.getNewDeviceClientUuid(),
                req);
    }

}
