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

package space.ao.services.push.dto;

import java.time.OffsetDateTime;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import space.ao.services.push.entity.NotificationEntity;

@Data
public class PushNotificationResult {
  @NotNull
  String messageId;
  @NotNull
  String optType;
  @NotNull
  String requestId;
  @NotNull
  Boolean read;
  @NotNull
  OffsetDateTime createAt;
  @NotNull
  String title;
  @NotNull
  String text;
  @NotNull
  String data;

  public static PushNotificationResult fromNotificationEntity(NotificationEntity notification){
    var pushNotificationResult = new PushNotificationResult();

    pushNotificationResult.messageId = notification.getMessageId();
    pushNotificationResult.optType = notification.getOptType();
    pushNotificationResult.requestId = notification.getRequestId();
    pushNotificationResult.read = notification.getRead();
    pushNotificationResult.createAt = notification.getCreateAt();
    pushNotificationResult.data = notification.getData();

    return pushNotificationResult;
  }
}
