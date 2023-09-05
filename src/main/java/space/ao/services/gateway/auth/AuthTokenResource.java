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

package space.ao.services.gateway.auth;

import io.quarkus.logging.Log;
import io.vertx.core.http.HttpServerRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Objects;
import jakarta.ws.rs.core.Context;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import space.ao.services.account.member.dto.Const;
import space.ao.services.gateway.auth.member.client.ResponseCodeConstant;
import space.ao.services.account.authorizedterminalinfo.dto.AuthorizedTerminalInfo;
import space.ao.services.account.authorizedterminalinfo.repository.AuthorizedTerminalRepository;
import space.ao.services.account.authorizedterminalinfo.service.AuthorizedTerminalService;
import space.ao.services.account.member.service.MemberManageService;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.support.response.ResponseBaseEnum;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.StringUtils;
import space.ao.services.support.TokenUtils;
import space.ao.services.support.log.Logged;
import space.ao.services.support.platform.check.CheckPlatformStatus;
import space.ao.services.support.platform.check.PlatformTypeEnum;
import space.ao.services.support.response.ResponseBase;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import space.ao.services.support.security.SecurityUtils;
import space.ao.services.support.service.ServiceError;
import space.ao.services.support.service.ServiceOperationException;

import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static space.ao.services.support.RestConfiguration.REQUEST_ID;

@Path("/v1/api/gateway")
@Tag(name = "Space Gateway Admin-authing Service",
    description = "提供管理员访问令牌相关的服务。你可以使用访问令牌进行进一步的调用请求。")
public class AuthTokenResource {

  @Inject
  OperationUtils utils;
  @Inject
  SecurityUtils securityUtils;
  @Inject
  TokenUtils tokenUtils;
  @Inject
  MemberManageService memberManageService;

  @Context
  HttpServerRequest request;
  @Inject
  AuthorizedTerminalRepository authorizedTerminalRepository;
  @Inject
  AuthorizedTerminalService authorizedTerminalService;
  @Inject
  UserInfoRepository userInfoRepository;

  @POST
  @Logged
  @Path("/auth/token/create")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description =
          "Tries to get an admin access token for further api call. NOTE: you need to use encrypted(box public key)" +
                  " auth-key and client uuid to exchange an access token.")
  public CreateTokenResult create(@Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId,
                                  @Valid CreateTokenInfo info) {
    try {
      securityUtils.init();
    } catch (IOException | GeneralSecurityException e) {
      throw new ServiceOperationException(ServiceError.INIT_UTIL_FAILED);
    }
    String authKey = securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, info.getEncryptedAuthKey());
    String clientUUID = securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, info.getEncryptedClientUUID());

    var userEntity = memberManageService.findByClientUUID(clientUUID);

    if (Objects.equals(userEntity, null) || !Objects.equals(clientUUID, userEntity.getClientUUID())) {
      Log.errorv("clientUUID not match, clientUUID: {0}, userEntity: {1}", clientUUID, userEntity);
      throw new ServiceOperationException(ServiceError.CLIENT_UUID_NOT_MATCH);
    }
    if (!Objects.equals(authKey, userEntity.getAuthKey())) {
      if(Objects.isNull(userEntity.getAuthKey())){
        Log.errorv("authKey is null, clientUUID: {0}, userEntity: {1}", clientUUID, userEntity);
        throw new ServiceOperationException(ServiceError.CLIENT_UUID_NOT_MATCH);
      }
      Log.errorv("authKey not match, clientUUID: {0}, userEntity: {1}", clientUUID, userEntity);
      throw new ServiceOperationException(ServiceError.AUTH_KEY_NOT_MATCH);
    }
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    var address = "";
    if(!StringUtils.isBlank(xForwardedFor)){
      String ip = xForwardedFor.split(",")[0];
      address = utils.getCityInfo(ip);
    }
    String userAgent = request.getHeader("Http-User-Agent");
    if(!StringUtils.isBlank(userAgent) && userAgent.contains("okhttp")){
      userEntity.setPhoneType("android");
      userInfoRepository.updatePhoneTypeByUserId("android", userEntity.getId());
    }
    if (!StringUtils.isBlank(userAgent) && (userAgent.contains("iOS")||userAgent.contains("CFNetwork"))) {
      userEntity.setPhoneType("ios");
      userInfoRepository.updatePhoneTypeByUserId("ios", userEntity.getId());
    }
    var authorizedTerminal = authorizedTerminalRepository.findByUseridAndUuid(userEntity.getId(), clientUUID);
    var authorizedTerminalInfo = AuthorizedTerminalInfo.of(userEntity.getId().toString(),
            userEntity.getClientUUID(), userEntity.getPhoneModel(), utils.get100YearSeconds(), address,
            userEntity.getPhoneType());
    if(Objects.nonNull(authorizedTerminal)){
      authorizedTerminalService.updateAuthorizedTerminalValidTime(requestId, authorizedTerminalInfo);
    } else {
      authorizedTerminalRepository.insert(authorizedTerminalInfo);
    }
    return tokenUtils.createDefaultTokenResult(requestId, null, Const.Admin.ADMIN_ID, clientUUID, null);
  }

  @POST
  @Logged
  @Path("/auth/token/refresh")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description =
      "Tries to refresh an admin access token for further api call with a refresh-token.")
  public CreateTokenResult refresh(@Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId,
                                   @Valid RefreshTokenInfo info) {
    var refreshToken = tokenUtils.verifyRefreshToken(requestId, info.getRefreshToken());
    if (refreshToken != null) {
      return tokenUtils.createDefaultTokenResult(requestId, null,
          refreshToken.getUserId(), refreshToken.getClientUUID(), null);
    } else {
      throw new WebApplicationException("refresh token is invalid", FORBIDDEN);
    }
  }

  @GET
  @Logged
  @Path("/auth/token/verify")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(description = "Tries to verify an admin access token for further api call.")
  public VerifyTokenResult verify(@Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId,
                                  @Valid @NotBlank @QueryParam("access-token") String accessToken) {
    final boolean ok = tokenUtils.verifyAccessToken(requestId, accessToken) != null;
    return VerifyTokenResult.of(ok, requestId);
  }

  @POST
  @Logged
  @Path("/auth/revoke")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "解绑管理员客户端。")
  // @Tags( {@Tag(name = "0.4.0")}) // 标注该接口创建和修改版本
  // @Schema(description = "响应 GW-200：解绑成功，GW-406：请求参数错误，GW-500：服务内部错误。")
  public ResponseBase<RevokeClientResult> revoke(
      @Valid @NotBlank @Schema(description = "请求 id") @HeaderParam(REQUEST_ID) String requestId,
      @Valid @NotNull @Schema(description = "解绑管理员客户端认证信息") RevokeClientInfo info) {
    if(Boolean.TRUE.equals(utils.getEnableInternetAccess()) && Objects.equals(checkPlatform(requestId).code(), "GW-5005")){
      return ResponseBase.<RevokeClientResult>fromResponseBaseEnum(requestId, ResponseBaseEnum.SPACE_SERVICE_PLATFORM_ERROR).build();
    }
    String clientUUID = securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, info.getEncryptedClientUUID());
    var userEntity = memberManageService.findByClientUUID(clientUUID);
    if (Objects.equals(userEntity, null) || !Objects.equals(clientUUID, userEntity.getClientUUID())) {
      throw new ServiceOperationException(ServiceError.CLIENT_UUID_NOT_MATCH);
    }
    if (!userEntity.getAuthKey().equals(securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, info.getEncryptedAuthKey()))) {
      throw new WebApplicationException("admin auth-key was not matched!", FORBIDDEN);
    }
    if (!userEntity.getClientUUID().equals(securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, info.getEncryptedClientUUID()))) {
      throw new WebApplicationException("admin client uuid was not matched!", FORBIDDEN);
    }
    String passCode = securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, info.getEncryptedPasscode());

    // 撤销管理员在账号系统中的客户端认证信息，使其所有后续的 token 验证都失败，除非重新绑定新的客户端信息。
    var result = memberManageService.revokeUserClientInfo(
        String.valueOf(userEntity.getId()), requestId, passCode, clientUUID);

    if (ResponseCodeConstant.ACC_200.equals(result.code())) {
      return ResponseBase.ok(requestId, RevokeClientResult.of(true)).message(result.message()).build();
    } else {
      return ResponseBase.<RevokeClientResult>notAcceptable(requestId)
          .results(RevokeClientResult.of(false))
          .message(result.message()).build();
    }
  }

  @GET
  @Logged
  @Path("/auth/public-key")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "get box public key")
  public ResponseBase<String> getPublicKey(@Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId){
    var publicKey = securityUtils.getSecurityProvider().getBoxPublicKey(requestId);
    return ResponseBase.ok(requestId, Base64.getEncoder().encodeToString(publicKey.getEncoded())).build();
  }

  @CheckPlatformStatus(type = PlatformTypeEnum.SPACE)
  public ResponseBase<Object> checkPlatform(String requestId){
    // 空方法作用是借助 @CheckPlatformStatus 检查平台可用性
    return ResponseBase.ok(requestId,null).build();
  }
}
