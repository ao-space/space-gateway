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
package space.ao.services.account.deviceinfo.service;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import space.ao.services.account.authorizedterminalinfo.entity.AuthorizedTerminalResult;
import space.ao.services.account.deviceinfo.dto.DeviceInfoResult;
import space.ao.services.account.deviceinfo.dto.DeviceStorageInfo;
import space.ao.services.account.deviceinfo.dto.NetworkChannelInfo;
import space.ao.services.account.personalinfo.dto.AccountInfoResult;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.authorizedterminalinfo.repository.AuthorizedTerminalRepository;
import space.ao.services.account.member.service.MemberManageService;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.log.Logged;
import space.ao.services.support.platform.temp.TempInfoHandleService;
import space.ao.services.support.service.ServiceError;
import space.ao.services.support.service.ServiceOperationException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class DeviceInfoService {
  @Inject
  MemberManageService memberManageService;

  @Inject
  @RestClient
  DeviceStorageService deviceStorageService;

  @Inject
  UserInfoRepository userInfoRepository;
  @Inject
  AuthorizedTerminalRepository authorizedTerminalRepository;
  @Inject
  OperationUtils operationUtils;
  @Inject
  TempInfoHandleService tempInfoHandleService;
  @Logged
  public DeviceInfoResult getDeviceInfo(String requestId){
    ResponseBase<DeviceStorageInfo> rsp = deviceStorageService.getStorageInfo(requestId);
    var adminEntity = userInfoRepository.findByRole(UserEntity.Role.ADMINISTRATOR);
    return DeviceInfoResult.of(requestId, rsp.results().getTotal(), rsp.results().getUsed(), adminEntity.getPhoneModel());
  }
  
  public List<AccountInfoResult> getMemberList(String requestId){
    return memberManageService.getMemberList(requestId);
  }

  public List<AuthorizedTerminalResult> getAuthorizedTerminal(UserEntity userInfo) {
    List<AuthorizedTerminalResult> result = new ArrayList<>();
    for (var terminal: authorizedTerminalRepository.findByUserid(userInfo.getId())){
      if(terminal.getExpireAt().isAfter(OffsetDateTime.now())) {
        result.add(AuthorizedTerminalResult.of(terminal.getAoid(), terminal.getUuid(), terminal.getTerminalMode(),
                terminal.getTerminalType(), terminal.getLoginAt(), terminal.getAddress(), true));
      }
    }
    return result;
  }

  public AccountInfoResult getBinderInfo(String requestId, UserEntity userInfo) {
    return memberManageService.getMemberInfo(requestId, userInfo);
  }

  public NetworkChannelInfo getNetworkChannelInfo() {
    var internetAccess = operationUtils.getEnableInternetAccess();
    return NetworkChannelInfo.of(true, false, internetAccess);
  }

  @Transactional
  public NetworkChannelInfo setInternetAccess(String requestId, boolean enableInternetAccess) throws Exception {
    operationUtils.loadInternetServiceConfig(requestId);
    var internetAccess = operationUtils.getEnableInternetAccess();
    if (!Objects.equals(internetAccess, enableInternetAccess)) {
      throw new ServiceOperationException(ServiceError.INIT_UTIL_FAILED);
    }
    if (enableInternetAccess){
      tempInfoHandleService.handleTempInfo();
    }
    return NetworkChannelInfo.of(true, false, internetAccess);
  }
}
