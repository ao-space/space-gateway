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

package space.ao.services.gateway.auth.qrcode.rest;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import space.ao.services.account.personalinfo.dto.AccountInfoResult;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.auth.service.TotpService;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.gateway.auth.qrcode.dto.*;
import space.ao.services.gateway.auth.qrcode.dto.v2.BkeyInfo;
import space.ao.services.gateway.auth.qrcode.dto.v2.CreateAuthCodeResult;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.RestConfiguration;
import space.ao.services.support.StringUtils;
import space.ao.services.support.limit.LimitReq;
import space.ao.services.support.log.Logged;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.support.response.ResponseBaseEnum;
import space.ao.services.support.security.SecurityUtils;
import space.ao.services.support.service.ServiceError;
import space.ao.services.support.service.ServiceOperationException;

import javax.crypto.spec.IvParameterSpec;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Objects;

@Path("/v1/api/gateway/totp")
@Tag(name = "Space Gateway QRCode-scanning Service base on TOTP",
        description = "Provides overall space requests' scan QR code service base on TOTP.")
public class TotpGatewayResource {

  @Inject
  TotpService totpService;

  @Inject
  ApplicationProperties properties;
  @Inject
  OperationUtils utils;
  @Inject
  UserInfoRepository userInfoRepository;

  @Context
  HttpServerRequest request;
  @Context
  HttpServerResponse response;
  @Inject
  SecurityUtils securityUtils;

  private static final String X_FORWARDED_FOR = "X-Forwarded-For";

  @POST
  @Path("bkey")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description =
          "被授权端调用获取 bkey.")
  @Logged
  public ResponseBase<String> bkey(@Valid @NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId) {
    String bkey = utils.createRandomType4UUID();
    totpService.saveUserIdByBkey(new CreateAuthCodeDTO(bkey, System.currentTimeMillis(), false,
            Duration.parse(properties.timeOfAllowAutoLogin()).toSeconds()));
    return ResponseBase.ok(requestId, bkey).build();
  }

  @GET
  @Path("/bkey")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description =
          "被授权端调用获取 bkey 和公钥.")
  @Logged
  public ResponseBase<CreateBkeyInfo> bkey(@Valid @NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId,
                                           @QueryParam("spaceId") String spaceId) {
    String bkey = utils.createRandomType4UUID();
    totpService.saveUserIdByBkey(new CreateAuthCodeDTO(bkey, System.currentTimeMillis(), false,
            Duration.parse(properties.timeOfAllowAutoLogin()).toSeconds()));
    var user = userInfoRepository.findByDomain(spaceId);
    if(user.isPresent()){
      return ResponseBase.ok(requestId, CreateBkeyInfo.of(bkey, totpService.getBoxLanInfo(requestId, user.get().getId().toString()))).build();
    }

    var usersByPersonalName = userInfoRepository.findByPersonalName(spaceId);
    if(usersByPersonalName.isEmpty()){
      return ResponseBase.<CreateBkeyInfo>notFound(requestId).build();
    }
    if(usersByPersonalName.size() > 1){
      return ResponseBase.<CreateBkeyInfo>fromResponseBaseEnum(requestId, ResponseBaseEnum.SPACE_ID_NOT_UNIQUE).build();
    }
    return ResponseBase.ok(requestId, CreateBkeyInfo.of(bkey, totpService.getBoxLanInfo(requestId, usersByPersonalName.get(0).getId().toString()))).build();
  }


  @POST
  @Path("/bkey/poll")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "被授权端轮询校检结果.")
  @Logged
  public ResponseBase<CreateAuthCodeResult> bkeyPoll(@Valid @NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId,
                                                     @Valid BkeyInfo bkeyInfo) {
    var createAuthCodeDTO = totpService.getUserIdByBkey(bkeyInfo.getBkey());
    if(createAuthCodeDTO == null){
      return ResponseBase.<CreateAuthCodeResult>notFound(requestId).build();
    }
    if(createAuthCodeDTO.getUserId() != null){
      return ResponseBase.ok(requestId, CreateAuthCodeResult.of(
              true, totpService.getBoxLanInfo(requestId, createAuthCodeDTO.getUserId())
      )).build();
    } else {
      return ResponseBase.ok(requestId, CreateAuthCodeResult.of(false, null)).build();
    }
  }

  @POST
  @LimitReq(keyPrefix="ACREQRATE-", interval = 60, max = 5)
  @Path("/bkey/verify")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Logged
  public ResponseBase<EncryptAuthDTO> verify(@Valid @NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId,
                                             @Valid EncryptAuthInfo encryptAuthInfo) {

    var authInfo = totpService.decryptAuthInfo(requestId, encryptAuthInfo);
    String aoid, userid, userBindClientUUID;
    var expireAt = Duration.parse(properties.timeOfAllowAutoLogin()).toSeconds();
    var autoLogin = true;

    String xForwardedFor = request.getHeader(X_FORWARDED_FOR);
    String ip = request.remoteAddress().host();
    if(!StringUtils.isBlank(xForwardedFor)){
      ip = xForwardedFor.split(",")[0];
      authInfo.setLoginAddress(utils.getCityInfo(ip));
    }
    UserEntity userEntity;
    if(!StringUtils.isBlank(authInfo.getSpaceId())){
      if(!utils.isLocalAddress(ip)){
        return ResponseBase.<EncryptAuthDTO>notAcceptable(requestId).build();
      }
      // 局域网或者直接域名登陆的
      var userIsExist = totpService.verifyUser(authInfo);

      if(userIsExist) {
        userEntity = totpService.verifyAuthCode(authInfo);
      } else {
        return ResponseBase.<EncryptAuthDTO>fromResponseBaseEnum(requestId, ResponseBaseEnum.INVALID_USER).build();
      }
      if(userEntity == null){
        return ResponseBase.<EncryptAuthDTO>fromResponseBaseEnum(requestId, ResponseBaseEnum.AUTH_CODE_NOT_MATCH).build();
      }
    } else {
      // 扫码登陆的
      var createAuthCodeDTO = totpService.getUserIdByBkey(authInfo.getBkey());
      if(createAuthCodeDTO == null || !Objects.equals(createAuthCodeDTO.getBkey(), authInfo.getBkey())){
        throw new ServiceOperationException(ServiceError.BOX_KEY_NOT_MATCH);
      }
      userid = createAuthCodeDTO.getUserId();
      if(Objects.equals(authInfo.getVersion(), "v2")){
        userEntity = totpService.verifyAuthCode(authInfo.getAuthCode(), Long.parseLong(userid));
        if(userEntity == null){
          return ResponseBase.<EncryptAuthDTO>fromResponseBaseEnum(requestId, ResponseBaseEnum.AUTH_CODE_NOT_MATCH).build();
        }
      } else {
        // 校检授权码(authCode)
        if (!authInfo.getAuthCode().equals(createAuthCodeDTO.getAuthCode())) {
          return ResponseBase.<EncryptAuthDTO>fromResponseBaseEnum(requestId, ResponseBaseEnum.AUTH_CODE_NOT_MATCH).build();
        }
        userEntity = userInfoRepository.findById(Long.parseLong(userid));
      }
      autoLogin = createAuthCodeDTO.getAutoLogin();
      if(!autoLogin){
        // web 侧不自动登陆的时候有效期 1天
        if(Objects.nonNull(authInfo.getTerminalType()) &&
                authInfo.getTerminalType().equalsIgnoreCase("web")){
          expireAt = Duration.parse(properties.gatewayTimeOfQrAkLife()).toSeconds();
        }
      } else {
        expireAt = createAuthCodeDTO.getAutoLoginExpiresAt();
      }
      createAuthCodeDTO.setAuthResult(true);
      totpService.saveUserIdByBkey(createAuthCodeDTO);
    }

    aoid = userEntity.getAoId();
    userid = userEntity.getId().toString();
    userBindClientUUID = userEntity.getClientUUID();

    var createTokenResult = totpService.createTokenAndPushMessage(requestId, authInfo, userid, userBindClientUUID, expireAt);

    var ivParameterSpec = new IvParameterSpec(Base64.getDecoder().decode(
            createTokenResult.getAlgorithmConfig().getTransportation().getInitializationVector()));
    var tempSecret = securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(
            requestId, encryptAuthInfo.getTmpEncryptedSecret());

    String boxName = securityUtils.encryptWithSecret(properties.boxName(), tempSecret, ivParameterSpec);
    String boxUUID = securityUtils.encryptWithSecret(properties.boxUuid(), tempSecret, ivParameterSpec);
    aoid = securityUtils.encryptWithSecret(aoid, tempSecret, ivParameterSpec);

    String encryptedAuthUserInfo;

    var accountInfoResult = AccountInfoResult.of(userEntity.getRole().name(), userEntity.getPersonalName(),
            userEntity.getPersonalSign(), userEntity.getCreateAt(),userEntity.getAoId(), userEntity.getClientUUID(),
            userEntity.getPhoneModel(), userEntity.getUserDomain(), userEntity.getImageMd5(), null, null, null);

    encryptedAuthUserInfo = securityUtils.encryptWithSecret(utils.objectToJson(EncryptedAuthUserInfo.of(
            accountInfoResult, securityUtils.getDeviceInfo())), tempSecret, ivParameterSpec);
    totpService.setCookies(response, authInfo.getClientUUID(),
            expireAt, request.getHeader("Host"));
    return ResponseBase.ok(requestId,EncryptAuthDTO.of(boxName, boxUUID, aoid, createTokenResult,
            autoLogin, ZonedDateTime.now().plusSeconds(expireAt).toString(), totpService.getBoxLanInfo(requestId,
                    userid), encryptedAuthUserInfo)).build();
  }

}
