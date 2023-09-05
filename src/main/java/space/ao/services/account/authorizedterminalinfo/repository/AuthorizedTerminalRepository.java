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

package space.ao.services.account.authorizedterminalinfo.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import java.util.Objects;

import space.ao.services.account.authorizedterminalinfo.dto.AuthorizedTerminalInfo;
import space.ao.services.account.authorizedterminalinfo.entity.AuthorizedTerminalEntity;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.support.log.Logged;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;

@Singleton
public class AuthorizedTerminalRepository implements PanacheRepository<AuthorizedTerminalEntity> {

  private static final String BY_USERID_AND_UUID = "userid=?1 and uuid=?2";

  @Inject
  UserInfoRepository userInfoRepository;
  /**
   * 根据userid查询授权端信息
   * @author suqin
   * @param  userId userId
   * @date 2021-11-22 16:39:57
   **/
  @Transactional
  public List<AuthorizedTerminalEntity> findByUserid(Long userId){
    return find("userid", userId).list();
  }

  /**
   * 查询所有非管理员角色的授权信息
   * @author suqin
   * @date 2021-11-22 16:39:57
   **/
  public List<AuthorizedTerminalEntity> findByAoid(String aoId){
    return find("aoid", aoId).list();
  }

  /**
   * 根据userid和uuid查询授权端信息
   * @author suqin
   * @param userId userId userId
   * @param uuid uuid
   * @date 2021-11-22 16:39:57
   **/
  @Transactional
  public AuthorizedTerminalEntity findByUseridAndUuid(Long userId, String uuid) {
    return find(BY_USERID_AND_UUID, userId, uuid).firstResult();
  }

  /**
   * 根据 uuid查询授权端信息
   * @param uuid clientUUID
   * @return AuthorizedTerminalEntity
   */
  public AuthorizedTerminalEntity findByUuid(String uuid) {
    return find("uuid=?1",  uuid).firstResult();
  }

  public AuthorizedTerminalEntity findByAoidAndUuid(String aoId, String uuid) {
    return find("aoid=?1 and uuid=?2", aoId, uuid).firstResult();
  }

  /**
   * 根据userId
   * @author suqin
   * @param userId userId
   * @date 2021-11-22 16:39:57
   **/
  @Transactional
  public void delete(Long userId){
    delete("userid", userId);
  }

  /**
   * 根据userId和uuid删除相关授权信息
   * @author suqin
   * @param userId userId
   * @date 2021-11-22 16:39:57
   **/
  @Transactional
  public void delete(Long userId, String uuid){
    delete(BY_USERID_AND_UUID, userId, uuid);
  }

  /**
   * 插入授权终端信息
   * @author suqin
   * @param authorizedTerminalInfo 授权端信息
   * @date 2021-11-22 16:39:57
   **/
  @Transactional
  @Logged
  public AuthorizedTerminalEntity insert(AuthorizedTerminalInfo authorizedTerminalInfo){
    var userEntity = userInfoRepository.findByUserId(Long.valueOf(authorizedTerminalInfo.userId()));
    var authorizedTerminalEntity = new AuthorizedTerminalEntity();
    var authorizedTerminal = findByUseridAndUuid(Long.valueOf(authorizedTerminalInfo.userId()), authorizedTerminalInfo.uuid());

    if(Objects.nonNull(authorizedTerminal)){
      authorizedTerminalEntity = authorizedTerminal;
    }
    authorizedTerminalEntity.setTerminalMode(authorizedTerminalInfo.terminalMode());
    authorizedTerminalEntity.setTerminalType(authorizedTerminalInfo.terminalType());

    authorizedTerminalEntity.setUserid(Long.valueOf(authorizedTerminalInfo.userId()));
    authorizedTerminalEntity.setAoid(userEntity.getAoId());
    authorizedTerminalEntity.setUuid(authorizedTerminalInfo.uuid());
    authorizedTerminalEntity.setCreateAt(OffsetDateTime.now());
    authorizedTerminalEntity.setLoginAt(OffsetDateTime.now());
    authorizedTerminalEntity.setExpireAt(OffsetDateTime.now().plusSeconds(authorizedTerminalInfo.expireAt()));
    authorizedTerminalEntity.setAddress(authorizedTerminalInfo.address());
    save(authorizedTerminalEntity);

    var list = find(BY_USERID_AND_UUID, authorizedTerminalEntity.getUserid(), authorizedTerminalEntity.getUuid()).list();

    for (var i=1;i<list.size();i++){
      list.get(i).delete();
    }

    return authorizedTerminalEntity;
  }

  @Transactional
  public List<AuthorizedTerminalEntity> findAllTerminal(){
    List<AuthorizedTerminalEntity> all = findAll().list();
    return all.stream().distinct().toList();
  }

  @Transactional
  @Logged
  public void save(AuthorizedTerminalEntity terminalEntity) {
    persist(terminalEntity);
  }
}
