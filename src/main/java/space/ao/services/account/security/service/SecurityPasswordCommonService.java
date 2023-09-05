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

import java.time.Instant;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import space.ao.services.account.security.dto.ApplyInfoNewDeviceReq;
import space.ao.services.account.security.dto.SecurityPasswdResetBinderAcceptReq;
import space.ao.services.account.security.dto.SecurityPasswdResetNewDeviceReq;
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
import space.ao.services.support.OperationUtils;
import space.ao.services.support.task.ScheduledService;
import space.ao.services.support.task.TaskBaseEntity;

/**
 * 密码逻辑服务公共类。
 */
@ApplicationScoped
public class SecurityPasswordCommonService {

    @Inject
    SecurityTokenUtils securityTokenUtils;

    @Inject
    SecurityPasswordUtils securityPasswordUtils;

    @Inject
    PushUtils pushUtils;

    @Inject
    ApplicationProperties properties;

    @Inject
    OperationUtils utils;
    @Inject
    ScheduledService scheduledService;
    static final Logger LOG = Logger.getLogger("app.log");

    public ResponseBase<Object> securityPasswdResetAuthorApply(String requestId,
                                                               String applyId,
                                                               String userId,
                                                               String clientUUid,
                                                               ApplyInfoNewDeviceReq req,
                                                               boolean newDevice) {


        ZonedDateTime expiresAt = ZonedDateTime.now().plusSeconds(
                Duration.parse(properties.gatewayTimeOfSecurityPasswdAkLife()).toSeconds());
        if (newDevice) {
            expiresAt = ZonedDateTime.now().plusSeconds(
                    Duration.parse(properties.gatewayTimeOfSecurityPasswdModifyTakeEffectForNewApp()).toSeconds());
        }

        pushUtils.doPushApply(requestId, applyId,
                NotificationEnum.SECURITY_PASSWD_RESET_APPLY,
                userId, req.getClientUuid(), req.getDeviceInfo(),
                SecurityTokenType.TOKEN_TYPE_APPLY_RESET_ADMIN_PWD,
                expiresAt);
        LOG.debugv("securityPasswdResetAutherApply succ, req={0}, clientUUID:{1}", req, clientUUid);
        return ResponseBase.okACC(requestId, null);
    }

    public ResponseBase<Object> securityPasswdResetBinderAccept(String requestId,
                                                        String applyId,
                                                        String clientUUid,
                                                        SecurityPasswdResetBinderAcceptReq req) {

        var securityToken = securityTokenUtils.verifySecurityToken(requestId, req.getSecurityToken(),
                SecurityTokenType.TOKEN_TYPE_APPLY_RESET_ADMIN_PWD, req.getClientUuid());
        if (securityToken == null) {
            return ResponseBase.forbidden("verifySecurityToken error", requestId);
        }

        pushUtils.doPushAccept(requestId,
                applyId,
                NotificationEnum.SECURITY_PASSWD_PARTICULAR_RESET_ACCEPT,
                req.getClientUuid(), clientUUid,
                req.isAccept(),
                SecurityTokenType.TOKEN_TYPE_RESET_ADMIN_PWD);
        LOG.debug("securityPasswdResetBinderAccept succ, req="+req);
        return ResponseBase.okACC(requestId, null);
    }

    @Transactional
    public ResponseBase<Object> securityPasswdResetAuthorOrNewDevice(String requestId,
                                                                     String applyId,
                                                                     String userId,
                                                                     String clientUUid,
                                                                     SecurityPasswdResetNewDeviceReq req,
                                                                     boolean newDevice) {

        var securityToken = securityTokenUtils.verifySecurityToken(requestId, req.getAcceptSecurityToken(),
                SecurityTokenType.TOKEN_TYPE_RESET_ADMIN_PWD, req.getClientUuid());
        if(securityToken == null){
            securityToken = securityTokenUtils.verifySecurityToken(requestId, req.getAcceptSecurityToken(),
                SecurityTokenType.TOKEN_TYPE_MODIFY_ADMIN_PWD, req.getClientUuid());
        }
        // 授权设备, 没有允许的令牌时直接返回错误。
        if (securityToken == null && !newDevice) {
            throw new ServiceOperationException(ServiceError.EMAIL_VERIFICATION_TOKEN_TIMEOUT);
        }

        if(!securityTokenUtils.verifyEmailToken(requestId, req.getEmailSecurityToken(), clientUUid)) {
            throw new ServiceOperationException(ServiceError.EMAIL_VERIFICATION_TOKEN_TIMEOUT);
        }

        // 已经允许了, 有2种情况:
        // 1. 授权手机/授权web时, 绑定端已经允许了(上面做了是否允许校验)，修改数据库和推送。
        // 2. 新设备时， 绑定端已经已经允许了， 修改数据库和推送。
        if (!newDevice || securityToken != null) {
            if (!securityPasswordUtils.doModifyPasscode(requestId, req.getNewPasswd())) {
                return ResponseBase.forbidden("doModifyPasswd error", requestId);
            }

            pushUtils.doPushSucc(requestId,
                    NotificationEnum.SECURITY_PASSWD_RESET_SUCC);
        }

        // 新设备, 暂未允许的情况。加入定时任务队列, 24h 后执行任务再入库和推送。
        if (newDevice && securityToken == null) {
            var task = new TaskBaseEntity(requestId,
                Instant.now().plusSeconds(Duration.parse(properties.gatewayTimeOfSecurityPasswdModifyTakeEffectForNewApp()).toSeconds()),
                utils.objectToJson(req), "passwordModify");
            task.persist();
            LOG.info(task);
            scheduledService.onStart(requestId, task, SecurityPasswordModifyJob.class);
        }

        LOG.debugv("securityPasswdResetAuther succ, req={0}, applyId: {1}, userId: {2}", req, applyId, userId);
        return ResponseBase.okACC(requestId, null);
    }





}

