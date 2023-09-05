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

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import java.time.OffsetDateTime;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import space.ao.services.account.authorizedterminalinfo.service.AuthorizedTerminalService;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.push.services.NotificationService;
import space.ao.services.push.services.RedisService;
import space.ao.services.support.RestConfiguration;
import space.ao.services.support.StringUtils;
import space.ao.services.support.TokenUtils;
import space.ao.services.support.log.Logged;
import space.ao.services.support.model.StatusResult;
import space.ao.services.support.security.SecurityUtils;

@Tag(name = "Space Gateway Notification Service",
    description = "Provides Notification Service.")
@Path("/v1/api/gateway/poll")
public class PollNotificationResource {

    @Inject
    ApplicationProperties properties;

    @Inject
    NotificationService notificationService;

    @Inject
    SecurityUtils securityUtils;
    @Inject
    TokenUtils tokenUtils;
    @Context
    HttpServerResponse response;
    @Context
    HttpServerRequest request;
    @Inject
    AuthorizedTerminalService authorizedTerminalService;
    static final Logger LOG = Logger.getLogger("app.log");

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Logged
    @Operation(description = "Try to poll the current Notification of server.")
    public StatusResult poll(@Valid @HeaderParam(RestConfiguration.REQUEST_ID) String requestId,
                             @QueryParam("accessToken") String accessToken, @QueryParam("count")String count) {
        if(StringUtils.isBlank(accessToken)){
            return StatusResult.of("ok", properties.version(),null, StatusResult.PlatformInfo.of(
                    properties.ssplatformUrl()
            ));
        } else {
            var token = tokenUtils.checkAccessToken(requestId, accessToken);
            var clientUUID = token.getClientUUID();

            RedisService.setClientStatus(clientUUID, OffsetDateTime.now());
            authorizedTerminalService.updateAuthorizedTerminalValidTime(requestId, token.getUserId(), clientUUID);
            //根据 response 获取 在线状态
            response.closeHandler(statusResponse->{
                RedisService.setClientStatus(clientUUID, OffsetDateTime.MIN);
                LOG.infov("连接被关闭");
            });

            var notification = notificationService.poll(clientUUID + token.getUserId(), token.getUserId(), StringUtils.isBlank(count)? 1 : Integer.parseInt(count));
            return StatusResult.of("ok", properties.version(), notification != null ?
                    securityUtils.encryptWithSecret(notification, token.getSharedSecret(), token.getSharedInitializationVector()): "",StatusResult.PlatformInfo.of(
                    properties.ssplatformUrl()
            ));
        }
    }

}
