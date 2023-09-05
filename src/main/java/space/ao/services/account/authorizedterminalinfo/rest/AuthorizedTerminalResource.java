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

package space.ao.services.account.authorizedterminalinfo.rest;

import java.util.ArrayList;
import java.util.Objects;

import io.smallrye.openapi.runtime.util.StringUtil;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import space.ao.services.account.authorizedterminalinfo.service.AuthorizedTerminalService;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.authorizedterminalinfo.entity.AuthorizedTerminalResult;
import space.ao.services.account.authorizedterminalinfo.repository.AuthorizedTerminalRepository;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.push.dto.Message;
import space.ao.services.push.dto.NotificationEnum;
import space.ao.services.push.services.RedisService;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.log.Logged;
import space.ao.services.account.support.service.ServiceError;
import space.ao.services.account.support.service.ServiceOperationException;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.time.OffsetDateTime;
import java.util.List;

@Path("/v1/api/terminal")
@Tag(name = "Terminal authorization Service", description = "Provides authorized terminal services.")
public class AuthorizedTerminalResource {
  @Inject
  AuthorizedTerminalRepository authorizedTerminalRepository;
  @Inject
  AuthorizedTerminalService authorizedTerminalService;

  @Inject
  UserInfoRepository userInfoRepository;

  @Inject
  RedisService redisService;

  @Inject
  OperationUtils utils;

  /**
   * 查询被授权终端相关信息
   * @author suqin
   * @date 2021-11-22 17:53:57
   * @param userId userId
   * @param requestId requestId
   **/
  @GET
  @Logged
  @Path("/info")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "Tries to get authorized terminal information by userId.")
  public ResponseBase<AuthorizedTerminalResult> get(@Valid @NotBlank @QueryParam("userId") String userId,
                                                    @Valid @NotBlank @QueryParam("aoid") String aoid,
                                                    @Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                    @Valid @QueryParam("clientUUID") String clientUUID) {
    var terminal  = authorizedTerminalRepository.findByAoidAndUuid(aoid, clientUUID);
    if(Objects.isNull(terminal)){
      return ResponseBase.of("ACC-404", "no authorized terminal corresponding to client uuid",requestId,null);
    }
    var authorizedTerminalResult = AuthorizedTerminalResult.of(terminal.getAoid(), terminal.getUuid(),
        terminal.getTerminalMode(), terminal.getTerminalMode(), terminal.getLoginAt(), terminal.getAddress(), null);
    return ResponseBase.okACC(requestId, authorizedTerminalResult);


  }

  /**
   * 查询在线状态的被授权终端相关信息
   * @author suqin
   * @date 2021-11-22 17:53:57
   * @param userId userId
   * @param requestId requestId
   **/
  @GET
  @Logged
  @Path("/all/info")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "Tries to get authorized terminal information by userId.")
  public ResponseBase<List<AuthorizedTerminalResult>> authorizedTerminalInfoByUserid(@Valid @NotBlank @QueryParam("userId") String userId,
                                                                             @Valid @QueryParam("aoid") String aoid,
                                                                             @Valid @NotBlank @HeaderParam("Request-Id") String requestId) {

    List<AuthorizedTerminalResult> result = new ArrayList<>();
    var terminalList = StringUtil.isNotEmpty(aoid)?
            authorizedTerminalRepository.findByAoid(aoid):authorizedTerminalRepository.findByUserid(Long.valueOf(userId));
    for(var terminal:terminalList) {
      var online = terminal.getExpireAt().isAfter(OffsetDateTime.now());
      if(online) {
        result.add(AuthorizedTerminalResult.of(terminal.getAoid(), terminal.getUuid(),
                terminal.getTerminalMode(), terminal.getTerminalType(), terminal.getLoginAt(),
                terminal.getAddress(), true));
      }
    }
    return ResponseBase.okACC(requestId,  result);
  }



  /**
   * 删除终端授权信息
   * @author suqin
   * @date 2021-11-22 17:53:57
   **/
  @DELETE
  @Logged
  @Path("/info/delete")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  @Operation(description = "Tries to get authorized terminal information by userId.")
  public ResponseBase<AuthorizedTerminalResult> delAuthorizedTerminalInfo(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
      @Valid @NotBlank @QueryParam("userId") String userId,
      @Valid @NotBlank @QueryParam("aoid") String aoid,
      @Valid @NotBlank @QueryParam("clientUUID") String clientUUID) {
    return authorizedTerminalLogout(requestId, userId, "", aoid, clientUUID);
  }

  /**
   * 兼容 IOS 代码生成插件
   */
  @POST
  @Logged
  @Path("/info/delete")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  @Operation(description = "Tries to get authorized terminal information by userId.")
  public ResponseBase<AuthorizedTerminalResult> delAuthorizedTerminalInfoIOS(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
      @Valid @NotBlank @QueryParam("userId") String userId,
      @Valid @NotBlank @QueryParam("aoid") String aoid,
      @Valid @NotBlank @QueryParam("clientUUID") String clientUUID) {
    return delAuthorizedTerminalInfo(requestId, userId, aoid, clientUUID);
  }


  /**
   * 终端下线
   */
  @POST
  @Logged
  @Path("/logout")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  @Operation(description = "Tries to get authorized terminal information by userId.")
  public ResponseBase<AuthorizedTerminalResult> authorizedTerminalLogout(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
      @Valid @NotBlank @QueryParam("userId") String userId, @QueryParam("AccessToken-clientUUID") String accessTokenClientUUID,
      @Valid @NotBlank @QueryParam("aoid") String aoid,
      @Valid @NotBlank @QueryParam("clientUUID") String clientUUID) {
    var authorizedTerminalEntity= authorizedTerminalRepository.findByAoidAndUuid(aoid, clientUUID);
    if(authorizedTerminalEntity == null){
      throw new ServiceOperationException(ServiceError.INVALID_AUTHORIZED_CLIENT);
    }
    var userInfo = userInfoRepository.findByUserId(Long.valueOf(userId));
    if(userInfo == null || userInfo.getClientUUID() == null || Objects.equals(
        userInfo.getClientUUID(), clientUUID)){
      throw new ServiceOperationException(ServiceError.INVALID_AUTHORIZED_CLIENT);
    }

    if(userId.equals("1") || userId.equals(authorizedTerminalEntity.getUserid().toString())) {

      var terminalEntity  = authorizedTerminalService.logoutAuthorizedTerminalInfo(requestId, userId, clientUUID);

      var authorizedTerminalResult = AuthorizedTerminalResult.of(
          terminalEntity.getAoid(), terminalEntity.getUuid(),
          terminalEntity.getTerminalMode(), terminalEntity.getTerminalType(),
          terminalEntity.getLoginAt(),
          terminalEntity.getAddress(), false);

      if(!Objects.equals(clientUUID, accessTokenClientUUID)){
        redisService.pushMessage(Message.of(userId, authorizedTerminalEntity.getUuid(),
            NotificationEnum.LOGOUT.getType(), requestId,
            utils.objectToJson(authorizedTerminalResult)));
      }

      return ResponseBase.okACC(requestId, authorizedTerminalResult);
    }
    throw new ServiceOperationException(ServiceError.NO_MODIFY_RIGHTS);
  }

}
