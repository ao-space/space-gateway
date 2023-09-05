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

package space.ao.services.account.personalinfo.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import space.ao.services.account.member.dto.Const;
import space.ao.services.support.log.Logged;
import space.ao.services.account.personalinfo.entity.UserEntity;

import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Singleton
public class UserInfoRepository implements PanacheRepository<UserEntity> {

  private static final String UPDATE_USER_DOMAIN_BY_USERID = "userDomain=?1 where id=?2";
  private static final String UPDATE_CLIENT_BY_USERID = "phoneModel=?1, clientUUID=?2, authKey=?3 where id=?4";

  /**
   * 根据角色查询用户
   * @author suqin
   * @param role 角色
   * @date 2021-10-08 21:39:57
   **/
  @Transactional
  public UserEntity findByRole(UserEntity.Role role) {
    return find("role", role).firstResult();
  }

  /**
   * 根据userid查询用户
   * @author suqin
   * @param userId userId 用户id
   * @date 2021-10-08 21:39:57
   **/
  @Transactional
  public UserEntity findByUserId(Long userId){
    return find("id", userId).firstResult();
  }

  /**
   * 根据clientUUID查询用户
   * @author suqin
   * @param clientUUID 客户端id
   * @date 2021-10-08 21:39:57
   **/
  public UserEntity findByClientUUID(String clientUUID) {
    return find("clientUUID", clientUUID).firstResult();
  }


  /**
   * 根据aoid查询用户
   * @author suqin
   * @param aoid aoid
   * @date 2021-10-08 21:39:57
   **/
  public UserEntity findByAoId(String aoid) {
    return find("aoId", aoid).firstResult();
  }

  /**
   * 根据userdomain查询用户
   * @author suqin
   * @param domain 客户domain
   * @date 2021-10-08 21:39:57
   **/
  public Optional<UserEntity> findByDomain(String domain) {
    return find("userDomain", domain).firstResultOptional();
  }

  /**
   * 根据 personal_name 查询用户
   * @author zhichuang
   * @param personalName 客户domain
   * @date 2023-03-10 15:39:11
   **/
  public List<UserEntity> findByPersonalName(String personalName) {
    return find("personalName", personalName).list();
  }


  @Transactional
  @Logged
  public UserEntity insertAdminUser(UserEntity userEntity) {
    userEntity.setAoId(Const.Admin.ADMIN_AOID);
    userEntity.setCreateAt(OffsetDateTime.now());
    userEntity.setRole(UserEntity.Role.ADMINISTRATOR);
    this.persist(userEntity);
    this.update("set id=1 where role = ?1", UserEntity.Role.ADMINISTRATOR);
    return userEntity;
  }

  public void revokeUserClientInfo(Long userId) {
    if (userId == 1L){
      this.update(
          "set  phoneModel=null, authKey=null where id=?1", userId);
    } else {
      this.update(
        "set clientUUID=null, phoneModel=null where id=?1", userId);
    }

  }

  @Transactional
  public void updatePhoneTypeByUserId(String phoneType, Long userid){
    this.update("set phoneType = ?1 where id = ?2", phoneType, userid);
  }


  @Transactional
  public void updateApplyEmail(String email, Long id) {
    this.update("applyEmail=?1 where id=?2", email, id);
  }

  @Transactional
  public boolean isPersonalNameUsed(String personalName, String userid) {
    var userEntities = this.findByPersonalName(personalName);
    if(userEntities.isEmpty()){
      return false;
    } else {
      return userEntities.stream().map(UserEntity::getId).noneMatch(Long.valueOf(userid)::equals);
    }
  }

  @Transactional
  public void updateUserDomainByUserid(String userDomain, Long id) {
    this.update(UPDATE_USER_DOMAIN_BY_USERID, userDomain, id);
  }

  public void updateClient(String phoneModel, String clientUUID, String authKey, Long userid) {
    this.update(UPDATE_CLIENT_BY_USERID, phoneModel, clientUUID, authKey, userid);
  }
}
