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

package space.ao.services.push.rest;

import java.util.Objects;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import space.ao.services.push.entity.NotificationEntity;
import space.ao.services.push.repository.NotificationRepository;
import space.ao.services.push.services.NotificationService;
import space.ao.services.push.services.RedisService;
import space.ao.services.support.RestConfiguration;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.push.dto.Message;
import space.ao.services.push.dto.MessageIdInfo;
import space.ao.services.push.dto.NotificationPageInfo;
import space.ao.services.push.dto.NotificationPageQueryInfo;

@Path("/v1/api/notification")
@Tag(name = "Space Gateway Notification Service",
    description = "Provides Notification Service.")
public class NotificationResource {
  @Inject
  RedisService redisService;
  @Inject
  NotificationRepository notificationRepository;

  @Inject
  NotificationService notificationService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description =
      "get notification.")
  public ResponseBase<NotificationEntity> get(@Valid @NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId,
                                              @Valid @NotBlank @QueryParam("userId") String userid,
                                              @Valid @NotBlank @QueryParam("messageId") String messageId) {
    return ResponseBase.ok(requestId, notificationRepository.getNotificationByMessageId(messageId)).build();
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description =
      "add notification.")
  public String add(@Valid @NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId,
      @Valid Message message) {

    return redisService.pushMessage(message);
  }


  @POST
  @Path("/all")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description =
      "get all notification. page 页码，默认1 pageSize页码大小，默认10")
  public ResponseBase<NotificationPageInfo> getAll(@Valid @NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId,
      @Valid @NotBlank @QueryParam("userId") String userid,
      @Valid @NotBlank @QueryParam("AccessToken-clientUUID") String clientUUID,
      NotificationPageQueryInfo notificationPageQueryInfo) {
    return ResponseBase.ok(requestId, notificationService.getNotification(clientUUID, Integer.valueOf(userid),
        notificationPageQueryInfo.page(), notificationPageQueryInfo.pageSize(), notificationPageQueryInfo.optTypes())).build();
  }

  @POST
  @Path("/all/delete")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description =
      "delete all notification.")
  @Transactional
  public ResponseBase<Long> deleteAll(@Valid @NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId,
      @Valid @NotBlank @QueryParam("userId") String userid,
      @Valid @NotBlank @QueryParam("AccessToken-clientUUID") String clientUUID,
      MessageIdInfo messageIdInfo) {
    var messageIds = messageIdInfo.getMessageId();
    if(Objects.nonNull(messageIds) && !messageIds.isEmpty()){
       return ResponseBase.ok(requestId, notificationRepository.deleteByUserIdAndClientUUIDByMessageId(userid, clientUUID, messageIds)).build();
      }
    return ResponseBase.ok(requestId, notificationRepository.deleteByUserIdAndClientUUID(userid, clientUUID)).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description =
      "delete notification.")
  @Transactional
  public ResponseBase<Long> delete(@Valid @NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId,
      @Valid @NotBlank @QueryParam("userId") String userid,
      @Valid @NotBlank @QueryParam("messageId") String messageId) {
    return ResponseBase.ok(requestId, notificationRepository.deleteByMessageId(messageId)).build();
  }

  @POST
  @Path("/set/read")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description =
      "set notification read status .")
  @Transactional
  public ResponseBase<Integer> setRead(@Valid @NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId,
      @Valid @NotBlank @QueryParam("userId") String userid,  @Valid @NotBlank @QueryParam("AccessToken-clientUUID") String clientUUID,
      MessageIdInfo messageIdInfo) {
    return ResponseBase.ok(requestId, notificationService.setRead(userid, clientUUID, messageIdInfo.getMessageId(), true)).build();
  }


}
