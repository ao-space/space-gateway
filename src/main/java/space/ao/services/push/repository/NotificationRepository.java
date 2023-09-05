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

package space.ao.services.push.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import java.util.List;
import java.util.Objects;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import space.ao.services.push.dto.NotificationEnum;
import space.ao.services.push.entity.NotificationEntity;

@Singleton
public class NotificationRepository implements PanacheRepository<NotificationEntity> {

  @Transactional
  public void add(NotificationEntity notificationEntity){
    NotificationEntity notification = find("messageId", notificationEntity.getMessageId()).firstResult();
    var isNeedSave = !NotificationEnum.noNeedToSave().contains(notificationEntity.getOptType());

    if(notification == null && isNeedSave){
      notificationEntity.persist();
    }
  }
  public List<NotificationEntity> getAllNotificationByClientUUIDAndUserid(String clientUUID, Integer userId,
      int page, int pageSize, List<String> optTypes){
    page--;
    PanacheQuery<NotificationEntity> notifications;
    var sql = "clientUUID=?1 and userid =?2 order by createAt desc";
    notifications = find(sql, clientUUID, userId);
    if (Objects.nonNull(optTypes) && !optTypes.isEmpty()){
      sql = "clientUUID=?1 and userid =?2  and optType in (?3) order by createAt desc";
      notifications = find(sql, clientUUID, userId, optTypes);
    }
    return notifications.page(Page.of(page, pageSize)).list();
  }

  public NotificationEntity getNotificationByMessageId(String messageId){
    return find("messageId", messageId).firstResult();
  }

  public long deleteByMessageId(String messageId){
    return delete("messageId=?1 ", messageId);
  }
  public long deleteByUserIdAndClientUUID(String userId, String clientUUID){
    return delete("userid=?1 and clientUUID=?2 ", Integer.valueOf(userId), clientUUID);
  }
  public long deleteByUserIdAndClientUUIDByMessageId(String userId, String clientUUID, List<String> messageIds){
    return delete("userid=?1 and clientUUID=?2 and message_id in (?3) ", Integer.valueOf(userId), clientUUID, messageIds);
  }
  public int setReadStatus(String userId, String clientUUID, List<String> messageIds, boolean readStatus){
    return update("set read=?1 where messageId in (?2) and userid=?3 and clientUUID=?4", readStatus, messageIds,
        Integer.parseInt(userId), clientUUID);
  }
}
