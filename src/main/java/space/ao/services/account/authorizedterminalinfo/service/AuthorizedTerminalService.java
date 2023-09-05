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

package space.ao.services.account.authorizedterminalinfo.service;

import com.google.common.base.Stopwatch;
import io.quarkus.scheduler.Scheduled;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.jboss.logging.Logger;
import space.ao.services.account.authorizedterminalinfo.dto.AuthorizedTerminalInfo;
import space.ao.services.account.authorizedterminalinfo.entity.AuthorizedTerminalEntity;
import space.ao.services.account.authorizedterminalinfo.repository.AuthorizedTerminalRepository;
import space.ao.services.account.member.service.PlatformRegistryService;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.account.support.service.ServiceError;
import space.ao.services.account.support.service.ServiceOperationException;
import space.ao.services.config.ApplicationProperties;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import space.ao.services.push.services.RedisService;
import space.ao.services.support.StringUtils;
import space.ao.services.support.log.Logged;
import space.ao.services.support.platform.info.registry.ClientRegistryInfo;
import space.ao.services.support.platform.info.registry.RegistryTypeEnum;

@ApplicationScoped
public class AuthorizedTerminalService {
  private static final Logger LOG = Logger.getLogger("app.log");

  @Inject
  AuthorizedTerminalRepository authorizedTerminalRepository;

  @Inject
  UserInfoRepository userInfoRepository;
  @Inject
  ApplicationProperties properties;

  @Inject
  PlatformRegistryService platformRegistryService;

  /**
   * 插入终端授权信息
   * @author suqin
   * @date 2021-11-22 17:53:57
   * @param authorizedTerminalInfo 终端授权信息
   **/
  @Transactional
  @Logged
  public AuthorizedTerminalEntity insertAuthorizedTerminalInfo(@Valid @NotBlank String requestId,
      @Valid AuthorizedTerminalInfo authorizedTerminalInfo) {
    if(authorizedTerminalRepository.findByUseridAndUuid(
        Long.valueOf(authorizedTerminalInfo.userId()), authorizedTerminalInfo.uuid()) != null){
      return updateAuthorizedTerminalValidTime(requestId, authorizedTerminalInfo);
    }
    if(userInfoRepository.findByUserId(Long.valueOf(authorizedTerminalInfo.userId())).getClientUUID().equals(authorizedTerminalInfo.uuid())){
      throw new ServiceOperationException(ServiceError.CLIENT_HAS_REGISTERED);
    }

    var authorizedTerminalEntity = authorizedTerminalRepository.insert(authorizedTerminalInfo);
    platformRegistryService.registryClient(requestId, ClientRegistryInfo.of(
              authorizedTerminalInfo.uuid(), RegistryTypeEnum.CLIENT_AUTH.getName()), authorizedTerminalEntity.getAoid());


    return authorizedTerminalEntity;
  }

  /**
   * 更新终端授权信息
   * @author suqin
   * @date 2021-11-22 17:53:57
   * @param authorizedTerminalInfo 终端授权信息 TerminalMode 不为空字符串时， 更新型号TerminalMode、类型TerminalType、地址Address
   **/
  @Transactional
  @Logged
  public AuthorizedTerminalEntity updateAuthorizedTerminalValidTime(@Valid @NotBlank String requestId,
      @Valid AuthorizedTerminalInfo authorizedTerminalInfo) {
    var authorizedTerminalEntity = authorizedTerminalRepository.findByUseridAndUuid
        (Long.valueOf(authorizedTerminalInfo.userId()), authorizedTerminalInfo.uuid());
    if (authorizedTerminalEntity == null) {
      LOG.error("requestId: " + requestId + ": No authorized terminal info found");
      return null; // "No authorized terminal info found"
    }

    if(!StringUtils.isBlank(authorizedTerminalInfo.terminalMode())){
      authorizedTerminalEntity.setTerminalMode(authorizedTerminalInfo.terminalMode());
      authorizedTerminalEntity.setTerminalType(authorizedTerminalInfo.terminalType());
      if(!authorizedTerminalInfo.address().contains("内网IP")){
        authorizedTerminalEntity.setAddress(authorizedTerminalInfo.address());
      }
    }
    authorizedTerminalEntity.setLoginAt(OffsetDateTime.now());
    authorizedTerminalEntity.setExpireAt(OffsetDateTime.now().plusSeconds(authorizedTerminalInfo.expireAt()));
    authorizedTerminalEntity.persist();
    return authorizedTerminalEntity;
  }

  /**
   * 删除终端授权信息
   * @author suqin
   * @date 2021-11-22 17:53:57
   * @param authorizedTerminalInfo 终端授权信息
   **/

  @Transactional
  public void delAuthorizedTerminalInfo(@Valid @NotBlank String requestId,
      @Valid AuthorizedTerminalInfo authorizedTerminalInfo) {
    var authorizedTerminalEntity= authorizedTerminalRepository.findByUseridAndUuid(Long.valueOf(authorizedTerminalInfo.userId()), authorizedTerminalInfo.uuid());
    if(authorizedTerminalEntity == null){
      throw new ServiceOperationException(ServiceError.INVALID_AUTHORIZED_CLIENT);
    }
    platformRegistryService.platformRegistryClientReset(requestId, authorizedTerminalEntity.getAoid(),
        authorizedTerminalInfo.uuid());
    authorizedTerminalEntity.delete();
  }


  /**
   * 下线终端授权信息
   * @author suqin
   * @date 2021-11-22 17:53:57
   * @param userId clientUUID 终端授权信息
   **/

  @Transactional
  public AuthorizedTerminalEntity logoutAuthorizedTerminalInfo(@Valid @NotBlank String requestId,
      @Valid String userId, String clientUUID) {
    RedisService.setClientStatus(clientUUID+userId, OffsetDateTime.now().minusSeconds(Duration.parse(properties.pushTimeout()).getSeconds()));
    return updateAuthorizedTerminalValidTime(requestId, AuthorizedTerminalInfo.of(
        userId, clientUUID, "", -Duration.parse(properties.pushTimeout()).getSeconds()*2,"",""));
  }

  /**
   * 更新终端授权有效时间
   **/
  @Transactional
  @Logged
  public void updateAuthorizedTerminalValidTime(@Valid @NotBlank String requestId,
      @Valid String userId, String clientUUID) {
    var authorizedTerminalEntity = authorizedTerminalRepository.findByUseridAndUuid
        (Long.valueOf(userId), clientUUID);
    if (authorizedTerminalEntity == null) {
      LOG.error("requestId: " + requestId + ": No authorized terminal info found");
      return; // "No authorized terminal info found"
    }
    if(Objects.equals(authorizedTerminalEntity.getTerminalType(), "web")
        && authorizedTerminalEntity.getExpireAt().isBefore(OffsetDateTime.now().minusSeconds(Duration.parse(properties.pushTimeout()).getSeconds()))){
      return;
    }
    if(authorizedTerminalEntity.getExpireAt().isBefore(OffsetDateTime.now())
        && authorizedTerminalEntity.getExpireAt().isAfter(OffsetDateTime.now().minusSeconds(Duration.parse(properties.pushTimeout()).getSeconds()*2))){
      authorizedTerminalEntity.setExpireAt(OffsetDateTime.now().plusSeconds(Duration.parse(properties.pushTimeout()).getSeconds()));
    }
    authorizedTerminalEntity.persist();
  }


  @Scheduled(cron = "0 0 0 1 * ? *")
  @SuppressWarnings("unused")
  void cleanTerminal(){
    Stopwatch stopwatch = Stopwatch.createStarted();

    var authorizedTerminals = authorizedTerminalRepository.findAll().list();
    for (var authorizedTerminal: authorizedTerminals){
      if(authorizedTerminal.getExpireAt().isBefore(
          OffsetDateTime.now().minusSeconds(Duration.parse(properties.timeOfAllowLogin()).toSeconds()))){
        // 有效期三十天之前
        delAuthorizedTerminalInfo("", AuthorizedTerminalInfo.of(authorizedTerminal.getUserid().toString(),
            authorizedTerminal.getUuid(),"",0,"",""));
        LOG.info("regularly clean invalid authorizedTerminal - " + authorizedTerminal);
      }
    }
    LOG.info("regularly clean invalid authorizedTerminal completed - " + stopwatch.elapsed(TimeUnit.SECONDS));
  }
}
