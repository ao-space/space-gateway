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

package space.ao.services.gateway.auth.qrcode.rest;

import io.vertx.core.http.HttpServerRequest;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import jakarta.ws.rs.core.Context;
import io.vertx.core.http.HttpServerResponse;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import space.ao.services.auth.service.TotpService;
import space.ao.services.gateway.auth.qrcode.dto.*;
import space.ao.services.gateway.auth.qrcode.service.BoxKeyService;
import space.ao.services.gateway.auth.qrcode.service.CacheService;
import space.ao.services.account.authorizedterminalinfo.dto.AuthorizedTerminalInfo;
import space.ao.services.account.authorizedterminalinfo.entity.AuthorizedTerminalEntity;
import space.ao.services.account.authorizedterminalinfo.service.AuthorizedTerminalService;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.gateway.auth.CreateTokenResult;
import space.ao.services.gateway.auth.RefreshTokenInfo;
import space.ao.services.gateway.auth.VerifyTokenResult;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.StringUtils;
import space.ao.services.support.TokenUtils;
import space.ao.services.support.limit.LimitReq;
import space.ao.services.support.log.Logged;
import space.ao.services.support.model.RefreshToken;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.support.response.ResponseBaseEnum;
import space.ao.services.support.security.SecurityUtils;
import space.ao.services.support.service.ServiceError;
import space.ao.services.support.service.ServiceOperationException;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static space.ao.services.support.RestConfiguration.REQUEST_ID;

@Path("/v1/api/auth")
@Tag(name = "Space Gateway QRCode-scanning Service",
        description = "Provides overall space requests' scan QR code service.")
public class BoxKeyResource {

  @Inject
  BoxKeyService boxKeyService;
  @Inject
  OperationUtils utils;
  @Inject
  TokenUtils tokenUtils;
  @Inject
  SecurityUtils securityUtils;
  @Context
  HttpServerRequest request;
  @Context
  HttpServerResponse response;
  @Inject
  AuthorizedTerminalService authorizedTerminalService;
  @Inject
  CacheService cacheService;
  @Inject
  ApplicationProperties properties;
  @Inject
  TotpService totpService;

  private static final String X_FORWARDED_FOR = "X-Forwarded-For";

  @POST
  @Logged
  @Path("/bkey/create")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "Get authorization code; NOTE: you need to use encrypted(symmetric key) "
      +
      "auth-key, client uuid, boxName, boxUUID to exchange an authCode;  authCode in the response uses symmetric key encryption")
  public CreateAuthCodeResult createQrcodeAuthInfo(
      @Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId,
      @Valid CreateAuthCodeInfo createAuthCodeInfo) {
    CreateAuthCodeDTO authCode = boxKeyService.createAuthCode(requestId, createAuthCodeInfo);
    return CreateAuthCodeResult.of(200, "获取授权码成功", authCode);
  }

  @POST
  @LimitReq(keyPrefix="ACREQRATE-", interval = 60, max = 5)
  @Logged
  @Path("/bkey/verify")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description =
      "Verify the authorization code on the new device side (caller: front end of the new device box); "
          +
          "NOTE: you need to use encrypted(box public key) authCode, bkey, tmpEncryptedSecret; " +
          "encryptedSecret, boxName, boxUUID in the response uses tmpEncryptedSecret encryption")
  public EncryptAuthResult verifyQrcodeAuthInfo(
      @Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId,
      @Valid EncryptAuthInfo encryptAuthInfo) {

    String xForwardedFor = request.getHeader(X_FORWARDED_FOR);
    if(!StringUtils.isBlank(xForwardedFor)){
      String ip = xForwardedFor.split(",")[0];
      encryptAuthInfo.setLoginAddress(utils.getCityInfo(ip));
    }
    encryptAuthInfo = totpService.decryptAuthInfo(requestId, encryptAuthInfo);
    EncryptAuthDTO encryptAuthDTO = boxKeyService.verify(requestId, encryptAuthInfo);
    totpService.setCookies(response, encryptAuthInfo.getClientUUID(),
            encryptAuthDTO.expiresAtEpochSeconds() - OffsetDateTime.now().toEpochSecond(), request.getHeader("Host"));
    return EncryptAuthResult.of(200, "OK", requestId, encryptAuthDTO);
  }

  @POST
  @Logged
  @Path("/bkey/refresh")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description =
      "Tries to refresh an access token for further api call with a refresh-token. you need to use encrypted(box public key) tmpEncryptedSecret; ")
  public CreateTokenResult refresh(@Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId,
      @Valid RefreshTokenInfo info,
      @Valid @QueryParam("tmpEncryptedSecret") String tmpEncryptedSecret) {
    var refreshToken = tokenUtils.verifyRefreshToken(requestId, info.getRefreshToken());
    if (refreshToken != null) {
      var address = getAddressByIP();
      totpService.setCookies(response, refreshToken.getClientUUID(),
              refreshToken.getExpiresAt().toEpochSecond() - OffsetDateTime.now().toEpochSecond(), request.getHeader("Host"));
      return boxKeyService.refreshQrcodeAuthInfo(requestId, tmpEncryptedSecret,
          refreshToken.getUserId(), refreshToken.getClientUUID(), address, null);
    } else {
      throw new ServiceOperationException(ServiceError.REFRESH_TOKEN_INVALID);
    }
  }

  @POST
  @Logged
  @Path("/bkey/poll")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "short-polling the result (caller: scan code mobile phone)")
  public VerifyTokenResult pollQrcodeAuthResult(
      @Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId,
      @Valid @NotBlank @QueryParam("bkey") String bkey,
      @Valid @QueryParam("autoLogin") @DefaultValue("true") Boolean autoLogin) {
    var authResult = boxKeyService.getAuthResult(bkey);
    // 为空是被授权端没有获取token等信息，不为空是已获取
    // 为空，自动登录 插入 自动登录有效期为15天
    // 不为空 自动登录 更新 自动登录有效期为15天，（默认设置）
    // 为空，不自动登录 插入 不允许自动登录 有效期为 now
    // 不为空，不自动登录 更新 不允许自动登录 有效期为 now

    if(Boolean.FALSE.equals(autoLogin)){
      authResult.setAutoLogin(false);
      authResult.setAutoLoginExpiresAt(Duration.parse(properties.pushTimeout()).toSeconds());
      cacheService.setAuthCodeInfo(authResult);
    }
    return VerifyTokenResult.of(authResult.isAuthResult(), requestId);
  }

  @POST
  @Logged
  @Path("/auto/login")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "authorized terminal login")
  public ResponseBase<CreateTokenResult> authLogin(@Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId,
      @Valid AuthorizedTerminalLoginInfo info){
    RefreshToken refreshToken = null;
    try {
      refreshToken = tokenUtils.verifyRefreshToken(requestId, info.getRefreshToken());
    } catch (ServiceOperationException e){
      if(e.getErrorCode() == ServiceError.REFRESH_TOKEN_TIMEOUT.getCode()){
        return ResponseBase.<CreateTokenResult>fromResponseBaseEnum(requestId, ResponseBaseEnum.NO_AUTH).build();
      }
    }
    if (refreshToken != null) {
      var address = getAddressByIP();
      totpService.setCookies(response, refreshToken.getClientUUID(),
              refreshToken.getExpiresAt().toEpochSecond() - OffsetDateTime.now().toEpochSecond(), request.getHeader("Host"));
      return boxKeyService.login(requestId, info, address, refreshToken, true);
    } else {
      throw new ServiceOperationException(ServiceError.REFRESH_TOKEN_INVALID);
    }
  }

  @POST
  @Logged
  @Path("/auto/login/poll")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "authorized terminal login")
  public ResponseBase<CreateTokenResult> loginPoll(@Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId,
      @Valid AuthorizedTerminalLoginInfo info){
    var refreshToken = tokenUtils.verifyRefreshToken(requestId, info.getRefreshToken());
    if (refreshToken != null) {
      var address = getAddressByIP();
      totpService.setCookies(response, refreshToken.getClientUUID(),
              refreshToken.getExpiresAt().toEpochSecond() - OffsetDateTime.now().toEpochSecond(), request.getHeader("Host"));
      return boxKeyService.login(requestId, info, address, refreshToken, false);

    } else {
      throw new ServiceOperationException(ServiceError.REFRESH_TOKEN_INVALID);
    }
  }

  @POST
  @Logged
  @Path("/auto/login/confirm")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "authorized terminal login")
  public ResponseBase<VerifyTokenResult> loginConfirm(@Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId,
      @Valid AuthorizedTerminalLoginConfirmInfo info) {
    final var accessToken = tokenUtils.checkAccessToken(requestId, info.getAccessToken());
    var clientUUID = securityUtils.decryptWithSecret(info.getEncryptedClientUUID(), accessToken.getSharedSecret(),
        accessToken.getSharedInitializationVector());
    AuthorizedTerminalEntity authorizedTerminalEntity;
    if(Boolean.FALSE.equals(info.getLogin())){
      authorizedTerminalService.updateAuthorizedTerminalValidTime(requestId, AuthorizedTerminalInfo.of(accessToken.getUserId(),
          clientUUID,"",-Duration.parse(properties.timeOfAllowLogin()).getSeconds(),"",""));
      return ResponseBase.ok(requestId, VerifyTokenResult.of(true, requestId)).build();
    }
    if(Boolean.TRUE.equals(info.getAutoLogin())){
      authorizedTerminalEntity = authorizedTerminalService.updateAuthorizedTerminalValidTime(requestId, AuthorizedTerminalInfo.of(
          accessToken.getUserId(), clientUUID, "",
              Duration.parse(properties.timeOfAllowAutoLogin()).getSeconds(),
          "", ""));
    } else {
      authorizedTerminalEntity = authorizedTerminalService.updateAuthorizedTerminalValidTime(requestId, AuthorizedTerminalInfo.of(
          accessToken.getUserId(), clientUUID, "", Duration.parse(properties.pushTimeout()).getSeconds(),
          "", ""));
    }

    if(Objects.isNull(authorizedTerminalEntity)){
      throw new ServiceOperationException(ServiceError.CLIENT_UUID_NOT_FOUND);
    }
    return ResponseBase.ok(requestId, VerifyTokenResult.of(true, null)).build();
  }

  public String getAddressByIP(){
    var address = "";
    String xForwardedFor = request.getHeader(X_FORWARDED_FOR);
    if(!StringUtils.isBlank(xForwardedFor)){
      String ip = xForwardedFor.split(",")[0];
      address = utils.getCityInfo(ip);
    }
    return address;
  }

}
