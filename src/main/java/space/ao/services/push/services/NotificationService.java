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

package space.ao.services.push.services;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.jose4j.json.JsonUtil;
import org.jose4j.lang.JoseException;
import space.ao.services.account.member.dto.MemberCreateInfo;
import space.ao.services.account.security.dto.ApplyPushNotificationRsp;
import space.ao.services.account.security.dto.SuccPushNotitificationRsp;
import space.ao.services.push.dto.*;
import space.ao.services.push.repository.NotificationRepository;
import space.ao.services.support.log.Logged;
import space.ao.services.support.model.PageInfo;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.push.entity.NotificationEntity;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.StringUtils;
import space.ao.services.support.redis.message.ReceiveMessage;
import space.ao.services.support.redis.message.RedisMessageService;

@ApplicationScoped
public class NotificationService {
  @Inject
  ApplicationProperties properties;

  @Inject
  OperationUtils utils;
  @Inject
  RedisMessageService redisMessageService;
  @Inject
  UserInfoRepository userInfoRepository;
  @Inject
  NotificationRepository notificationRepository;
  static final Logger LOG = Logger.getLogger("push_message.log");
  public static final String PUSH_NOTIFICATION_PLATFORM_KEY = "push_notification_platform";

  @Logged
  public String poll(String key, String userId, int count) {
    long millis = Duration.parse(properties.pushTimeout()).toMillis();
    var notification = redisMessageService.getMessage(properties.pushMqClientPrefix() + key, count, millis);
    if(Objects.isNull(notification) || notification.isEmpty()){
      return null;
    }
    return notificationConsumer(notification,properties.pushMqClientPrefix() + key, userId).toString();
  }

  @Logged
  public List<String> notificationConsumer(List<ReceiveMessage> receiveMessages, String key, String userId){
    var result = new ArrayList<String>();

    for (var receiveMessage: receiveMessages){
      var notificationEntity = fromReceiveMessage(receiveMessage);
      var pushNotificationResult = PushNotificationResult.fromNotificationEntity(notificationEntity);

      var notificationEnum = generateNotificationEnum(notificationEntity, false);

      pushNotificationResult.setTitle(notificationEnum.getTitle());
      pushNotificationResult.setText(notificationEnum.getText());
      if(receiveMessage.userId().equals(userId)){
        notificationEntity.setPushed(1);
        if (redisMessageService.del(key, receiveMessage.messageId())){
          notificationRepository.add(notificationEntity);
        }
        result.add(utils.objectToJson(pushNotificationResult));
      } else {
        redisMessageService.del(key, receiveMessage.messageId());
      }
    }
    return result;
  }

  public NotificationPageInfo getNotification(String clientUUID, Integer userId,
                                              int page, int pageSize, List<String> optTypes) {
    var sql = "clientUUID=?1 and userid =?2";
    var total = notificationRepository.find(sql, clientUUID, userId).count();
    total = total / pageSize + (total % pageSize == 0 ? 0 : 1);
    return NotificationPageInfo.of(notificationRepository.getAllNotificationByClientUUIDAndUserid(clientUUID, userId,
            page, pageSize, optTypes), PageInfo.of(total, page, pageSize));
  }

  public int setRead(String userId, String clientUUID, List<String> messageId, boolean readStatus) {
    return notificationRepository.setReadStatus(userId, clientUUID, messageId, readStatus);
  }


  @Logged
  public NotificationEnum generateNotificationEnum(NotificationEntity notification, boolean isPlatform){
    NotificationEnum notificationEnum = NotificationEnum.of(notification.getOptType());

    if(Objects.isNull(notificationEnum) || StringUtils.isBlank(notification.getClientUUID())){
      LOG.errorv("Invalid enumeration type: {0}, Invalid ClientUUID: {0}", notification.getOptType(), notification.getClientUUID());
      redisMessageService.del(PUSH_NOTIFICATION_PLATFORM_KEY, notification.getMessageId());
      redisMessageService.del(properties.pushMqClientPrefix() + notification.getClientUUID(), notification.getMessageId());
      return null;
    }

    switch (notificationEnum){
      case LOGIN:
        var terminalInfo = utils.jsonToObject(notification.getData(), TerminalInfo.class);
        notificationEnum = notificationEnum.setInnerLogin(terminalInfo.terminalMode());
        if(isPlatform){
          notificationEnum = notificationEnum.setLogin(terminalInfo.terminalMode());
        }
        break;
      case MEMBER_JOIN:
        var name = utils.jsonToObject(notification.getData(), MemberCreateInfo.class).nickName();
        notificationEnum = notificationEnum.setMemberJoin(name);
        break;
      case LOGIN_CONFIRM:
        name = utils.jsonToObject(notification.getData(), TerminalInfo.class).terminalMode();
        var userEntity = userInfoRepository.findByUserId(notification.getUserid().longValue());
        notificationEnum = notificationEnum.setLoginConfirm(name, userEntity.getUserDomain());
        break;
      case SECURITY_PASSWD_MOD_APPLY:
        name = utils.jsonToObject(notification.getData(), ApplyPushNotificationRsp.class).getAuthDeviceInfo();
        notificationEnum = notificationEnum.getSecurityPasswdModApply(name);
        break;
      case SECURITY_PASSWD_RESET_APPLY:
        name = utils.jsonToObject(notification.getData(), ApplyPushNotificationRsp.class).getAuthDeviceInfo();
        notificationEnum = notificationEnum.getSecurityPasswdResetApply(name);
        break;
      case SECURITY_EMAIL_SET_SUCC:
        var mail = utils.jsonToObject(notification.getData(), SuccPushNotitificationRsp.class).getEmail();
        notificationEnum = notificationEnum.getSecurityEmailSetSucc(mail);
        break;
      case SECURITY_EMAIL_MOD_SUCC:
        mail = utils.jsonToObject(notification.getData(), SuccPushNotitificationRsp.class).getEmail();
        notificationEnum = notificationEnum.getSecurityEmailModSucc(mail);
        break;
      case UPGRADE_DOWNLOAD_SUCCESS:
        try {
          var version = JsonUtil.parseJson(notification.getData());
          notificationEnum = notificationEnum.getUpgradeDownloadSuccess((String) version.get("version"));
        } catch (JoseException e) {
          LOG.errorv("Invalid data: {0}", notification.getData());
        }
        break;
      default:
    }
    return notificationEnum;
  }

  public NotificationEntity fromReceiveMessage(ReceiveMessage receiveMessage) {
    return new NotificationEntity(
            receiveMessage.messageId(),
            Integer.parseInt(receiveMessage.userId()),
            receiveMessage.clientUUID(),
            receiveMessage.optType(),
            receiveMessage.requestId(),
            receiveMessage.data());
  }
}
