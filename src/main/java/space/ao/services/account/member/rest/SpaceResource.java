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

/*
 * Copyright (c) 2023 Institute of Software Chinese Academy of Sciences (ISCAS)
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

package space.ao.services.account.member.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import space.ao.services.account.member.dto.*;
import space.ao.services.account.member.service.AdminUserService;
import space.ao.services.account.member.service.PlatformRegistryService;
import space.ao.services.account.support.response.ResponseBaseEnum;
import space.ao.services.support.StringUtils;
import space.ao.services.support.log.Logged;
import space.ao.services.support.response.ResponseBase;

@Path("/v1/api")
@Tag(name = "Admin user service", description = "Provide admin user revoke/bind requests.")
public class SpaceResource {

  @Inject
  AdminUserService adminUserService;
  @Inject
  PlatformRegistryService platformRegistryService;
  @Path("/space/admin")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "create space own admin")
  @Logged
  public ResponseBase<AdminBindResult> createAdmin(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                   @Valid AdminBindInfo adminBindInfo) {
    if(!adminUserService.checkPasscodeOrNew(requestId, adminBindInfo.getPassword())){
     return ResponseBase.forbidden("password is not correct", requestId);
    }
    if(!StringUtils.isBlank(adminBindInfo.getSpaceName()) && adminUserService.isPersonalNameUsed(adminBindInfo.getSpaceName(), Const.Admin.ADMIN_ID)){
      return ResponseBaseEnum.SPACE_NAME_IS_USED.getResponseBase(requestId);
    }
    return ResponseBase.okACC(requestId, adminUserService.createAdmin(requestId, adminBindInfo));
  }

  @Path("/space/platform")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "change platform url")
  @Logged
  public ResponseBase<PlatformInfo> changePlatform(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                     @Valid PlatformInfo platformInfo) {
    return ResponseBase.ok(requestId, platformRegistryService.setPlatform(platformInfo.ssplatformUrl())).build();
  }
}
