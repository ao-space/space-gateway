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

package space.ao.services.account.personalinfo.rest;

import static space.ao.services.account.support.service.ServiceDefaultVar.*;

import java.util.*;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import space.ao.services.account.member.dto.MemberNameUpdateInfo;
import space.ao.services.account.personalinfo.service.PersonalInfoService;
import space.ao.services.gateway.auth.member.client.ResponseCodeConstant;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.member.service.MemberManageService;
import space.ao.services.account.personalinfo.dto.MultiPersonalInfo;
import space.ao.services.account.personalinfo.dto.PersonalCallResult;
import space.ao.services.account.personalinfo.dto.PersonalInfo;
import space.ao.services.account.personalinfo.dto.PersonalInfoResult;
import space.ao.services.account.personalinfo.dto.AccountInfoResult;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.account.support.response.ResponseBaseEnum;
import space.ao.services.support.FileUtils;
import space.ao.services.support.StringUtils;
import space.ao.services.support.log.Logged;
import org.eclipse.microprofile.openapi.annotations.Operation;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.ResourceUtils;

import java.io.*;

import space.ao.services.account.support.service.ServiceOperationException;
import space.ao.services.account.support.service.ServiceError;
import space.ao.services.account.support.service.ServiceDefaultVar;

@Path("/v1/api")
@Tag(name = "Account service Personal Info Resource", description = "Provides account info requests.")
public class PersonalInfoResource {
  @Inject
  ApplicationProperties properties;

  @Inject
  OperationUtils utils;

  @Inject
  MemberManageService memberManageService;

  @Inject
  PersonalInfoService personalInfoService;

  private static final Logger LOG = Logger.getLogger("personalInfo.log");

  /**
   * 查询用户个人信息
   * @author suqin
   * @param userId userId
   * @param requestId 请求id
   * @date 2021-10-08 21:39:57
   **/
  @GET
  @Logged
  @Path("/personal/info")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "Tries to get personal information.")
  public ResponseBase<ArrayList<AccountInfoResult>> messageInfoResult(@Valid @NotBlank @QueryParam("userId") String userId,
                                                                      @Valid @NotBlank @HeaderParam("Request-Id") String requestId) {
    UserEntity userinfo = memberManageService.findByUserId(userId);
    if (userinfo == null) {
      throw new ServiceOperationException(ServiceError.INVALID_USER_ID);
    } else {
      var accountInfoResult = memberManageService.getMemberInfo(requestId, userinfo);
      ArrayList<AccountInfoResult> personalList = new ArrayList<>();
      personalList.add(accountInfoResult);
      return ResponseBase.of(ResponseCodeConstant.ACC_200, "get personalinfo of "+userinfo.getId(), requestId, personalList);
    }
  }

  /**
   * 查询所有用户信息
   * @author suqin
   * @param userId userId
   * @param requestId requestId
   * @date 2021-10-08 21:39:57
   **/
  @GET
  @Logged
  @Path("/member/list")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "Tries to all user information.")
  public ResponseBase<List<AccountInfoResult>> accountInfoResult(@Valid @NotBlank @QueryParam("userId") String userId,
                                                                      @Valid @NotBlank @HeaderParam("Request-Id") String requestId) {
    UserEntity userInfo = memberManageService.findByUserId(userId);
    if (userInfo == null) {
      throw new ServiceOperationException(ServiceError.INVALID_USER_ID);
    }
    List<AccountInfoResult> personalList = memberManageService.getMemberList(requestId);
    return ResponseBase.of(ResponseCodeConstant.ACC_200, "get all user info", requestId, personalList);
  }

  /**
   * 更新个人用户信息
   * @author suqin
   * @param userId userId
   * @param requestId requestId
   * @param personalInfo 用户个人信息
   * @date 2021-10-08 21:39:57
   **/
  @Logged
  @POST
  @Transactional
  @Path("/personal/info/update")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "Tries to update personal name and sign information.")
  public ResponseBase<ArrayList<AccountInfoResult>> updatePersonalInfo(@Valid @NotBlank @QueryParam("userId") String userId,
                                                                       @Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                                       @Valid @Schema(description = "用户信息") PersonalInfo personalInfo) {
    if (!StringUtils.isBlank(personalInfo.getUserDomain())) {
      return ResponseBase.of("ACC-4022", "domain name cannot be modified", requestId,
              null);
    }
    var userEntity = memberManageService.findByUserId(userId);

    if (userEntity == null) {
      throw new ServiceOperationException(ServiceError.USER_NOT_FOUND);
    }

    if (personalInfo.getPersonalName()!=null) {
      if(!personalInfo.getPersonalName().matches(ServiceDefaultVar.NAME_REGULAR_EXPRESS.toString())){
        throw new ServiceOperationException(ServiceError.WRONG_NAME_FORM);
      }
      if(personalInfoService.isPersonalNameUsed(personalInfo.getPersonalName(), userId)){
        return ResponseBaseEnum.PERSONAL_NAME_IS_USED.getResponseBase(requestId);
      }
    }

    personalInfoService.updatePersonalInfo(userEntity, personalInfo);

    ArrayList<AccountInfoResult> personalList = new ArrayList<>();
    personalList.add(AccountInfoResult.of(userEntity.getRole().name(), userEntity.getPersonalName(), userEntity.getPersonalSign(),
            userEntity.getCreateAt(), userEntity.getAoId(), userEntity.getClientUUID(), userEntity.getPhoneModel(),
            userEntity.getUserDomain(), userEntity.getImageMd5(), null, null, null));
    return ResponseBase.of(ResponseCodeConstant.ACC_201, "update user info success",
            requestId, personalList);
  }

  /**
   * 管理员更新成员用户名称信息
   * @author suqin
   * @param userId userId
   * @param requestId requestId
   * @param memberNameUpdateInfo 成员用户名称信息
   * @date 2021-10-08 21:39:57
   **/
  @Logged
  @POST
  @Transactional
  @Path("/member/name/update")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "Tries to update member name information, only admin and yourself has right.")
  public ResponseBase<MemberNameUpdateInfo> memberNameUpdateResult(@Valid @NotBlank @QueryParam("userId") String userId,
                                                                   @Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                                   @Valid @Schema(description = "修改者信息和被修改这信息")MemberNameUpdateInfo memberNameUpdateInfo){
    if(personalInfoService.isPersonalNameUsed(memberNameUpdateInfo.nickName(),userId)){
      return ResponseBaseEnum.PERSONAL_NAME_IS_USED.getResponseBase(requestId);
    }
    UserEntity userInfo = memberManageService.findByUserId(userId);
    if(userId.equals("1") || userInfo.getAoId().equals(memberNameUpdateInfo.aoId())){
      UserEntity userModify = memberManageService.findByAoId(memberNameUpdateInfo.aoId());
      userModify.setPersonalName(memberNameUpdateInfo.nickName());
      return ResponseBase.of(ResponseCodeConstant.ACC_201, "Modify nickname success",
              requestId, memberNameUpdateInfo);
    } else{
      throw new ServiceOperationException(ServiceError.NO_MODIFY_RIGHTS);
    }
  }

  /**
   * 更新个人头像
   * @author suqin
   * @param userId userId
   * @param requestId requestId
   * @param personalImage 用户头像
   * @date 2021-10-08 21:39:57
   **/
  @POST
  @Transactional
  @Path("/personal/image/update")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public PersonalCallResult updatePersonalImage(@Valid @NotBlank @QueryParam("userId") String userId,
                                                @Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                @Valid MultiPersonalInfo personalImage) {
    var userEntity = memberManageService.findByUserId(userId);
    String imageFolder = properties.accountImageLocation()+"aoid-"+userId+"/";
    String imagePath = imageFolder + utils.jsonToObject(personalImage.param, HashMap.class).get("filename");
    FileUtils.createFileIfNotExists(imagePath);
    try (FileOutputStream stream = new FileOutputStream(ResourceUtils.getFile(imagePath))) {
      byte[] b = personalImage.inputStream.readAllBytes();
      stream.write(b, 0, b.length);
      userEntity.setImageMd5(DigestUtils.md5Hex(b));
      userEntity.setImage(imagePath);
    } catch (Exception ie) {
      LOG.errorv("requestId: {0}, upload file failed: {1}",requestId, ie);
      throw new ServiceOperationException(ServiceError.UPLOAD_FILE_FAILED);
    }
    return PersonalCallResult.of(201, memberManageService.findByUserId(userId).getClientUUID());
  }

  /**
   * 成员获取他人头像
   * @author suqin
   * @param userId userId
   * @param requestId requestId
   * @param aoid aoId
   * @date 2021-10-08 21:39:57
   **/
  @GET
  @Path("/personal/image")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  @Operation(description = "Tries to download personal image, you also can get the image of other member by clientUUID")
  public Response personalImageResult(@Valid @NotBlank @QueryParam("userId") String userId,
                                      @Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                      @Valid @QueryParam("aoid") String aoid)  {
    var userEntity = new UserEntity();
    if (!StringUtils.isBlank(aoid)) {
      userEntity = memberManageService.findByAoId(aoid);
    } else {
      userEntity = memberManageService.findByUserId(userId);
    }

    if(userEntity == null) {
      throw new ServiceOperationException(ServiceError.USER_NOT_FOUND);
    }
    String imagePath = userEntity.getImage();
    File avatarFile;

    try {
      if (StringUtils.isBlank(imagePath)||userEntity.getImage().contains("s.png")) {
        imagePath = DEFAULT_IMAGE_PATH + DEFAULT_AVATAR_FILE.toString();
        avatarFile = FileUtils.readDefaultFile(imagePath);
      } else {
        avatarFile = ResourceUtils.getFile(imagePath);
      }
      return Response.ok(avatarFile).header("Content-Disposition", "attachment;filename="
                      + imagePath.substring(imagePath.lastIndexOf('/') + 1)).
              header("Content-Length", avatarFile.length()).build();
    } catch (Exception e) {
      LOG.errorv("requestId: {0}, get image failed: {1}", requestId, e);
      throw new ServiceOperationException(ServiceError.GET_IMAGE_FAILED);
    }

  }


  /**
   * 更新用户信息
   * @author zhichuang
   * @param userId userId
   * @param requestId requestId
   * @date 2021-10-08 21:39:57
   **/
  @Logged
  @PUT
  @Transactional
  @Path("/personal/info")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "Tries to update member information, only admin and yourself has right.")
  public ResponseBase<PersonalInfoResult> editUserInfo(@Valid @NotBlank @QueryParam("userId") String userId,
      @Valid @NotBlank @HeaderParam("Request-Id") String requestId,
      @Valid @Schema(description = "修改者信息和被修改的信息") PersonalInfo personalInfo) {
    return userInfoUpdate(userId, requestId, personalInfo);
  }


  @Logged
  public ResponseBase<PersonalInfoResult> userInfoUpdate(String userId, String requestId, PersonalInfo personalInfo) {
    if(!StringUtils.isBlank(personalInfo.getUserDomain())){
      return ResponseBase.of("ACC-4022", "domain name cannot be modified", requestId,
              null);
    }
    if(Objects.nonNull(personalInfo.getPersonalName()) && personalInfoService.isPersonalNameUsed(personalInfo.getPersonalName(), userId)){
      return ResponseBaseEnum.PERSONAL_NAME_IS_USED.getResponseBase(requestId);
    }
    var userEntity = memberManageService.findByUserId(userId);
    if(StringUtils.isBlank(personalInfo.getAoId())){
      throw new ServiceOperationException(ServiceError.INVALID_AO_ID);
    }
    if (userId.equals("1") || personalInfo.getAoId().equals(userEntity.getAoId())) {
      UserEntity userModify = memberManageService.findByAoId(personalInfo.getAoId());
      if (Objects.isNull(userModify)) {
        throw new ServiceOperationException(ServiceError.USER_NOT_FOUND);
      }
      personalInfoService.updatePersonalInfo(userModify, personalInfo);
      return ResponseBase.of(ResponseCodeConstant.ACC_201, "The user information is modified successfully.",
          requestId, PersonalInfoResult.of(true, null));
    } else {
      return ResponseBase.of("ACC-4001", "Failed to modify user information, permission denied",
          requestId, PersonalInfoResult.of(false, null));
    }
  }
}
