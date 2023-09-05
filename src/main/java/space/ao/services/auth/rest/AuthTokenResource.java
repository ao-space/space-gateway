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

package space.ao.services.auth.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import space.ao.services.account.authorizedterminalinfo.repository.AuthorizedTerminalRepository;
import space.ao.services.support.TokenUtils;
import space.ao.services.support.log.Logged;
import space.ao.services.support.model.AccessToken;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.support.service.ServiceError;
import space.ao.services.support.service.ServiceOperationException;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.time.OffsetDateTime;
import java.util.Objects;

import static space.ao.services.support.RestConfiguration.REQUEST_ID;

@Path("/v1/api/auth/token")
@Tag(name = "Authing Token Service",
        description = "提供访问令牌相关的服务。你可以使用访问令牌进行进一步的调用请求。")
public class AuthTokenResource {

  @Inject
  TokenUtils tokenUtils;
  @Inject
  AuthorizedTerminalRepository authorizedTerminalRepository;

  @GET
  @Logged
  @Path("verify")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(description = "Tries to verify an access token for further api call.")
  public ResponseBase<AccessToken> verify(@Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId,
                                          @Valid @NotBlank @QueryParam("access-token") String accessToken) {
    final var ak = tokenUtils.verifyAccessToken(requestId, accessToken);
    if(ak == null || !verifyClient(ak.getUserId(), ak.getClientUUID())) {
      return ResponseBase.<AccessToken>forbidden(requestId).build();
    }
    return ResponseBase.ok(requestId, ak).build();
  }

  @Logged
  public boolean verifyClient(String userId, String clientUUID){
    var authorizedTerminalEntityList =
            authorizedTerminalRepository.findByUserid(Long.valueOf(userId));
    if(authorizedTerminalEntityList == null || authorizedTerminalEntityList.isEmpty()) {
      return false;
    }
    for (var entity: authorizedTerminalEntityList) {
      if(entity.getExpireAt().isBefore(OffsetDateTime.now())){
        throw new ServiceOperationException(ServiceError.ACCESS_TOKEN_INVALID);
      }
      if(Objects.equals(entity.getUuid(), clientUUID)){
        return true;
      }
    }
    return false;
  }
}
