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

package space.ao.services.auth.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import space.ao.services.auth.service.TotpService;
import space.ao.services.gateway.auth.qrcode.dto.TotpAuthCode;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.response.ResponseBase;

import java.time.Duration;
import java.util.Objects;

import static space.ao.services.support.RestConfiguration.REQUEST_ID;

@Path("/v1/api/auth/totp")
@Tag(name = "Space QRCode-scanning Service base on TOTP",
        description = "Provides overall space requests' scan QR code service base on TOTP.")
public class AuthTotpResource {

  @Inject
  TotpService totpService;
  @Inject
  ApplicationProperties properties;


  /**
   * 授权端创建 bkey时，成员端验证 bkey
   */
  @POST
  @Path("/bkey/verify")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description =
          "成员端调用绑定 bkey 归属的 userid。 授权端创建 bkey时，成员端验证 bkey。 通过 call 接口调用")
  public ResponseBase<Boolean> bkeyVerity(@Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId,
                                          @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam("userId") String userId,
                                          @Valid @NotBlank @QueryParam("bkey") String bkey) {
    var createAuthCodeDTO = totpService.getUserIdByBkey(bkey);
    createAuthCodeDTO.setUserId(userId);
    totpService.saveUserIdByBkey(createAuthCodeDTO);
    return ResponseBase.ok(requestId,true).build();
  }


  @POST
  @Path("/bkey/poll")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description =
          "成员端调用设置自动登录并获取扫码结果. 通过 call 接口调用")
  public ResponseBase<Boolean> bkeyPoll(@Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId,
                                         @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam("userId") String userId,
                                         @Valid @NotBlank @QueryParam("bkey") String bkey,
                                         @Valid @QueryParam("autoLogin") @DefaultValue("true") Boolean autoLogin) {
    var createAuthCodeDTO = totpService.getUserIdByBkey(bkey);
    if(Boolean.FALSE.equals(autoLogin)){
      createAuthCodeDTO.setAutoLogin(false);
      createAuthCodeDTO.setAutoLoginExpiresAt(Duration.parse(properties.pushTimeout()).toSeconds());
      totpService.saveUserIdByBkey(createAuthCodeDTO);
    }
    return ResponseBase.ok(requestId, createAuthCodeDTO.isAuthResult()).build();
  }

  @GET
  @Path("/auth-code")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "成员调用获取 authCode. ")
  public ResponseBase<TotpAuthCode> authCode(@Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId,
                                             @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam("userId") String userId) {
    var authCode = totpService.generateAuthCode(Long.parseLong(userId));
    if(authCode == null) {
      return ResponseBase.<TotpAuthCode>notFound(requestId).build();
    }
    return ResponseBase.ok(requestId, authCode).build();
  }

  @GET
  @Path("/auth-code/verify")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "成员 调用 验证 authCode. ")
  public ResponseBase<Boolean> authCodeVerify(@Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId,
                                              @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam("userId") String userId,
                                              @NotBlank @QueryParam("auth-code") String authCode) {
    if(Objects.equals(totpService.generateAuthCode(Long.parseLong(userId)).getAuthCode(), authCode)) {
      totpService.setAuthenticatorStatus(Long.parseLong(userId), true);
      return ResponseBase.ok(requestId, true).build();
    }
    return ResponseBase.ok(requestId, false).build();
  }
}
