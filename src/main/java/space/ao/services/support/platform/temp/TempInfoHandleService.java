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

package space.ao.services.support.platform.temp;

import com.google.common.base.Stopwatch;
import io.quarkus.panache.common.Sort;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import space.ao.services.account.member.dto.Const;
import space.ao.services.account.member.service.MemberManageService;
import space.ao.services.account.member.service.PlatformRegistryService;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.log.Logged;
import space.ao.services.support.platform.PlatformUtils;
import space.ao.services.support.platform.info.registry.ClientRegistryInfo;
import space.ao.services.support.platform.info.registry.UserRegistryInfo;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author zhichuang
 * @date 2023/3/30 0030
 **/
@ApplicationScoped
public class TempInfoHandleService {

  @Inject
  TempRegistryInfoRepository tempRegistryInfoRepository;
  static final Logger LOG = Logger.getLogger("app.log");
  @Inject
  PlatformRegistryService platformRegistryService;
  @Inject
  OperationUtils operationUtils;
  @Inject
  PlatformUtils platformUtils;
  @Inject
  MemberManageService memberManageService;
  @Inject
  UserInfoRepository userInfoRepository;
  @Scheduled(every = "2m")
  @SuppressWarnings("unused") // Executing a Scheduled Task
  void handle() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    if(Boolean.TRUE.equals(operationUtils.getEnableInternetAccess()) && platformUtils.isRegistryPlatformAvailable(stopwatch.toString())) {
      handleTempInfo();
    }
    LOG.info("regularly handle temp registry info completed - " + stopwatch.elapsed(TimeUnit.SECONDS));
  }


  public void handleTempInfo(){
    var tempInfos = tempRegistryInfoRepository.findAll(Sort.by("createAt")).list();
    for(var tempInfo : tempInfos) {
      switch (RequestTypeEnum.fromValue(tempInfo.getType())) {
        case REGISTRY_AUTH_CLIENT -> {
          var clientRegistryInfo = operationUtils.jsonToObject(tempInfo.getTempInfo(), ClientRegistryInfo.class);
          registryClient(tempInfo, clientRegistryInfo);
        }
        case REGISTRY_USER -> {
          var userRegistryInfo = operationUtils.jsonToObject(tempInfo.getTempInfo(), UserRegistryInfo.class);
          registryUser(tempInfo, userRegistryInfo);
        }
        case RESET_AUTH_CLIENT -> resetAuthClient(tempInfo);
        default -> {
        }
      }
    }
  }

  @Logged
  @Transactional
  public void registryClient(TempRegistryInfoEntity tempInfo, ClientRegistryInfo clientRegistryInfo){
    platformRegistryService.registryClient(tempInfo.getRequestId(), clientRegistryInfo, "aoid-" + tempInfo.getUserId());
    tempRegistryInfoRepository.delete(tempInfo.getRequestId());
  }

  @Logged
  @Transactional
  public void registryUser(TempRegistryInfoEntity tempInfo, UserRegistryInfo userRegistryInfo){
    var userRegistryResult = platformRegistryService.registryUser(tempInfo.getRequestId(), userRegistryInfo, true);
    if (Objects.equals(tempInfo.getUserId(), Long.valueOf(Const.Admin.ADMIN_ID))){
      memberManageService.writeUserDomainToAdminFile(userRegistryResult.userDomain());
    }
    userInfoRepository.updateUserDomainByUserid(userRegistryResult.userDomain(), tempInfo.getUserId());
    tempRegistryInfoRepository.delete(tempInfo.getRequestId());
  }

  @Logged
  @Transactional
  public void resetAuthClient(TempRegistryInfoEntity tempInfo){
    platformRegistryService.platformRegistryClientReset(tempInfo.getRequestId(), "aoid-" + tempInfo.getUserId(), tempInfo.getClientUUID());
    tempRegistryInfoRepository.delete(tempInfo.getRequestId());
  }

}
