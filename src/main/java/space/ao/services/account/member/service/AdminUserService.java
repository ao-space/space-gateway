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

package space.ao.services.account.member.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.jboss.logging.Logger;
import space.ao.services.account.member.dto.*;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.account.security.utils.SecurityPasswordUtils;
import space.ao.services.account.support.service.ServiceDefaultVar;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.FileUtils;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.agent.AgentService;
import space.ao.services.support.log.Logged;
import space.ao.services.support.platform.info.registry.ClientRegistryInfo;
import space.ao.services.support.platform.info.registry.RegistryTypeEnum;
import space.ao.services.support.platform.info.registry.UserRegistryInfo;
import space.ao.services.support.security.SecurityUtils;
import space.ao.services.account.member.respository.BoxInfoRepository;
import space.ao.services.account.personalinfo.entity.UserEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;

@ApplicationScoped
public class AdminUserService {

  @Inject
  UserInfoRepository userInfoRepository;
  @Inject
  BoxInfoRepository boxInfoRepository;
  @Inject
  PlatformRegistryService platformRegistryService;
  @Inject
  ApplicationProperties properties;
  @Inject
  OperationUtils utils;
  @Inject
  MemberManageService memberManageService;
  @Inject
  SecurityUtils securityUtils;
  @Inject
  SecurityPasswordUtils securityPasswordUtils;
  @Inject
  AgentService agentService;

  static final Logger LOG = Logger.getLogger("app.log");

  public boolean checkPasscodeOrNew(String requestId, String passcode) {
    var userEntityAdmin = userInfoRepository.findByRole(UserEntity.Role.ADMINISTRATOR);
    if(Objects.isNull(userEntityAdmin)) {
      return true;
    }
    return securityPasswordUtils.doVerifyPasscode(requestId, passcode);
  }

  @Transactional
  public AdminBindResult createAdmin(String requestId, AdminBindInfo adminBindInfo) throws Exception {
    var userEntityAdmin = userInfoRepository.findByRole(UserEntity.Role.ADMINISTRATOR);
    if(Objects.isNull(userEntityAdmin)){
      userEntityAdmin = bindAdminUser(requestId, adminBindInfo);
    } else {
      userEntityAdmin = rebindAdminUser(requestId, adminBindInfo, userEntityAdmin);
    }

    String boxName = "EulixBox";
    if(Objects.nonNull(userEntityAdmin.getUserDomain())) {
      boxName = boxName + "-" + userEntityAdmin.getUserDomain().split("\\.")[0];
    }
    memberManageService.writeToAdminFile(adminBindInfo, userEntityAdmin.getAuthKey(), userEntityAdmin.getUserDomain(), boxName, ClientPairStatusEnum.CLIENT_PAIRED);
    securityUtils.loadAdminClientPublicFile();
    String avatar;
    if(Objects.isNull(userEntityAdmin.getUserDomain())) {
      var boxInfo = agentService.getBoxLanInfo(requestId);
      avatar = boxInfo.getLanIp() + boxInfo.getPort() + "/space/image/" + userEntityAdmin.getImage().substring(properties.accountImageLocation().length());
    } else {
      avatar = userEntityAdmin.getUserDomain() + "/space/image/" + userEntityAdmin.getImage().substring(properties.accountImageLocation().length());
    }
    return AdminBindResult.of(userEntityAdmin.getAuthKey(), userEntityAdmin.getUserDomain(), properties.boxUuid(), adminBindInfo.getClientUUID(),
            boxName, Const.Admin.ADMIN_AOID, userEntityAdmin.getPersonalName(), avatar);
  }

  /**
   * 初次绑定
   */
  @Logged
  public UserEntity bindAdminUser(String requestId, AdminBindInfo adminBindInfo) throws Exception {
    var userEntity = new UserEntity();

    var userRegistryResult = platformRegistryService.registryUser(requestId, UserRegistryInfo.of(Const.Admin.ADMIN_AOID, null,
                    RegistryTypeEnum.USER_ADMIN.getName(), adminBindInfo.getClientUUID()), adminBindInfo.getEnableInternetAccess());

    String authKey = utils.unifiedRandomCharters(32);

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
      LOG.errorv("create admin image path failed, path: {0}", adminImageFile.getPath());
    }

    userEntity.setPhoneModel(adminBindInfo.getPhoneModel());
    userEntity.setClientUUID(adminBindInfo.getClientUUID());
    userEntity.setApplyEmail(adminBindInfo.getApplyEmail());
    userEntity.setPersonalName(adminBindInfo.getSpaceName());
    userEntity.setUserDomain(userRegistryResult.userDomain());
    userEntity.setAuthKey(authKey);
    userInfoRepository.insertAdminUser(userEntity);
    boxInfoRepository.create();
    securityPasswordUtils.doModifyPasscodeNotChangeDidDocument(requestId, adminBindInfo.getPassword());
    utils.loadInternetServiceConfig(requestId);
    return userEntity;
  }

  /**
   * 重新绑定
   */
  @Logged
  public UserEntity rebindAdminUser(String requestId, AdminBindInfo adminBindInfo, UserEntity userEntityAdmin) {
    platformRegistryService.registryClient(requestId, ClientRegistryInfo.of(adminBindInfo.getClientUUID(),
            RegistryTypeEnum.CLIENT_BIND.getName()), userEntityAdmin.getAoId());
    String authKey = utils.unifiedRandomCharters(32);
    userEntityAdmin.setAuthKey(authKey);
    userInfoRepository.updateClient(adminBindInfo.getPhoneModel(), adminBindInfo.getClientUUID(), authKey, userEntityAdmin.getId());
    return userEntityAdmin;
  }

  public boolean isPersonalNameUsed(String spaceName, String adminId) {
    return userInfoRepository.isPersonalNameUsed(spaceName, adminId);
  }
}
