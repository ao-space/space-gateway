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

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.runtime.Startup;

import java.nio.charset.StandardCharsets;
import java.util.*;

import io.smallrye.openapi.runtime.util.StringUtil;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import space.ao.services.account.deviceinfo.dto.UserStorageInfo;
import space.ao.services.account.deviceinfo.service.DeviceStorageService;
import space.ao.services.account.member.dto.*;
import space.ao.services.account.personalinfo.dto.AccountInfoResult;
import space.ao.services.account.security.utils.SecurityPasswordUtils;
import space.ao.services.account.support.service.AdminInfoFileDTO;
import space.ao.services.account.support.service.ServiceDefaultVar;
import space.ao.services.account.support.service.ServiceError;
import space.ao.services.support.agent.AgentServiceRestClient;
import space.ao.services.support.agent.info.DidDoc;
import space.ao.services.support.agent.info.DidDocResult;
import space.ao.services.support.file.FileServiceRestClient;
import space.ao.services.support.log.Logged;
import space.ao.services.support.platform.PlatformOpstageServiceRestClient;
import space.ao.services.support.platform.PlatformUtils;
import space.ao.services.support.redis.RedisTokenService;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.authorizedterminalinfo.repository.AuthorizedTerminalRepository;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.push.dto.Message;
import space.ao.services.push.dto.NotificationEnum;
import space.ao.services.push.services.RedisService;
import space.ao.services.support.OperationUtils;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.*;

@ApplicationScoped
@Startup
public class MemberManageService {
  @Inject
  ApplicationProperties properties;

  @Inject
  @RestClient
  FileServiceRestClient fileServiceRestClient;

  @Inject
  OperationUtils utils;

  @Inject
  UserInfoRepository userInfoRepository;

  @Inject
  AuthorizedTerminalRepository authorizedTerminalRepository;
  
  @Inject
  RedisService redisService;

  @Inject
  PlatformRegistryService platformRegistryService;

  @Inject
  SecurityPasswordUtils securityPasswordUtils;
  @Inject
  RedisTokenService redisTokenService;
  @Inject
  PlatformUtils platformUtils;
  @Inject
  @RestClient
  AgentServiceRestClient agentServiceRestClient;

  @Inject
  @RestClient
  PlatformOpstageServiceRestClient platformOpstageServiceRestClient;
  @Inject
  @RestClient
  DeviceStorageService deviceStorageService;
  private static int count = 0; // 该变量用于记录密码输入次数。通过成员变量在不同的请求中共享，其正确性的前提是该服务只会单实例部署，如果多实例部署，需要重构该变量的共享方式。

  private static Long timeTicket = System.currentTimeMillis(); // 该变量用于记录密码输入次数超过限制之后，再次允许验证密码的时间。通过成员变量在不同的请求中共享，其正确性的前提是该服务只会单实例部署，如果多实例部署，需要重构该变量的共享方式。
  private static Long windowsTicket = System.currentTimeMillis(); // 密码输入窗口
  static final Logger LOG = Logger.getLogger("app.log");

  /**
   * 查询用户所占用的空间信息
   * @author suqin
   * @date 2021-10-12 21:38:33
   * @param userId userId
   * @param targetUserId 目标用户信息
   * @return 返回空间结果
   **/
  @Logged
  public ResponseBase<UserStorageInfo> fileStorageInfo(String requestId, String userId, String targetUserId){
    return fileServiceRestClient.getUserStorageInfo(requestId, userId, targetUserId);
  }


  /**
   * 数据库根据clientUUID查找用户信息
   * @author suqin
   * @date 2021-10-12 21:38:33
   * @param clientUUID 客户端id
   * @return 用户信息
   **/
  public UserEntity findByClientUUID(String clientUUID){
    return userInfoRepository.findByClientUUID(clientUUID);
  }


  /**
   * 数据库查找所有用户
   * @author suqin
   * @date 2021-10-12 21:38:33
   * @return 查询所有用户结果
   **/
  public PanacheQuery<UserEntity> findAll(){
    return  userInfoRepository.findAll();
  }

  @Logged
  public List<AccountInfoResult> getMemberList(String requestId){
    ArrayList<AccountInfoResult> personalList = new ArrayList<>();
    List<UserEntity> userList = findAll(Sort.by("id")).list();
    for(UserEntity userEntity :userList){
      personalList.add(getMemberInfo(requestId, userEntity));
    }
    return personalList;
  }

  public AccountInfoResult getMemberInfo(String requestId, UserEntity userEntity){
    // 对于用户总的存储空间：如果邀请时设置的空间配额，则为该配额；如果没有设置，则为设备的总存储大小。
    var fileStorageInfo = fileStorageInfo(requestId, userEntity.getId().toString(), String.valueOf(userEntity.getId()));
    if (userEntity.getSpaceLimit() != null) {
      fileStorageInfo.results().setTotalStorage(String.valueOf(userEntity.getSpaceLimit()));
    } else {
      var resp = deviceStorageService.getStorageInfo(requestId);
      fileStorageInfo.results().setTotalStorage(resp.results().getTotal());
    }
    var did = getDid(requestId, null, userEntity.getAoId());
    return AccountInfoResult.of(userEntity.getRole().name(), userEntity.getPersonalName(), userEntity.getPersonalSign(),
            userEntity.getCreateAt(),userEntity.getAoId(), userEntity.getClientUUID(), userEntity.getPhoneModel(),
            userEntity.getUserDomain(), userEntity.getImageMd5(), fileStorageInfo.results().getUserStorage(),
            fileStorageInfo.results().getTotalStorage(), did);
  }

  public String getDid(String requestId, String did, String aoid){
    ResponseBase<DidDocResult> didDocResult = null;
    String didDocString = null;
    DidDoc didDoc = null;
    try {
      didDocResult = agentServiceRestClient.getDidDocument(requestId, did, aoid);
      didDocString = new String(Base64.getDecoder().decode(didDocResult.results().didDoc()), StandardCharsets.UTF_8);
      didDoc = utils.jsonToObject(didDocString, DidDoc.class);
      did = didDoc.id();
    } catch (Exception e){
      LOG.errorv("did obtain failed, requestId: {0}, did: {1}, aoid: {2}, didDocResult: {3}, didDocString: {4}, " +
                      "didDoc: {5}, exception: {6}", requestId, did, aoid, didDocResult, didDocString, didDoc, e.getMessage());
    }
    return did;
  }
  /**
   * 数据库查找所有用户，并按照指定顺序排列
   * @author suqin
   * @date 2021-10-12 21:38:33
   * @param sort 指定顺序排列
   * @return 按 sort 查询所有用户结果
   **/
  public PanacheQuery<UserEntity> findAll(Sort sort){
    return  userInfoRepository.findAll(sort);
  }

  /**
   * 数据库根据userId查找用户
   * @author suqin
   * @date 2021-10-12 21:38:33
   * @param userId userId
   * @return 用户信息
   **/
  public UserEntity findByUserId(String userId){
    return  userInfoRepository.findByUserId(Long.valueOf(userId));
  }

  /**
   * 数据库根据aoId查找用户
   * @author suqin
   * @date 2021-10-12 21:38:33
   * @param aoid aoId
   * @return 用户信息
   **/
  public UserEntity findByAoId(String aoid){
    return userInfoRepository.findByAoId(aoid);
  }

  /**
   * 撤销指定用户的客户端授权信息。
   *
   * @param userId userId 指定的用户 id
   * @return 撤销是否成功，0：成功，1：向平台解邦失败, 2: 密码错误， 3：输入密码次数过多， 4：管理员已被解绑。
   */
  @Logged
  @Transactional
  public ResponseBase<PasswdTryInfo> revokeUserClientInfo(String userId, String requestId, String passcode, String clientUUID) {
    var userEntity = findByUserId(userId);
    if(userId.equals("1")){
      if(timeTicket > System.currentTimeMillis()){
        return ResponseBase.of(ServiceDefaultVar.ACCOUNT_PASSWD_EXCEED.toString(), "too much password tries", requestId,
            PasswdTryInfo.of(properties.boxUuid(), count, 0, timeTicket-System.currentTimeMillis()));
      } else {
        if (!securityPasswordUtils.doVerifyPasscode(requestId, passcode)) {
          return passcodeError(requestId, properties.boxUuid());
        }
        count = 0;
        if (userEntity.getClientUUID() == null){//如果数据库查询clientUUID为null，判断成员/管理员已经被解绑过
          return  ResponseBase.of(ServiceDefaultVar.ACCOUNT_ADMIN_REVOKED.toString(), "admin has been revoked", requestId,
              PasswdTryInfo.of(properties.boxUuid(), 0, 0, 0L));
        } else {
          Map<String, String> mapInfo =utils.readFromFile(new File(properties.accountDataLocation()+ ServiceDefaultVar.DEFAULT_DATA_FILE));
          mapInfo.put("status", "2");
          mapInfo.put("clientUUID", null);
          mapInfo.put("phoneModel", null);
          mapInfo.put("authKey", null);
          utils.writeToFile(new File(properties.accountDataLocation() + ServiceDefaultVar.DEFAULT_DATA_FILE), mapInfo);
        }
      }
    }

    var authorizedTerminals = authorizedTerminalRepository.findByUserid(Long.valueOf(userId));

    for (var auth: authorizedTerminals){
      authorizedTerminalRepository.delete(auth.getUserid(), auth.getUuid());

      try {
        //向平台解注册授权端
        platformRegistryService.platformRegistryClientReset(requestId, auth.getAoid(), auth.getUuid());
      } catch (Exception e) {
        LOG.warn("platformRegistryClientReset error", e);
      }

      if(!Objects.equals(auth.getUuid(), clientUUID)){ // 自己解绑自己时不发送通知给自己
        redisService.pushMessage(Message.of(
            String.valueOf(auth.getUserid()), auth.getUuid(), NotificationEnum.REVOKE.getType(), requestId,
            utils.objectToJson(
                MemberDeleteResult.of(auth.getUuid(), auth.getAoid(), userEntity.getUserDomain()))));
      }
      redisService.del(auth.getUserid().toString(), auth.getUuid());
    }
    // 目前预期只有一条记录被成功修改。
    userInfoRepository.revokeUserClientInfo(Long.valueOf(userId));
    redisTokenService.deleteByAoid(userEntity.getAoId());
    return ResponseBase.of("ACC-200", "revoke user success", requestId, null);
  }

  private static ResponseBase<PasswdTryInfo> passcodeError(String requestId, String boxUUID) {
    count++;
    if(windowsTicket < System.currentTimeMillis() - 60 * 1000){ // 上次输入错误 在一分钟之外
      windowsTicket = System.currentTimeMillis();
      count = 1;
    }
    if(count <= 3 && windowsTicket > System.currentTimeMillis() - 60 * 1000){  // 上次输入错误 在一分钟之内 且三次之内
      return ResponseBase.of(ServiceDefaultVar.ACCOUNT_PASSWD_ERROR.toString(), "password error", requestId,
          PasswdTryInfo.of(boxUUID, count, 3-count, 0));
    }
    count = 0;
    timeTicket = System.currentTimeMillis() + 1000*60;
    return ResponseBase.of(ServiceDefaultVar.ACCOUNT_PASSWD_ERROR.toString(), "password error, please try again after one minute", requestId,
        PasswdTryInfo.of(boxUUID, 3, 0, 0));

  }

  @Logged
  public void writeToAdminFile(AdminBindInfo adminBindInfo, String authKey, String userDomain, String boxName, ClientPairStatusEnum clientPairStatus){
    writeToAdminFile(
            AdminInfoFileDTO.of(adminBindInfo.getClientUUID(), authKey, adminBindInfo.getPhoneModel(), boxName,
                    clientPairStatus.getStatus(), null, userDomain)
    );
  }


  @Logged
  public void writeUserDomainToAdminFile(String userDomain){
    var adminInfo = readFromAdminFile();
    adminInfo.setUserDomain(userDomain);
    this.writeToAdminFile(adminInfo);
  }

  @Logged
  public void writeToAdminFile(AdminInfoFileDTO adminInfoFileDTO){
    var adminInfo = readFromAdminFile();
    if (Objects.nonNull(adminInfoFileDTO.getStatus())){
      adminInfo.setStatus(adminInfoFileDTO.getStatus());
    }
    if (Objects.nonNull(adminInfoFileDTO.getApplyEmail())){
      adminInfo.setApplyEmail(adminInfoFileDTO.getApplyEmail());
    }
    if (Objects.nonNull(adminInfoFileDTO.getBoxName())){
      adminInfo.setBoxName(adminInfoFileDTO.getBoxName());
    }
    if (Objects.nonNull(adminInfoFileDTO.getClientUUID())){
      adminInfo.setClientUUID(adminInfoFileDTO.getClientUUID());
    }
    if (Objects.nonNull(adminInfoFileDTO.getPhoneModel())){
      adminInfo.setPhoneModel(adminInfoFileDTO.getPhoneModel());
    }
    if (Objects.nonNull(adminInfoFileDTO.getAuthKey())){
      adminInfo.setAuthKey(adminInfoFileDTO.getAuthKey());
    }
    if (Objects.nonNull(adminInfoFileDTO.getUserDomain())){
      adminInfo.setUserDomain(adminInfoFileDTO.getUserDomain());
    }
    utils.writeToFile(new File(properties.accountDataLocation() + ServiceDefaultVar.DEFAULT_DATA_FILE), utils.objectToMap(adminInfoFileDTO));
  }

  @Logged
  public AdminInfoFileDTO readFromAdminFile(){
    Map<String, String> adminInfo = utils.readFromFile(new File(properties.accountDataLocation() + ServiceDefaultVar.DEFAULT_DATA_FILE));
    return utils.mapToObject(adminInfo, AdminInfoFileDTO.class);
  }

  /**
   * 开机自启动，写管理员信息
   */
  @PostConstruct
  @SuppressWarnings("unused") // 开机自启动，写管理员信息
  public void createAdminFile() throws IOException {
    var file = new File(properties.accountDataLocation());
    if(file.exists() || file.mkdirs()) {
      LOG.error(ServiceError.CREATE_ADMIN_INIT_FAILED);
    }
    var data = new File(properties.accountDataLocation() + ServiceDefaultVar.DEFAULT_DATA_FILE);
    if(!data.exists()) {
      LOG.info("admin file create");
      if(!data.createNewFile()){
        LOG.error(ServiceError.CREATE_ADMIN_INIT_FAILED);
      }
      Map<String, String> mapInfo = new HashMap<>();
      mapInfo.put("status", "1");
      utils.writeToFile(data, mapInfo);
    }

    var allUsers = userInfoRepository.findAll().list();
    //创建成员通讯录collection
    //试用用户填写applyEmail
    var requestId = utils.createRandomType4UUID();
    String boxRegKey;
    if(utils.getEnableInternetAccess() && platformUtils.isRegistryPlatformAvailable(requestId)){
      boxRegKey = platformUtils.createOpstageBoxRegKey(requestId);
    } else {
      boxRegKey = null;
    }
    var spaceBootstrapType = SpaceBootstrapTypeEnum.getSpaceBootstrapType(properties.boxDeviceModelNumber());
    userInfoRepository.findAll().list().forEach( p -> {
      if(Objects.nonNull(boxRegKey)){
        if(spaceBootstrapType.equals(SpaceBootstrapTypeEnum.SpaceBootstrapTypePc) &&
                p.getRole().equals(UserEntity.Role.ADMINISTRATOR) &&
                !StringUtil.isNotEmpty(p.getApplyEmail())) {
          var res = platformOpstageServiceRestClient.trailUser(requestId, boxRegKey, SpaceBootstrapTypeEnum.SpaceBootstrapTypePc.getName(), properties.boxUuid(), null);
          if(!Objects.isNull(res) && !Objects.isNull(res.getEmail())) {
            userInfoRepository.updateApplyEmail(res.getEmail(),p.getId());
          }
        } else if(spaceBootstrapType.equals(SpaceBootstrapTypeEnum.SpaceBootstrapTypeOnline) &&
                p.getRole().equals(UserEntity.Role.GUEST) &&
                !StringUtil.isNotEmpty(p.getApplyEmail())) {
          var res = platformOpstageServiceRestClient.trailUser(requestId, boxRegKey, SpaceBootstrapTypeEnum.SpaceBootstrapTypeOnline.getName(), properties.boxUuid(), p.getAoId());
          if(!Objects.isNull(res) && !Objects.isNull(res.getEmail())) {
            userInfoRepository.updateApplyEmail(res.getEmail(),p.getId());
          }
        }
      }
    });

  }
}
