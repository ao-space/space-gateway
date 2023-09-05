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

package space.ao.services.auth.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerResponse;
import space.ao.services.gateway.auth.qrcode.service.CacheService;
import space.ao.services.account.authorizedterminalinfo.dto.AuthorizedTerminalInfo;
import space.ao.services.account.authorizedterminalinfo.repository.AuthorizedTerminalRepository;
import space.ao.services.account.authorizedterminalinfo.service.AuthorizedTerminalService;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.auth.repository.TotpRepository;
import space.ao.services.gateway.auth.CreateTokenResult;
import space.ao.services.gateway.auth.qrcode.dto.BoxLanInfo;
import space.ao.services.gateway.auth.qrcode.dto.EncryptAuthInfo;
import space.ao.services.gateway.auth.qrcode.dto.TotpAuthCode;
import space.ao.services.gateway.auth.qrcode.dto.CreateAuthCodeDTO;
import space.ao.services.push.dto.Message;
import space.ao.services.push.dto.NotificationEnum;
import space.ao.services.push.dto.TerminalInfo;
import space.ao.services.push.services.RedisService;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.StringUtils;
import space.ao.services.support.TokenUtils;
import space.ao.services.support.agent.AgentService;
import space.ao.services.support.log.Logged;
import space.ao.services.support.security.SecurityTotpUtils;
import space.ao.services.support.security.SecurityUtils;

import java.util.Objects;

@ApplicationScoped
public class TotpService {

  @Inject
  TotpRepository totpRepository;
  @Inject
  UserInfoRepository userInfoRepository;
  @Inject
  SecurityUtils securityUtils;
  @Inject
  SecurityTotpUtils securityTotpUtils;
  @Inject
  AgentService agentService;
  @Inject
  RedisService redisService;
  @Inject
  AuthorizedTerminalService authorizedTerminalService;
  @Inject
  AuthorizedTerminalRepository authorizedTerminalRepository;
  @Inject
  TokenUtils tokenUtils;
  @Inject
  OperationUtils utils;
  @Inject
  CacheService cacheService;

  public Boolean verifyUser(EncryptAuthInfo authInfo){
    var user = userInfoRepository.findByDomain(authInfo.getSpaceId());
    if(user.isPresent()){
        return true;
    } else {
      var users = userInfoRepository.findByPersonalName(authInfo.getSpaceId());
      return !users.isEmpty();
    }
  }

  public UserEntity verifyAuthCode(EncryptAuthInfo authInfo) {
    var user = userInfoRepository.findByDomain(authInfo.getSpaceId());
    if(user.isPresent()){
      return user.map(userEntity -> verifyAuthCode(authInfo.getAuthCode(), userEntity.getId())).orElse(null);
    } else {
      var usersByPersonalName = userInfoRepository.findByPersonalName(authInfo.getSpaceId());
      return usersByPersonalName.stream().map(userEntity -> verifyAuthCode(authInfo.getAuthCode(), userEntity.getId()))
              .filter(Objects::nonNull).findFirst().orElse(null);
    }
  }

  @Logged
  public UserEntity verifyAuthCode(String authCode, Long userid) {
    var totp = totpRepository.findByUserid(userid);
    if(totp.isPresent()){
      if (securityTotpUtils.verifyCode(totp.get().getTotpSecret(), authCode)) {
        return userInfoRepository.findByUserId(totp.get().getUserid());
      }
    }
    return null;
  }

  /**
   * 通过 userid 生成 authCode
   */
  public TotpAuthCode generateAuthCode(Long userid) {
    var totpEntity = totpRepository.findByUserid(userid);
    String totpSecret;
    if (totpEntity.isEmpty()) {
      totpSecret = generateTotpSecretAndSave(userid);
    } else {
      totpSecret = totpEntity.get().getTotpSecret();
    }
    return securityTotpUtils.generateCode(totpSecret);
  }

  /**
   * 通过 bkey 获取 userid
   */
  @Logged
  public CreateAuthCodeDTO getUserIdByBkey(String bkey) {
    return cacheService.getAuthCodeInfo(bkey);
  }
  /**
   * 保存 userid
   */
  public void saveUserIdByBkey(CreateAuthCodeDTO createAuthCodeDTO) {
    cacheService.setAuthCodeInfo(createAuthCodeDTO);
  }

  /**
   * 生成一次性时间验证密码 secret 并保存到数据库，已存在则更新
   */
  public String generateTotpSecretAndSave(Long userid) {
    var totpSecret = securityTotpUtils.generateTotpSecret();
    totpRepository.insertOrUpdate(userid, totpSecret);
    return totpSecret;
  }

  /**
   * 通过 userid 获取 BoxLanInfo
   */
  public BoxLanInfo getBoxLanInfo(String requestId, String userid) {
    var userEntity = userInfoRepository.findByUserId(Long.valueOf(userid));
    var boxLanInfo = agentService.getBoxLanInfo(requestId);
    var userDomain = userEntity.getUserDomain();
    return BoxLanInfo.of(securityUtils.getBoxPublicKey(requestId), boxLanInfo.getLanIp(), userDomain, boxLanInfo.getPort(), boxLanInfo.getTlsPort());
  }

  /**
   * 创建 token 推送终端登录消息到绑定端，并将终端信息保存到数据库
   */
  public CreateTokenResult createTokenAndPushMessage(String requestId, EncryptAuthInfo encryptAuthInfo, String userid,
                                                     String clientUUID, Long expireAt) {

    var createTokenResult = tokenUtils.createDefaultTokenResult(requestId, encryptAuthInfo.getTmpEncryptedSecret(),
            userid, encryptAuthInfo.getClientUUID(), null);

    // 临时授权写入 account
    var authorizedTerminal = authorizedTerminalRepository.findByUseridAndUuid(Long.valueOf(userid), encryptAuthInfo.getClientUUID());


    var authorizedTerminalInfo = AuthorizedTerminalInfo.of(userid, encryptAuthInfo.getClientUUID(), encryptAuthInfo.getTerminalMode(),
            expireAt, encryptAuthInfo.getLoginAddress(), encryptAuthInfo.getTerminalType());

    if(Objects.isNull(authorizedTerminal)){ // 为空直接新增记录，不为空是更新记录，但是保留之前的自动登录时间
      authorizedTerminal = authorizedTerminalService.insertAuthorizedTerminalInfo(requestId, authorizedTerminalInfo);
    } else {
      authorizedTerminal = authorizedTerminalService.updateAuthorizedTerminalValidTime(requestId, authorizedTerminalInfo);
    }

    var terminalInfo = new TerminalInfo(authorizedTerminal.getAoid(),authorizedTerminal.getUuid(), authorizedTerminal.getTerminalMode(),
            authorizedTerminal.getAddress(), authorizedTerminal.getTerminalType());

    redisService.pushMessage(Message.of(userid, clientUUID, NotificationEnum.LOGIN.getType(), requestId, utils.objectToJson(terminalInfo)));

    return createTokenResult;
  }

  @Logged
  public EncryptAuthInfo decryptAuthInfo(String requestId, EncryptAuthInfo encryptAuthInfo) {
    encryptAuthInfo.setAuthCode(securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId,
            encryptAuthInfo.getAuthCode()));

    if(encryptAuthInfo.getClientUUID().length() > 36){ // 兼容老的 app 没有加密 clientUUID
      encryptAuthInfo.setClientUUID(securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId,
              encryptAuthInfo.getClientUUID()));
    }
    if(!StringUtils.isBlank(encryptAuthInfo.getBkey())){
      encryptAuthInfo.setBkey(securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId,
              encryptAuthInfo.getBkey()));
    }
    if(!StringUtils.isBlank(encryptAuthInfo.getSpaceId())){
      encryptAuthInfo.setSpaceId(securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId,
              encryptAuthInfo.getSpaceId()));
    }
    if(!StringUtils.isBlank(encryptAuthInfo.getVersion()) && encryptAuthInfo.getVersion().length() > 10){
      encryptAuthInfo.setVersion(securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId,
              encryptAuthInfo.getVersion()));
    }
    return encryptAuthInfo;
  }

  public void setAuthenticatorStatus(long userId, boolean status){
    totpRepository.setAuthenticatorStatus(userId, status);
  }

  public void setCookies(HttpServerResponse response, String clientUUID, long expire, String userDomain){
    response.addCookie(Cookie.cookie("client_uuid", clientUUID).setMaxAge(expire).setPath("/").setDomain(userDomain));
  }
}
