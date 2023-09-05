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

package space.ao.services.account.member.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import space.ao.services.account.deviceinfo.dto.UserStorageInfo;
import space.ao.services.account.deviceinfo.service.DeviceStorageService;
import space.ao.services.account.member.dto.MemberCreateResult;
import space.ao.services.account.member.service.MemberManageService;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.support.service.ServiceError;
import space.ao.services.account.support.service.ServiceOperationException;
import space.ao.services.support.log.Logged;


@Path("/v1/api")
@Tag(name = "Member manage service", description = "Provides member create/delete requests.")
public class MemberManageResource {

  @Inject
  MemberManageService memberManageService;

  @Inject
  @RestClient
  DeviceStorageService deviceStorageService;

  /**
   * 查询账户接口.
   * @author suqin
   * @date 2021-10-12 21:40:10
   * @param clientUUID 客户端id
   **/
  @GET
  @Logged
  @Path("/user")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(description = "Tries to get member info from database.")
  public ResponseBase<MemberCreateResult> userQueryInfo(@Valid @QueryParam("client_uuid")  String clientUUID,
                                                        @Valid @NotBlank @HeaderParam("Request-Id") String requestId){

    UserEntity userInfo = memberManageService.findByClientUUID(clientUUID);
    if(userInfo == null){
      return ResponseBase.of("ACC-403", ServiceError.USER_NOT_FOUND.getMessage(), requestId, null);
    }
    return ResponseBase.of("ACC-200","OK", requestId,
            MemberCreateResult.of(userInfo.getAuthKey(),String.valueOf(userInfo.getId()), clientUUID, userInfo.getAoId(), userInfo.getUserDomain()));
  }

  /**
   * 根据userId查询clientUUID.
   * @author suqin
   * @date 2021-10-12 21:40:10
   * @param userId userId
   **/
  @GET
  @Logged
  @Path("/user/clientuuid")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(description = "Tries to get member info from database.")
  public ResponseBase<MemberCreateResult> userQueryByUserId(@Valid @NotBlank @QueryParam("userid")  String userId,
                                                            @Valid @NotBlank @HeaderParam("Request-Id") String requestId){
    UserEntity userInfo = memberManageService.findByUserId(userId);
    if(userInfo == null){
      return ResponseBase.of("ACC-403", ServiceError.USER_NOT_FOUND.getMessage(), requestId, null);
    }
    return ResponseBase.of("ACC-200","OK", requestId,
            MemberCreateResult.of(userInfo.getAuthKey(),String.valueOf(userInfo.getId()),
                              userInfo.getClientUUID(),userInfo.getAoId(), userInfo.getUserDomain()));
  }



  /**
   * 查询各用户占用空间信息
   * @author suqin
   * @date 2021-10-08 21:39:57
   * @param userId userId
   * @param requestId 请求id
   * @param aoid aoId
   **/
  @GET
  @Logged
  @Path("/member/used/storage")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "获取各个用户所占用空间")
  public ResponseBase<UserStorageInfo> memberUsedStorageResult(@Valid @NotBlank @QueryParam("userId") String userId,
                                                               @Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                               @Valid @NotBlank @QueryParam("aoid") String aoid){
    var user = memberManageService.findByUserId(userId);
    var userEntity = memberManageService.findByAoId(aoid);
    if(userEntity == null || user == null){
      throw new ServiceOperationException(ServiceError.USER_NOT_FOUND);
    }else if(!userId.equals("1") && !userId.equals(String.valueOf(userEntity.getId()))){
      throw new ServiceOperationException(ServiceError.NO_MODIFY_RIGHTS);
    }

    // 对于用户总的存储空间：如果邀请时设置的空间配额，则为该配额；如果没有设置，则为设备的总存储大小。
    var result = memberManageService.fileStorageInfo(requestId, userId, String.valueOf(userEntity.getId()));
    if (user.getSpaceLimit() != null) {
      result.results().setTotalStorage(String.valueOf(user.getSpaceLimit()));
    } else {
      var resp = deviceStorageService.getStorageInfo(requestId);
      result.results().setTotalStorage(resp.results().getTotal());
    }
    return result;
  }

}
