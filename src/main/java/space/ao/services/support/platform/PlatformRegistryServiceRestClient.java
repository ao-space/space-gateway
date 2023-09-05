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

package space.ao.services.support.platform;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import space.ao.services.support.platform.info.ability.PlatformApiResults;
import space.ao.services.support.platform.info.token.TokenCreateResults;
import space.ao.services.support.platform.info.token.TokenInfo;
import space.ao.services.support.log.Logged;
import space.ao.services.support.platform.info.registry.ClientRegistryInfo;
import space.ao.services.support.platform.info.registry.ClientRegistryResult;
import space.ao.services.support.platform.info.registry.UserRegistryInfo;
import space.ao.services.support.platform.info.registry.UserRegistryResult;

@Path("/v2/platform")
@RegisterRestClient(configKey = "ssplatform-api")
@Tag(name = "Registry Platform Service",
    description = "Provides registry platform related APIs.")
public interface PlatformRegistryServiceRestClient {

  @POST
  @Path("/auth/box_reg_keys")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Logged
  TokenCreateResults createTokens(@Valid TokenInfo tokenInfo, @HeaderParam("Request-Id") @NotBlank String reqId);

  @POST
  @Path("/boxes/{box_uuid}/users")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Logged
  UserRegistryResult platformRegistryUser(@Valid UserRegistryInfo userRegistryInfo,
                                          @HeaderParam("Request-Id") @NotBlank String reqId,
                                          @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                                          @PathParam("box_uuid") @NotBlank String boxUUID);

  @DELETE
  @Path("/boxes/{box_uuid}/users/{user_id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Logged
  void platformResetUser(@HeaderParam("Request-Id") @NotBlank String reqId,
      @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
      @PathParam("box_uuid") @NotBlank String boxUUID,
      @PathParam("user_id") @NotBlank String userId);

  @POST
  @Path("/boxes/{box_uuid}/users/{user_id}/clients")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Logged
  ClientRegistryResult platformRegistryClient(@Valid ClientRegistryInfo clientInfo,
                                              @HeaderParam("Request-Id") @NotBlank String reqId,
                                              @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
                                              @PathParam("box_uuid") @NotBlank String boxUUID,
                                              @PathParam("user_id") @NotBlank String userId);

  @DELETE
  @Path("/boxes/{box_uuid}/users/{user_id}/clients/{client_uuid}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Logged
  void platformRestClient(@HeaderParam("Request-Id") @NotBlank String reqId,
      @HeaderParam("Box-Reg-Key") @NotBlank String boxRegKey,
      @PathParam("box_uuid") @NotBlank String boxUUID,
      @PathParam("user_id") @NotBlank String userId,
      @PathParam("client_uuid") @NotBlank String clientUUID);

  @GET
  @Path("/ability")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "能力查询")
  @Logged
  PlatformApiResults ability(@NotBlank @HeaderParam("Request-Id") String requestId);

  @GET
  @Path("/status")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "查询空间平台状态")
  @Logged
  PlatformStatusResult status(@NotBlank @HeaderParam("Request-Id") String requestId);

}
