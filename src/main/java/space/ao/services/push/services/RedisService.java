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


import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import lombok.Getter;
import org.jboss.logging.Logger;
import space.ao.services.account.authorizedterminalinfo.entity.TerminalType;
import space.ao.services.support.log.Logged;
import space.ao.services.support.platform.PlatformUtils;
import space.ao.services.support.redis.RedisCommonStringService;
import space.ao.services.support.redis.message.SendMessage;
import space.ao.services.account.authorizedterminalinfo.repository.AuthorizedTerminalRepository;
import space.ao.services.account.personalinfo.entity.UserEntity.Role;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.push.dto.Message;
import space.ao.services.push.dto.NotificationEnum;
import space.ao.services.push.entity.NotificationEntity;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import space.ao.services.support.redis.message.ReceiveMessage;
import space.ao.services.support.redis.message.RedisMessageService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
@Startup
public class RedisService {

  @Inject
  ApplicationProperties properties;

  @Inject
  RedisMessageService redisMessageService;
  @Inject
  AuthorizedTerminalRepository authorizedTerminalRepository;

  @Inject
  UserInfoRepository userInfoRepository;
  @Inject
  RedisCommonStringService redisCommonStringService;
  @Inject
  PlatformUtils platformUtils;
  static final Logger LOG = Logger.getLogger("push_message.log");
  @Getter
  private static final ConcurrentMap<String, OffsetDateTime> clientUUIDCacheMap = new ConcurrentHashMap<>(); // clientUUID , status 请求时间 缓存

  /**
   *
   * @param flag flag = clientUUID + userId
   * @param offsetDateTime time
   */
  public static void setClientStatus(String flag, OffsetDateTime offsetDateTime){
    clientUUIDCacheMap.put(flag, offsetDateTime);
  }


  @PostConstruct
  @SuppressWarnings("unused") // 启动程序时开启启动队列消费
  public void pushService() {
    new Thread(() -> blockingSubscribeMain(properties.pushMqMain())).start();
  }

  @Logged
  public String pushMessage(Message message){
    return redisMessageService.push(properties.pushMqMain(),
            new SendMessage(message.userId(), message.clientUUID(), message.optType(), message.requestId(), message.data()));
  }

  @Logged
  public void sendNotification(NotificationEntity notification) {
    var notificationEnum = NotificationEnum.of(notification.getOptType());
    Log.infov("notification type is " + notification.getOptType());
    if(Objects.isNull(notificationEnum)){
      LOG.error("Type is invalid");
      return;
    }
    switch (notificationEnum) {
      case UPGRADE_INSTALLING -> sendAllTerminalExcludeAdmin(notification);
      case ABILITY_CHANGE -> sendAllTerminal(notification);
      case TODAY_IN_HIS, MEMORIES ->
              sendAllTerminalByUserId(notification);
      default -> sendClientUUID(notification);
    }
  }

  @Logged
  public void sendClientUUID(NotificationEntity notification) {

    final String PUSH_TIMEOUT = properties.pushTimeout();
    final String PUSH_MQ_CLIENT_PREFIX = properties.pushMqClientPrefix();

    var key = notification.getClientUUID() + notification.getUserid();
    var now = OffsetDateTime.now();

    boolean isPlatformSupportMessagePush = platformUtils.isPlatformSupportPush();

    boolean shouldSendOnlyOnline = sendOnlyOnline(notification);

    // 判断是否在线
    var clientStatus = clientUUIDCacheMap.get(key);
    var isClientOnline = Objects.nonNull(clientStatus) && clientStatus.isAfter(now.minusSeconds(Duration.parse(PUSH_TIMEOUT).getSeconds() + 5));

    LOG.infov("client key {0} 对应的消息队列状态: {1}, isPlatformSupportMessagePush: {2}, shouldSendOnlyOnline: {3}, isClientOnline: {4}",
            key, clientStatus, isPlatformSupportMessagePush, shouldSendOnlyOnline, isClientOnline);

    redisMessageService.push(PUSH_MQ_CLIENT_PREFIX + key, new SendMessage(notification.getUserid()
              .toString(), notification.getClientUUID(), notification.getOptType(), notification.getRequestId(), notification.getData()));


  }

  public List<NotificationEntity> getNotificationEntityByReceiveMessage(List<ReceiveMessage> receiveMessages) {
    List<NotificationEntity> result = new ArrayList<>();
    if (Objects.isNull(receiveMessages)) {
      return result;
    }

    for (var receiveMessage: receiveMessages){
      var notificationEntity = new NotificationEntity(receiveMessage.messageId(),
              Integer.parseInt(receiveMessage.userId()), receiveMessage.clientUUID(),
              receiveMessage.optType(), receiveMessage.requestId(), receiveMessage.data());

      result.add(notificationEntity);
    }

    return result;
  }

  /**
   * 阻塞式异步消费, 没有消息是阻塞，收到消息发送到对应客户端队列 然后开启下一次阻塞式读
   * @param key 对应客户端队列 client prefix + clientUUID
   */
  @Logged
  public void blockingSubscribeMain(String key) {
    while (Objects.equals(key, properties.pushMqMain())){
      var notifications = getNotificationEntity(key, null, 60000L);
      LOG.infov("get notifications from redis, notifications is: " + notifications);
      for (var notification :notifications) {
        sendNotification(notification);
        redisMessageService.del(key, notification.getMessageId());
      }
    }
  }

  public List<NotificationEntity> getNotificationEntity(String key, Integer count, Long milliseconds) {
      var response = redisMessageService.getMessage(key, count, milliseconds);
      return getNotificationEntityByReceiveMessage(response);
  }

  public Integer increaseFailedLoginCounter(String errorCounterKey, String userId, String clientUUID){
    var key = errorCounterKey + userId + clientUUID;
    int count;
    var x = redisCommonStringService.get(key);
    if(Objects.isNull(x)){
      redisCommonStringService.set(key, "1");
      count = 1;
    } else {
      count = (int) redisCommonStringService.incr(key);
    }
    return count;
  }

  public void resetFailedLoginCounter(String errorCounterKey, String userId, String clientUUID){
    var key = errorCounterKey + userId + clientUUID;
    redisCommonStringService.del(key);
  }
  
  public boolean sendOnlyOnline(NotificationEntity notificationEntity){
    var terminal = authorizedTerminalRepository.findByUseridAndUuid(notificationEntity.getUserid().longValue(), notificationEntity.getClientUUID());
    var isWeb = terminal != null && TerminalType.web.name().equalsIgnoreCase(terminal.getTerminalType());
    return isWeb || NotificationEnum.sendOnlyOnline().contains(notificationEntity.getOptType());
  }

  @Logged
  public void sendAllTerminalExcludeAdmin(NotificationEntity notificationEntity) {
    var authors =  authorizedTerminalRepository.findAllTerminal();
    var adminBinder = userInfoRepository.findByRole(Role.ADMINISTRATOR);
    for (var client: authors){
      if(!client.getUuid().equals(adminBinder.getClientUUID())){
        notificationEntity.setUserid(client.getUserid().intValue());
        notificationEntity.setClientUUID(client.getUuid());
        sendClientUUID(notificationEntity);
      }
    }
  }

  @Logged
  public void sendAllTerminal(NotificationEntity notificationEntity) {
    if(NotificationEnum.ABILITY_CHANGE.getType().equals(notificationEntity.getOptType())){
      platformUtils.queryPlatformAbility();
    }
    var authors =  authorizedTerminalRepository.findAllTerminal();
    for (var client: authors){
      notificationEntity.setUserid(client.getUserid().intValue());
      notificationEntity.setClientUUID(client.getUuid());
      sendClientUUID(notificationEntity);
    }
  }

  @Logged
  public void sendAllTerminalByUserId(NotificationEntity notificationEntity) {
    var authors =  authorizedTerminalRepository.findByUserid(Long.valueOf(notificationEntity.getUserid()));
    for (var client: authors){
      notificationEntity.setUserid(client.getUserid().intValue());
      notificationEntity.setClientUUID(client.getUuid());
      sendClientUUID(notificationEntity);
    }
  }

  @Logged
  public void del(String userid, String clientUUID){
    var key = properties.pushMqClientPrefix() + clientUUID + userid;
    redisCommonStringService.del(key);
  }
}
