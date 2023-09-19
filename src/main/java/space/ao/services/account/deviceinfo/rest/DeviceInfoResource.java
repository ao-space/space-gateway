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

package space.ao.services.account.deviceinfo.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import space.ao.services.account.deviceinfo.dto.*;
import space.ao.services.account.deviceinfo.service.DeviceStorageService;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.deviceinfo.service.DeviceInfoService;
import space.ao.services.account.member.service.MemberManageService;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.account.support.service.ServiceError;
import space.ao.services.account.support.service.ServiceOperationException;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.log.Logged;
import space.ao.services.support.model.AccessToken;

import java.util.Objects;

@Path("/v1/api/device")
@Tag(name = "Device storage Service", description = "Provides overall device requests.")
public class DeviceInfoResource{
  @Inject
  MemberManageService memberManageService;

  @Inject
  @RestClient
  DeviceStorageService deviceService;

  @Inject
  ApplicationProperties properties;

  @Inject
  UserInfoRepository userInfoRepository;
  @Inject
  DeviceInfoService deviceInfoService;

  /**
   * 查询盒子容量信息接口
   * @author suqin
   * @date 2021-10-08 21:39:57
   * @param userId userId
   * @param requestId requestId
   **/
  @GET
  @Logged
  @Path("/storage/info")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "Tries to get storage information and terminal type.")
  public DeviceInfoResult deviceInfoResult(@Valid @NotBlank @QueryParam("userId") String userId,
                                           @Valid @NotBlank @HeaderParam("Request-Id") String requestId) {
    var userEntity = memberManageService.findByUserId(userId);
    if(userEntity == null) {throw new ServiceOperationException(ServiceError.USER_NOT_FOUND);}
    ResponseBase<DeviceStorageInfo> rsp = deviceService.getStorageInfo(requestId);
    var adminEntity = userInfoRepository.findByRole(UserEntity.Role.ADMINISTRATOR);
    return DeviceInfoResult.of(requestId, rsp.results().getTotal(),rsp.results().getUsed(), adminEntity.getPhoneModel());
  }

  /**
   * 查询硬件信息接口 (since 1.0.7)
   * @param userId userId
   * @param requestId requestId
   * @return 设备硬件信息
   */
  @GET
  @Logged
  @Path("/hardware/info")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "获取设备硬件信息 (since 1.0.7)")
  public ResponseBase<HardwareInfoRsp> hardwareInfo(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                    @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam("userId") String userId,
                                                    @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam(AccessToken.AK_CLIENT_UUID)  String clientUUid) {
    var hardwareInfoRsp = HardwareInfoRsp.of(properties.boxBtid());
    return ResponseBase.okACC(requestId, hardwareInfoRsp);
  }

  @POST
  @Logged
  @Path("/network/channel/wan")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "网络通道信息")
  public ResponseBase<NetworkChannelInfo> setNetworkChannel(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                            @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam("userId") String userId,
                                                            @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam(AccessToken.AK_CLIENT_UUID)  String clientUUid,
                                                            WanChannelSettingInfo wanChannelSettingInfo) throws Exception {
    var userEntity = memberManageService.findByUserId(userId);
    if (userEntity.getRole() == UserEntity.Role.ADMINISTRATOR && Objects.equals(clientUUid, userEntity.getClientUUID())) {
      var networkChannelInfo = deviceInfoService.setInternetAccess(requestId, wanChannelSettingInfo.wan());
      networkChannelInfo.setUserDomain(userEntity.getUserDomain());
      return ResponseBase.okACC(requestId, networkChannelInfo);
    } else {
      throw new ServiceOperationException(ServiceError.NO_MODIFY_RIGHTS);
    }
  }

  /**
   * 盒子&用户 设置信息
   */
  @GET
  @Logged
  @Path("/setting/info")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "盒子&用户 设置信息")
  public ResponseBase<SettingInfo> settingInfo(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                               @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam("userId") String userId,
                                               @Schema(description = "前端调用不需要加") @Valid @NotBlank @QueryParam(AccessToken.AK_CLIENT_UUID)  String clientUUid) {
    UserEntity userinfo = memberManageService.findByUserId(userId);
    if (userinfo == null) {
      throw new ServiceOperationException(ServiceError.INVALID_USER_ID);
    }
    SettingInfo settingInfo = new SettingInfo();
    settingInfo.setNetworkChannelInfo(deviceInfoService.getNetworkChannelInfo());
    settingInfo.setDeviceInfo(deviceInfoService.getDeviceInfo(userId));
    settingInfo.setAccountInfoResults(deviceInfoService.getMemberList(requestId));
    settingInfo.setAuthorizedTerminalResults(deviceInfoService.getAuthorizedTerminal(userinfo));
    settingInfo.setBinderInfoResult(deviceInfoService.getBinderInfo(requestId, userinfo));
    return ResponseBase.okACC(requestId, settingInfo);
  }
}
