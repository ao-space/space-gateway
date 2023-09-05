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

import jakarta.validation.constraints.NotNull;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import space.ao.services.account.member.AdminCallOnly;
import space.ao.services.account.member.dto.*;
import space.ao.services.account.member.service.DevOptionsService;
import space.ao.services.account.member.service.MemberManageService;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.account.security.utils.SecurityPasswordUtils;
import space.ao.services.account.support.service.MemberBasicAttribute;
import space.ao.services.account.support.service.ServiceDefaultVar;
import space.ao.services.account.support.service.ServiceError;
import space.ao.services.account.support.service.ServiceOperationException;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.FileUtils;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.log.Logged;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.member.entity.BoxInfoEntity;
import space.ao.services.account.member.respository.BoxInfoRepository;
import space.ao.services.account.personalinfo.entity.UserEntity;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Objects;

@Path("/v1/api")
@Tag(name = "Admin user service", description = "Provide admin user revoke/bind requests.")
public class AdminUserResource {
  @Inject
  ApplicationProperties properties;

  @Inject
  OperationUtils utils;
  @Inject
  SecurityPasswordUtils securityPasswordUtils;
  @Inject
  BoxInfoRepository boxInfoRepository;

  @Inject
  MemberManageService memberManageService;

  @Inject
  UserInfoRepository userInfoRepository;

  @Inject
  DevOptionsService devOptionsService;

  static final Logger LOG = Logger.getLogger("app.log");


  /**
   * 创建密码。
   * @param requestId 请求id
   * @param adminPasswdInfo 管理员密码信息
   * @return 管理员创建结果。
   * @since 0.3.0
   */
  @POST
  @Logged
  @Path("/admin/passwd/set")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  @Operation(description = "Tries to create administrator into database.")
  public ResponseBase<String> adminPasswdCreate(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                @Valid AdminPasswdInfo adminPasswdInfo) {
    var boxInfo = boxInfoRepository.findAll().firstResult();
    if(Objects.nonNull(boxInfo)){
      securityPasswordUtils.doModifyPasscode(requestId, adminPasswdInfo.getPassword());
    } else {
      return ResponseBase.forbidden(requestId, null);
    }
    return ResponseBase.okACC(requestId, null);
  }

  /**
   * 查询盒子信息和安全密码
   * @param requestId 请求id
   * @return 管理员创建结果。
   * @since 0.3.0
   */
  @GET
  @Logged
  @Path("/admin/passwd/get")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  @Operation(description = "Tries to create administrator into database.")
  public ResponseBase<BoxInfoEntity> adminPasswdGet(@Valid @NotBlank @HeaderParam("Request-Id") String requestId) {
    var boxInfo = boxInfoRepository.findAll().firstResult();
    return ResponseBase.okACC(requestId, boxInfo);
  }

  /**
   * 校验盒子信息和安全密码
   * @param requestId 请求 id
   * @return 管理员创建结果。
   * @since 0.3.0
   */
  @GET
  @Logged
  @Path("/admin/passwd/check")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Transactional
  @Operation(description = "Tries to create administrator into database.")
  public ResponseBase<Boolean> adminPasswdCheck(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                      @Valid @NotBlank @FormParam("passcode") String passCode) {
    if(securityPasswordUtils.doVerifyPasscode(requestId, passCode)){
      return ResponseBase.okACC(requestId, true);
    }else{
      return ResponseBase.forbidden("passcode error", requestId);
    }
  }

  /**
   * 初始话成功标志
   * @param requestId 请求id
   * @param flag 初始话成功标志
   * @return 管理员创建结果。
   * @since 0.8.0
   */
  @POST
  @Logged
  @Path("/admin/inital/status")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Transactional
  @Operation(description = "admin initial status")
  public ResponseBase<Boolean> adminStatus(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                @Valid @NotNull @FormParam("flag") Boolean flag,
                                                @Valid @NotBlank @FormParam("password") String password)  {

    if(!securityPasswordUtils.doVerifyPasscode(requestId, password)){
      return ResponseBase.forbidden("passcode error", requestId);
    }

    Map<String, String> adminInfo = utils.readFromFile(new File(properties.accountDataLocation() + ServiceDefaultVar.DEFAULT_DATA_FILE));

    if(Boolean.FALSE.equals(flag)){ // 初始化成功标志
      boolean resetResult = false;
      if(adminInfo.get(MemberBasicAttribute.STATUS.toString()).equals("1")){ //盒子第一次初始化失败了
        resetResult = securityPasswordUtils.doResetPasscode(requestId); //重置安全密码
      }
      return ResponseBase.forbidden("initial failed, reset passcode: " + resetResult, requestId);
    } else {
      var userEntityAdmin = userInfoRepository.findByRole(UserEntity.Role.ADMINISTRATOR);

      if(adminInfo.get(MemberBasicAttribute.STATUS.toString()).equals("1") && userEntityAdmin == null) {
        var userEntity = new UserEntity(UserEntity.Role.ADMINISTRATOR,
                "ao_" + adminInfo.get(MemberBasicAttribute.USERDOMAIN.toString()).split("\\.")[0],
                adminInfo.get(MemberBasicAttribute.USERDOMAIN.toString()), Const.Admin.ADMIN_AOID);

        //创建管理员头像路径
        var adminImageFile = new File(properties.accountImageLocation() + Const.Admin.ADMIN_AOID);
        if(adminImageFile.exists() || adminImageFile.mkdirs()) {
          var imagePath = ServiceDefaultVar.DEFAULT_IMAGE_PATH + ServiceDefaultVar.DEFAULT_AVATAR_FILE.toString();
          var defaultImage = new File(adminImageFile, ServiceDefaultVar.DEFAULT_AVATAR_FILE.toString());
          FileUtils.saveFileToLocal(imagePath, defaultImage);
          try{
            userEntity.setImageMd5(DigestUtils.md5Hex(new FileInputStream(defaultImage)));
          } catch (Exception e) {
            LOG.error("get admin default image md5 failed");
            userEntity.setImageMd5(null);
          }
          userEntity.setImage(defaultImage.getPath());
        } else {
          throw new ServiceOperationException(ServiceError.PROFILE_PHOTO_INIT_FAILED);
        }
        userInfoRepository.insertAdminUser(userEntity);
      }
      if(adminInfo.containsKey(MemberBasicAttribute.APPLYEMAIL.toString())) {
        userInfoRepository.update("set clientUUID=?1, phoneModel=?2, authKey=?3, userDomain=?4, applyEmail=?5 where id=1",
                adminInfo.get(MemberBasicAttribute.CIENTUUID.toString()),
                adminInfo.get(MemberBasicAttribute.PHONEMODEL.toString()),
                adminInfo.get(MemberBasicAttribute.AUTHKEY.toString()),
                adminInfo.get(MemberBasicAttribute.USERDOMAIN.toString()),
                adminInfo.get(MemberBasicAttribute.APPLYEMAIL.toString()));
      }else{
        userInfoRepository.update("set clientUUID=?1, phoneModel=?2, authKey=?3, userDomain=?4 where id=1",
                adminInfo.get(MemberBasicAttribute.CIENTUUID.toString()),
                adminInfo.get(MemberBasicAttribute.PHONEMODEL.toString()),
                adminInfo.get(MemberBasicAttribute.AUTHKEY.toString()),
                adminInfo.get(MemberBasicAttribute.USERDOMAIN.toString()));
      }

      adminInfo.put(MemberBasicAttribute.STATUS.toString(), "0");
      utils.writeToFile(new File(properties.accountDataLocation() + ServiceDefaultVar.DEFAULT_DATA_FILE), adminInfo);
      return ResponseBase.okACC(requestId, true);
    }
  }

  /**
   * 撤回对于指定用户的客户端授权信息。该接口支持对任意已存在的用户进行操作（包括管理员）。
   *
   * @param requestId 请求 id
   * @param userId userId 指定的用户 id
   * @return 请求响应，ACC-200：请求成功，ACC-404：用户不存在，ACC-500：内部状态错误（可重试）。
   * @since 0.4.0
   */
  @Logged
  @POST
  @Path("/user/client/revoke")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Operation(description = "撤回对于指定用户的客户端授权信息。该接口支持对任意已存在的用户进行操作（包括管理员）。")
  @Transactional
  public ResponseBase<PasswdTryInfo> revokeUserClientInfo(
          @Valid @NotBlank @Schema(description = "请求 id") @HeaderParam("Request-Id") String requestId,
          @Valid @NotBlank @Schema(description = "指定撤回的用户 id") @FormParam ("userid") String userId,
          @Valid @Schema(description = "passcode") @FormParam ("passcode") String passcode,
          @Valid @Schema(description = "clientUUID") @FormParam ("clientUUID") String clientUUID) {
    UserEntity userInfo = memberManageService.findByUserId(userId);
    if (userInfo == null) {
      return ResponseBase.of(ServiceDefaultVar.ACCOUNT_USER_NOT_FOUND.toString(), ServiceError.USER_NOT_FOUND.getMessage(), requestId, null);
    }
    return memberManageService.revokeUserClientInfo(userId, requestId, passcode, clientUUID);
  }

  @GET
  @Path("/admin/dev-options/switch")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(description = "获得当前开发者选项开关状态")
  @AdminCallOnly
  public ResponseBase<DevOptionsSwitch> getDevOptionsSwitch(
          @NotBlank @Schema(description = "请求 id") @HeaderParam("Request-Id") String requestId,
          @NotBlank @Schema(description = "用户 id（客户端无需提供）") @QueryParam("userId") String userId) {
    return ResponseBase.okACC(requestId, DevOptionsSwitch.of(devOptionsService.getPermissionStatus()));
  }

  @POST
  @Path("/admin/dev-options/switch")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "修改当前开发者选项开关状态")
  @AdminCallOnly
  public ResponseBase<Boolean> updateDevOptionsSwitch(
          @NotBlank @Schema(description = "请求 id") @HeaderParam("Request-Id") String requestId,
          @NotBlank @Schema(description = "用户 id（客户端无需提供）") @QueryParam("userId") String userId,
          @Valid @Schema(description = "修改开关信息") DevOptionsSwitch devOptionsSwitch) {
    devOptionsService.setPermissionStatus(devOptionsSwitch.status);
    return ResponseBase.okACC(requestId, true);
  }

}
