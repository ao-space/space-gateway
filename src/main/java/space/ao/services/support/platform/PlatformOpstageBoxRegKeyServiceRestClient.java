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

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import space.ao.services.support.log.Logged;
import space.ao.services.support.platform.info.token.TokenCreateResults;
import space.ao.services.support.platform.info.token.TokenInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "psplatform-api")
@Tag(name = "Product Platform Service",
        description = "Provides product platform related APIs.")
public interface PlatformOpstageBoxRegKeyServiceRestClient {

  @POST
  @Path("/v2/platform/auth/box_reg_keys")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Logged
  TokenCreateResults createTokens(@Valid TokenInfo tokenInfo, @HeaderParam("Request-Id") @NotBlank String reqId);

}
