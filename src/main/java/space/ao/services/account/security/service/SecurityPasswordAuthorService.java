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

package space.ao.services.account.security.service;

import org.jboss.logging.Logger;
import space.ao.services.account.security.dto.*;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.security.utils.PushUtils;
import space.ao.services.account.security.utils.SecurityPasswordUtils;
import space.ao.services.account.security.utils.token.SecurityTokenType;
import space.ao.services.account.security.utils.token.SecurityTokenUtils;
import space.ao.services.account.support.service.ServiceError;
import space.ao.services.account.support.service.ServiceOperationException;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.push.dto.NotificationEnum;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * 密码逻辑服务类。
 */
@ApplicationScoped
public class SecurityPasswordAuthorService {

    @Inject
    SecurityTokenUtils securityTokenUtils;

    @Inject
    SecurityPasswordUtils securityPasswordUtils;

    @Inject
    PushUtils pushUtils;

    @Inject
    ApplicationProperties properties;

    @Inject
    SecurityPasswordCommonService securityPasswordCommonService;

    static final Logger LOG = Logger.getLogger("app.log");

    /**
     * 授权端请求修改
     * @param requestId requestId
     * @param userId userId
     * @param clientUUid clientUUid
     * @return 请求结果
     */
    public ResponseBase<Object> securityPasswdModifyAuthorApply(String requestId,
                                                                String userId,
                                                                String clientUUid,
                                                                ApplyInfoReq req) {

        var expiresAt = ZonedDateTime.now().plusSeconds(
                Duration.parse(properties.gatewayTimeOfSecurityPasswdAkLife()).toSeconds());

        pushUtils.doPushApply(requestId, req.getApplyId(),
                NotificationEnum.SECURITY_PASSWD_MOD_APPLY,
                userId, clientUUid, req.getDeviceInfo(),
                SecurityTokenType.TOKEN_TYPE_APPLY_MODIFY_ADMIN_PWD,
                expiresAt);
        LOG.debug("securityPasswdModifyAutherApply succ, req="+req);
        return ResponseBase.okACC(requestId, null);
    }

    /**
     * 绑定端允许修改
     * @param requestId requestId
     * @param userId userId
     * @param clientUUid clientUUid
     * @return 验证结果
     */
    public ResponseBase<Object> securityPasswdModifyBinderAccept(String requestId,
                                                        String userId,
                                                        String clientUUid,
                                                         SecurityPasswdModifyBinderAcceptReq req) {

        var securityToken = securityTokenUtils.verifySecurityToken(requestId, req.getSecurityToken(),
                SecurityTokenType.TOKEN_TYPE_APPLY_MODIFY_ADMIN_PWD, req.getClientUuid());
        if (securityToken == null) {
            return ResponseBase.forbidden("verifySecurityToken error", requestId);
        }

        pushUtils.doPushAccept(requestId, req.getApplyId(),
                NotificationEnum.SECURITY_PASSWD_PARTICULAR_MOD_ACCEPT,
                req.getClientUuid(), clientUUid,
                req.isAccept(),
                SecurityTokenType.TOKEN_TYPE_MODIFY_ADMIN_PWD);

        LOG.debugv("securityPasswdResetAuther succ, req={0}, userId: {1}", req, userId);
        return ResponseBase.okACC(requestId, null);
    }

    /**
     * 授权端提交修改
     * @param requestId requestId
     * @param userId userId
     * @param clientUUid clientUUid
     * @return 修改结果
     */
    public ResponseBase<Object> securityPasswdModifyAuthor(String requestId,
                                                           String userId,
                                                           String clientUUid,
                                                           SecurityPasswdModifyAutherReq req) {

        var securityToken = securityTokenUtils.verifySecurityToken(requestId, req.getSecurityToken(),
                SecurityTokenType.TOKEN_TYPE_MODIFY_ADMIN_PWD, req.getClientUuid());
        if(securityToken == null){
            // 授权端申请重置密码的token，在修改安全密码时也可以使用
            securityToken = securityTokenUtils.verifySecurityToken(requestId, req.getSecurityToken(),
                    SecurityTokenType.TOKEN_TYPE_RESET_ADMIN_PWD, req.getClientUuid());
        }
        if (securityToken == null) {
            throw new ServiceOperationException(ServiceError.EMAIL_VERIFICATION_TOKEN_TIMEOUT);
        }

        if (!securityPasswordUtils.doVerifyAndModifyPasswd(requestId, req.getOldPasswd(), req.getNewPasswd())) {
            return ResponseBase.forbidden("password error", requestId);
        }

        pushUtils.doPushSucc(requestId,
                NotificationEnum.SECURITY_PASSWD_MOD_SUCC);
        LOG.debugv("securityPasswdResetAuther succ, req={0}, userId: {1}, clientUUID {2}", req, userId, clientUUid);
        return ResponseBase.okACC(requestId, null);
    }

    /**
     * 授权端请求重置
     * @param requestId requestId
     * @param userId userId
     * @param clientUUid clientUUid
     * @return 请求结果
     */
    public ResponseBase<Object> securityPasswdResetAuthorApply(String requestId,
                                                               String userId,
                                                               String clientUUid,
                                                               ApplyInfoReq req) {
        var r = new ApplyInfoNewDeviceReq();
        r.setDeviceInfo(req.getDeviceInfo());
        r.setClientUuid(clientUUid);
        return securityPasswordCommonService.securityPasswdResetAuthorApply( requestId, req.getApplyId(),
                userId,
                clientUUid,
                r,
                false);
    }

    /**
     * 绑定端允许重置
     * @param requestId requestId
     * @param clientUUid clientUUid
     * @return 验证结果
     */
    public ResponseBase<Object> securityPasswdResetBinderAccept(String requestId,
                                                         String clientUUid,
                                                        SecurityPasswdResetBinderAcceptReq req) {

        return securityPasswordCommonService.securityPasswdResetBinderAccept( requestId, req.getApplyId(),
                clientUUid,
                req);
    }

    /**
     * 授权端提交重置(通过密保邮箱)
     * @param requestId requestId
     * @param userId userId
     * @param clientUUid clientUUid
     * @return 重置结果
     */
    public ResponseBase<Object> securityPasswdResetAuthor(String requestId,
                                                          String userId,
                                                          String clientUUid,
                                                          SecurityPasswdResetAutherReq req) {
        var r = new SecurityPasswdResetNewDeviceReq();
        r.setAcceptSecurityToken(req.getAcceptSecurityToken());
        r.setEmailSecurityToken(req.getEmailSecurityToken());
        r.setClientUuid(req.getClientUuid());
        r.setNewPasswd(req.getNewPasswd());
        return securityPasswordCommonService.securityPasswdResetAuthorOrNewDevice( requestId,
                 req.getApplyId(),
                 userId,
                 clientUUid,
                 r,
                false);
    }

    /**
     * 授权端提交重置(called by system-agent)
     * @param requestId requestId
     * @param clientUUid clientUUid
     * @return 重置结果
     */
    public ResponseBase<Object> securityPasswdResetAuthorInLocal(String requestId,
                                                                 String clientUUid,
                                                                 SecurityPasswdResetAutherInLocalReq req) {

        var securityToken = securityTokenUtils.verifySecurityToken(requestId, req.getAcceptSecurityToken(),
                SecurityTokenType.TOKEN_TYPE_RESET_ADMIN_PWD, req.getClientUuid());
        if(securityToken == null){
            // 授权端申请修改密码的token，在重置安全密码时也可以使用
            securityToken = securityTokenUtils.verifySecurityToken(requestId, req.getAcceptSecurityToken(),
                SecurityTokenType.TOKEN_TYPE_MODIFY_ADMIN_PWD, req.getClientUuid());
        }
        if (securityToken == null) {
            throw new ServiceOperationException(ServiceError.EMAIL_VERIFICATION_TOKEN_TIMEOUT);
        }

        if (!securityPasswordUtils.doModifyPasscode(requestId, req.getNewPasswd())) {
            return ResponseBase.forbidden("doModifyPasswd error", requestId);
        }

        pushUtils.doPushSucc(requestId,
                NotificationEnum.SECURITY_PASSWD_RESET_SUCC);
        LOG.debugv("securityPasswdResetAuther succ, req={0}, clientUUID {1}", req, clientUUid);
        return ResponseBase.okACC(requestId, null);

    }

}
