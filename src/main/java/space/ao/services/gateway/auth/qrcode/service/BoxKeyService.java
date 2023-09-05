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

package space.ao.services.gateway.auth.qrcode.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.spec.IvParameterSpec;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import space.ao.services.account.authorizedterminalinfo.dto.AuthorizedTerminalInfo;
import space.ao.services.account.authorizedterminalinfo.repository.AuthorizedTerminalRepository;
import space.ao.services.account.authorizedterminalinfo.service.AuthorizedTerminalService;
import space.ao.services.account.member.service.MemberManageService;
import space.ao.services.account.personalinfo.dto.AccountInfoResult;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.auth.service.TotpService;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.gateway.auth.CreateTokenResult;
import space.ao.services.gateway.auth.qrcode.dto.*;
import space.ao.services.push.dto.Message;
import space.ao.services.push.dto.NotificationEnum;
import space.ao.services.push.dto.TerminalInfo;
import space.ao.services.push.services.RedisService;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.StringUtils;
import space.ao.services.support.TokenUtils;
import space.ao.services.support.agent.AgentServiceRestClient;
import space.ao.services.support.log.Logged;
import space.ao.services.support.model.AccessToken;
import space.ao.services.support.model.RefreshToken;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.support.response.ResponseBaseEnum;
import space.ao.services.support.security.SecurityUtils;
import space.ao.services.support.service.ServiceError;
import space.ao.services.support.service.ServiceOperationException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BoxKeyService {

  @Inject
  OperationUtils utils;
  @Inject
  TokenUtils tokenUtils;
  @Inject
  SecurityUtils securityUtils;
  @Inject
  CacheService cacheService;

  @Inject
  MemberManageService memberManageService;

  @Inject
  RedisService redisService;
  @Inject
  UserInfoRepository userInfoRepository;
  @Inject
  AuthorizedTerminalService authorizedTerminalService;
  @Inject
  TotpService totpService;
  @Inject
  AuthorizedTerminalRepository authorizedTerminalRepository;
  @Inject
  ApplicationProperties properties;


  @Inject @RestClient
  AgentServiceRestClient agentServiceRestClient;

  static final String ERROR_COUNTER_LOGIN_FAILED = "LFC-"; // login failed counter ， + userid + clientUUID
  static final String UNKNOWN = "unknown";
  public CreateAuthCodeDTO createAuthCode(String requestId, CreateAuthCodeInfo createAuthCodeInfo) {
    AccessToken ak = tokenUtils.verifyAccessToken(requestId, createAuthCodeInfo.getAccessToken());
    if (ak == null) {
      throw new ServiceOperationException(ServiceError.ACCESS_TOKEN_INVALID);
    }
    String authKey = securityUtils.decryptWithSecret(
        createAuthCodeInfo.getAuthKey(), ak.getSharedSecret(), ak.getSharedInitializationVector());
    String clientUUID = securityUtils.decryptWithSecret(
        createAuthCodeInfo.getClientUUID(), ak.getSharedSecret(),
        ak.getSharedInitializationVector());
    UserEntity userEntity = memberManageService.findByClientUUID(clientUUID);
    if(Objects.isNull(userEntity)){
      throw new ServiceOperationException(ServiceError.CLIENT_UUID_NOT_FOUND);
    }
    if (!userEntity.getAuthKey().equals(authKey)) {
      throw new ServiceOperationException(ServiceError.AUTH_KEY_NOT_MATCH);
    }

    String aoId = userEntity.getAoId();
    String userid = String.valueOf(userEntity.getId());

    CreateAuthCodeDTO createAuthCodeDTO = new CreateAuthCodeDTO();

    // generate authCode
    String authCode = utils.createRandomNumbers(4);
    if(!StringUtils.isBlank(createAuthCodeInfo.getVersion()) &&
            createAuthCodeInfo.getVersion().equalsIgnoreCase("v2")){
      var autCodeInfo = totpService.generateAuthCode(Long.parseLong(userid));
      authCode = String.valueOf(autCodeInfo.getAuthCode());
      createAuthCodeDTO.setAuthCodeExpiresAt(autCodeInfo.getAuthCodeExpiresAt());
      createAuthCodeDTO.setAuthCodeTotalExpiresAt(autCodeInfo.getAuthCodeTotalExpiresAt());
    }
    String bkey = utils.createRandomType4UUID();

    long currentTimeMillis = System.currentTimeMillis();
    String tmpClientUUID = utils.createRandomType4UUID();

    var expiresAt = Duration.parse(properties.timeOfAllowAutoLogin()).getSeconds();

    cacheService.setAuthCodeInfo(
        new CreateAuthCodeDTO(aoId, userid, tmpClientUUID, authCode, bkey, currentTimeMillis, true, expiresAt));

    createAuthCodeDTO.setAuthCode(
            securityUtils.encryptWithSecret(authCode, ak.getSharedSecret(),
            ak.getSharedInitializationVector()));
    createAuthCodeDTO.setBkey(
            securityUtils.encryptWithSecret(bkey, ak.getSharedSecret(), ak.getSharedInitializationVector()));
    createAuthCodeDTO.setCreateTime(currentTimeMillis);

    var ipAddressInfo = agentServiceRestClient.getIpAddressInfo(requestId);
    var userDomainSuffix = utils.getUserDomainSuffix();
    if(Objects.nonNull(userEntity.getUserDomain())){
      createAuthCodeDTO.setLanDomain(userEntity.getUserDomain().split("\\.")[0]
              + ".lan." + userDomainSuffix);
    }
    if(!ipAddressInfo.results().isEmpty()){
      createAuthCodeDTO.setLanIp(ipAddressInfo.results().get(0).getIp());
    }
    return createAuthCodeDTO;
  }

  @Logged
  public EncryptAuthDTO verify(String requestId, EncryptAuthInfo encryptAuthInfo) {

    if (!cacheService.hasKey(encryptAuthInfo.getBkey())) {
      throw new ServiceOperationException(ServiceError.BOX_KEY_NOT_MATCH);
    }

    var createAuthCodeDTO = cacheService.getAuthCodeInfo(encryptAuthInfo.getBkey());

    if(!StringUtils.isBlank(encryptAuthInfo.getVersion()) && encryptAuthInfo.getVersion().equalsIgnoreCase("v2")){
      totpService.verifyAuthCode(encryptAuthInfo.getAuthCode(), Long.parseLong(createAuthCodeDTO.getUserId()));
    } else {
      // 校检授权码(authCode)
      if (!encryptAuthInfo.getAuthCode().equals(createAuthCodeDTO.getAuthCode())) {
        throw new ServiceOperationException(ServiceError.AUTH_CODE_NOT_MATCH);
      }
    }

    // 校检授权码(box key)
    if (!encryptAuthInfo.getBkey().equals(createAuthCodeDTO.getBkey())) {
      throw new ServiceOperationException(ServiceError.BOX_KEY_NOT_MATCH);
    }

    var createTokenResult = tokenUtils.createDefaultTokenResult(requestId,
        encryptAuthInfo.getTmpEncryptedSecret(),
        createAuthCodeDTO.getUserId(), encryptAuthInfo.getClientUUID() != null ?
            encryptAuthInfo.getClientUUID() : createAuthCodeDTO.getClientUUID(), null);

    var ivParameterSpec = new IvParameterSpec(Base64.getDecoder().decode(
        createTokenResult.getAlgorithmConfig().getTransportation().getInitializationVector()));

    createAuthCodeDTO.setAuthResult(true);
    // 临时授权写入 account
    var authorizedTerminal = authorizedTerminalRepository.findByUseridAndUuid(
        Long.valueOf(createAuthCodeDTO.getUserId()), encryptAuthInfo.getClientUUID());
    // web 侧有效期 1天
    if(Objects.nonNull(encryptAuthInfo.getTerminalType()) &&
        encryptAuthInfo.getTerminalType().equalsIgnoreCase("web") &&
        createAuthCodeDTO.getAutoLoginExpiresAt() < Duration.parse(properties.gatewayTimeOfQrAkLife()).toSeconds()
    ){
      createAuthCodeDTO.setAutoLoginExpiresAt(Duration.parse(properties.gatewayTimeOfQrAkLife()).toSeconds());
    }
    var authorizedTerminalInfo = AuthorizedTerminalInfo.of(
        createAuthCodeDTO.getUserId(),
        encryptAuthInfo.getClientUUID() != null ? encryptAuthInfo.getClientUUID() : createAuthCodeDTO.getClientUUID(),
        encryptAuthInfo.getTerminalMode() != null ? encryptAuthInfo.getTerminalMode() : UNKNOWN,
        createAuthCodeDTO.getAutoLoginExpiresAt(),
        encryptAuthInfo.getLoginAddress()!= null ? encryptAuthInfo.getLoginAddress() : UNKNOWN,
        encryptAuthInfo.getTerminalType()!= null ? encryptAuthInfo.getTerminalType() : UNKNOWN
    );

    if(Objects.isNull(authorizedTerminal)){ // 为空直接新增记录，不为空是更新记录，但是保留之前的自动登录时间
      authorizedTerminal = authorizedTerminalService.insertAuthorizedTerminalInfo(requestId, authorizedTerminalInfo);
    } else {
      authorizedTerminal = authorizedTerminalService.updateAuthorizedTerminalValidTime(requestId, authorizedTerminalInfo);
    }
    createAuthCodeDTO.setClientUUID(authorizedTerminal.getUuid());
    //更新value值
    cacheService.setAuthCodeInfo(createAuthCodeDTO);

    var userInfo = userInfoRepository.findByUserId(authorizedTerminal.getUserid());

    var terminalInfo = new TerminalInfo(authorizedTerminal.getAoid(),authorizedTerminal.getUuid(), authorizedTerminal.getTerminalMode(),
        authorizedTerminal.getAddress(), authorizedTerminal.getTerminalType());
    redisService.pushMessage(
        Message.of(String.valueOf(userInfo.getId()), userInfo.getClientUUID(), NotificationEnum.LOGIN.getType(), requestId,
            utils.objectToJson(terminalInfo)));

    var tempSecret = securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(
            requestId, encryptAuthInfo.getTmpEncryptedSecret());
    String boxName = securityUtils.encryptWithSecret(properties.boxName(), tempSecret, ivParameterSpec);
    String boxUUID = securityUtils.encryptWithSecret(properties.boxUuid(), tempSecret, ivParameterSpec);
    String aoid = securityUtils.encryptWithSecret(createAuthCodeDTO.getAoId(), tempSecret, ivParameterSpec);
    String encryptedAuthUserInfo;

    var accountInfoResult = AccountInfoResult.of(userInfo.getRole().name(), userInfo.getPersonalName(),
            userInfo.getPersonalSign(), userInfo.getCreateAt(),userInfo.getAoId(), userInfo.getClientUUID(),
            userInfo.getPhoneModel(), userInfo.getUserDomain(), userInfo.getImageMd5(), null, null, null);

    encryptedAuthUserInfo = securityUtils.encryptWithSecret(utils.objectToJson(EncryptedAuthUserInfo.of(
            accountInfoResult, securityUtils.getDeviceInfo()
    )), tempSecret, ivParameterSpec);
    return EncryptAuthDTO.of(boxName, boxUUID, aoid, createTokenResult, createAuthCodeDTO.getAutoLogin(),
        ZonedDateTime.now().plusSeconds(createAuthCodeDTO.getAutoLoginExpiresAt()).toString(),
            totpService.getBoxLanInfo(requestId, userInfo.getId().toString()), encryptedAuthUserInfo);  // 返回 access_token和后续解密密钥
  }

  public CreateTokenResult refreshQrcodeAuthInfo(String requestId, String tmpEncryptedSecret,
      String userId, String clientUUID, String address, Long expireAt) {
    var createTokenResult = tokenUtils.createDefaultTokenResult(requestId, tmpEncryptedSecret, userId,
        clientUUID, null);
    if(Objects.isNull(expireAt)){
      expireAt = createTokenResult.getExpiresAtEpochSeconds() - OffsetDateTime.now().toEpochSecond();
    }
    var authorizedTerminal = authorizedTerminalRepository.findByUseridAndUuid(Long.valueOf(userId), clientUUID);

    if(authorizedTerminal==null){
      throw new ServiceOperationException(ServiceError.CLIENT_UUID_NOT_FOUND);
    }else{
      var updateResult = authorizedTerminalService.updateAuthorizedTerminalValidTime(requestId, AuthorizedTerminalInfo.of(
          userId, clientUUID, authorizedTerminal.getTerminalMode(), expireAt,
          address != null ? address : authorizedTerminal.getAddress(), authorizedTerminal.getTerminalType()
      ));
      var userInfo = userInfoRepository.findByUserId(Long.valueOf(userId));
      var terminalInfo = new TerminalInfo(updateResult.getAoid(),updateResult.getUuid(), updateResult.getTerminalMode(),
          updateResult.getAddress(), updateResult.getTerminalType());
      redisService.pushMessage(Message.of(String.valueOf(userInfo.getId()),
          userInfo.getClientUUID(), NotificationEnum.LOGIN.getType(), requestId, utils.objectToJson(terminalInfo)));
      return createTokenResult;
    }
  }

  @Logged
  public ResponseBase<CreateTokenResult> login(String requestId, AuthorizedTerminalLoginInfo info,
      String address, RefreshToken refreshToken, boolean push){

      var authorizedTerminal = authorizedTerminalRepository.findByUseridAndUuid(Long.valueOf(refreshToken.getUserId()), refreshToken.getClientUUID());
      // 三种情况，1、直接登录 2、免扫码登录 3、直接进入扫码页面

      if(Objects.isNull(authorizedTerminal)){
        return ResponseBase.<CreateTokenResult>fromResponseBaseEnum(requestId, ResponseBaseEnum.NO_AUTH).build();
      }
      if(authorizedTerminal.getCreateAt().isBefore(OffsetDateTime.now().minusSeconds(Duration.parse(properties.timeOfAllowLogin()).toSeconds()))){
        // 创建时间在三十天之前
        authorizedTerminalService.delAuthorizedTerminalInfo(requestId, AuthorizedTerminalInfo.of(authorizedTerminal.getUserid().toString(),
            authorizedTerminal.getUuid(),"",0,"",""));
        return ResponseBase.<CreateTokenResult>fromResponseBaseEnum(requestId, ResponseBaseEnum.NO_AUTH).build();
      }
      if(Boolean.FALSE.equals(push)){
        // 取消登录时 3、直接进入扫码页面
        if(authorizedTerminal.getExpireAt().isBefore(OffsetDateTime.now().minusSeconds(Duration.parse(properties.timeOfAllowLogin()).toSeconds()))){
          authorizedTerminalService.logoutAuthorizedTerminalInfo(requestId, refreshToken.getUserId(), refreshToken.getClientUUID());
          return ResponseBase.<CreateTokenResult>fromResponseBaseEnum(requestId, ResponseBaseEnum.CANCEL_LOGIN).build();
        }
      }

      if(authorizedTerminal.getExpireAt().isAfter(OffsetDateTime.now())){
        // 1、有效期在现在之后(yml 文件 time-of-allow-automatic-login 控制)天之内 ，直接登录
        var expireAt = authorizedTerminal.getExpireAt().getLong(ChronoField.INSTANT_SECONDS) - OffsetDateTime.now()
            .getLong(ChronoField.INSTANT_SECONDS);
        // web 侧有效期 1天
        if(Objects.nonNull(authorizedTerminal.getTerminalType()) &&
            authorizedTerminal.getTerminalType().equalsIgnoreCase("web") &&
            expireAt < Duration.parse(properties.gatewayTimeOfQrAkLife()).toSeconds()){
          expireAt = Duration.parse(properties.gatewayTimeOfQrAkLife()).toSeconds();
        }
        var result = refreshQrcodeAuthInfo(requestId, info.getTmpEncryptedSecret(),
            refreshToken.getUserId(), refreshToken.getClientUUID(), address, expireAt
            );
        result.setAutoLoginExpiresAt(authorizedTerminal.getExpireAt().toString());

        var tempSecret = securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(
                requestId, info.getTmpEncryptedSecret());
        var userInfo = userInfoRepository.findByUserId(Long.valueOf(refreshToken.getUserId()));
        var accountInfoResult = AccountInfoResult.of(userInfo.getRole().name(), userInfo.getPersonalName(),
                userInfo.getPersonalSign(), userInfo.getCreateAt(),userInfo.getAoId(), userInfo.getClientUUID(),
                userInfo.getPhoneModel(), userInfo.getUserDomain(), userInfo.getImageMd5(), null, null, null);

        var encryptedAuthUserInfo = securityUtils.encryptWithSecret(utils.objectToJson(EncryptedAuthUserInfo.of(
                accountInfoResult, securityUtils.getDeviceInfo()
        )), tempSecret, new IvParameterSpec(Base64.getDecoder().decode(result.getAlgorithmConfig().getTransportation().getInitializationVector())));
        result.setExContext(encryptedAuthUserInfo);
        redisService.resetFailedLoginCounter(ERROR_COUNTER_LOGIN_FAILED, refreshToken.getUserId(), refreshToken.getClientUUID());
        return ResponseBase.ok(requestId, result).build();
      } else {
        if(push){ // 第一次需要推送
          // 2、免扫码登录 推送一条消息到用户绑定端（成员、管理员）
          if(redisService.increaseFailedLoginCounter(ERROR_COUNTER_LOGIN_FAILED, refreshToken.getUserId(), refreshToken.getClientUUID()) > 3){
            redisService.resetFailedLoginCounter(ERROR_COUNTER_LOGIN_FAILED, refreshToken.getUserId(), refreshToken.getClientUUID());
            return ResponseBase.<CreateTokenResult>fromResponseBaseEnum(requestId, ResponseBaseEnum.NO_AUTH).build();
          }
          var userInfo = userInfoRepository.findByUserId(authorizedTerminal.getUserid());
          var terminalInfo = new TerminalInfo(authorizedTerminal.getAoid(),authorizedTerminal.getUuid(), authorizedTerminal.getTerminalMode(),
              authorizedTerminal.getAddress(), authorizedTerminal.getTerminalType());
          redisService.pushMessage(Message.of(String.valueOf(userInfo.getId()),
              userInfo.getClientUUID(), NotificationEnum.LOGIN_CONFIRM.getType(), requestId, utils.objectToJson(terminalInfo)));
        }
        authorizedTerminalService.logoutAuthorizedTerminalInfo(requestId, refreshToken.getUserId(), refreshToken.getClientUUID());
        return ResponseBase.<CreateTokenResult>fromResponseBaseEnum(requestId, ResponseBaseEnum.NO_AUTO_LOGIN).build();
      }
  }


  public CreateAuthCodeDTO getAuthResult(String bkey) {
    var result = cacheService.getAuthCodeInfo(bkey);
    if (result.isAuthResult()) {
      cacheService.remove(bkey);
    }
    return result;
  }
}
