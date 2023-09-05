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

package space.ao.services.account.security.utils;

import jakarta.transaction.Transactional;
import space.ao.services.account.security.service.SecurityMessageService;
import space.ao.services.account.security.utils.token.SecurityTokenType;
import space.ao.services.account.security.utils.token.SecurityTokenUtils;
import space.ao.services.account.member.respository.BoxInfoRepository;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.account.security.dto.ApplyPushNotificationRsp;
import space.ao.services.account.security.dto.SecurityMessageRsp;
import space.ao.services.account.security.dto.SecurityMessageStore;
import space.ao.services.account.security.dto.SuccPushNotitificationRsp;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.push.dto.Message;
import space.ao.services.push.dto.NotificationEnum;
import space.ao.services.push.services.RedisService;
import space.ao.services.support.OperationUtils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * 推送相关的重复代码抽取封装
 */
@Singleton
public class PushUtils {

    @Inject
    RedisService redisService;

    @Inject
    SecurityTokenUtils securityTokenUtils;

    @Inject
    ApplicationProperties properties;

    @Inject
    SecurityMessageService securityMessageService;

    @Inject
    UserInfoRepository userInfoRepository;

    @Inject
    BoxInfoRepository boxInfoRepository;
    @Inject
    OperationUtils utils;

    /**
     * 申请修改的推送 (传统推送到绑定端)
     * @param requestId requestId
     * @param authUserId 发出修改申请的 userid
     * @param authClientUUid  发出修改申请的 ClientUUid
     * @param authDeviceInfo 授权设备信息
     * @param tokenType token类型
     * @return 推送结果
     */
    public boolean doPushApply(String requestId, String applyId,
                               NotificationEnum notificationEnum,
                               String authUserId, String authClientUUid,
                               String authDeviceInfo,
                               SecurityTokenType tokenType,
                               ZonedDateTime expiresAt) {
        var binderUserEntity = findBindUser();
        if (binderUserEntity==null || binderUserEntity.getRole()!= UserEntity.Role.ADMINISTRATOR ) {
            return false;
        }
        String binderUserId =String.valueOf(binderUserEntity.getId());
        String binderClientUUid =String.valueOf(binderUserEntity.getClientUUID());

        var result = securityTokenUtils.createWithExpiresAt(tokenType,
                authClientUUid,
                expiresAt);
        var optType = notificationEnum.getType();
        var data =utils.objectToJson(ApplyPushNotificationRsp.of(result,
                authUserId,
                authClientUUid,
                authDeviceInfo,
                requestId,
                applyId==null?"":applyId)); // 需要把 result、authUserId、authClientUUid、authDeviceInfo 推送给绑定端
        var msg= Message.of(binderUserId, binderClientUUid, optType, requestId, data);
        redisService.pushMessage(msg);
        return true;
    }

    /**
     * 允许修改后推送(到授权端/新设备。注意! 这个推送使用特定的私有轮询 poll)
     * @param authClientUUid 发出申请修改的 ClientUUid
     * @param binderClientUUid 绑定端 clientUUID
     * @param accept
     * @param tokenType token类型
     * @return 推送结果
     */
    public boolean doPushAccept(String requestId, String applyId,
                                NotificationEnum notificationEnum,
                                String authClientUUid, String binderClientUUid,
                                      boolean accept,
                                      SecurityTokenType tokenType) {

        final ZonedDateTime expiresAt = ZonedDateTime.now().plusSeconds(
                Duration.parse(properties.gatewayTimeOfSecurityPasswdAkLife()).toSeconds());
        var result = securityTokenUtils.createWithExpiresAt(tokenType, binderClientUUid, expiresAt);

        var securityMessageRsp = SecurityMessageRsp.of(notificationEnum.getType(),
                result,
                binderClientUUid,
                accept,
                requestId,
                applyId==null?"":applyId);
        if (!accept) {
            securityMessageRsp = SecurityMessageRsp.of(notificationEnum.getType(),
                    null, "", false,
                    requestId,
                    applyId==null?"":applyId);
        }
//        var securityMessageStore = SecurityMessageStore.of(securityMessageRsp, expiresAt);
        var securityMessageStore = new SecurityMessageStore(securityMessageRsp, expiresAt);
        securityMessageService.storeMessage(authClientUUid, securityMessageStore);

        return true;
    }

    /**
     * 修改/重置成功后推送 (传统推送到绑定端)
     * @param requestId requestId
     * @return 推送结果
     */
    @Transactional
    public boolean doPushSucc(String requestId,
                              NotificationEnum notificationEnum) {
        var binderUserEntity = findBindUser();
        if (binderUserEntity==null || binderUserEntity.getRole()!= UserEntity.Role.ADMINISTRATOR ) {
            return false;
        }
        var binderUserId = String.valueOf(binderUserEntity.getId());
        var binderClientUUid = String.valueOf(binderUserEntity.getClientUUID());

        var optType = notificationEnum.getType();

        var data =utils.objectToJson(SuccPushNotitificationRsp.of(requestId, boxInfoRepository.getEmail()));
        var msg= Message.of(binderUserId, binderClientUUid, optType, requestId, data);
        redisService.pushMessage(msg);
        return true;
    }

    private UserEntity findBindUser() {
        return userInfoRepository.findByUserId(1L);
    }
}
