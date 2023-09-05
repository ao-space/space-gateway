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

package space.ao.services.account.member.respository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.jboss.logging.Logger;
import space.ao.services.account.member.entity.BoxInfoEntity;
import space.ao.services.support.log.Logged;

import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;


@Singleton
public class BoxInfoRepository implements PanacheRepository<BoxInfoEntity> {
  private static final Logger LOG = Logger.getLogger("app.log");

  /**
   * 插入boxregkey和passcode的记录，该记录有且只有一条
   * @author suqin
   * @param boxRegKey 盒子注册密钥
   * @param passcode 盒子密码
   * @date 2021-10-08 21:39:57
   **/
  @Transactional
  public BoxInfoEntity insertOrUpdate(String boxRegKey, String passcode) {
    var boxInfo = new BoxInfoEntity();
    boxInfo.setBoxRegKey(boxRegKey);
    boxInfo.setPasscode(passcode);

    if(findAll().list().isEmpty()){
      boxInfo.persist();
    } else {
      this.update("set boxRegKey=?1, passcode=?2", boxRegKey, passcode);
    }
    return boxInfo;
  }

  @Transactional
  public BoxInfoEntity create() {
    var boxInfo = new BoxInfoEntity();
    if(findAll().list().isEmpty()){
      boxInfo.persist();
    } else {
      boxInfo = findById(1L);
    }
    return boxInfo;
  }

  @Transactional
  public int update(String email, String host, String port, boolean securityEmailSslEnable){
    return this.update("set securityEmail=?1, securityEmailHost=?2, "
            + "securityEmailPort=?3, securityEmailSslEnable=?4", email, host, port, securityEmailSslEnable);
  }

  @Logged
  @Transactional
  public String getAnyBoxRegKey() {
    var boxInfo = findAll().firstResult();
    return boxInfo != null ? boxInfo.getBoxRegKey() : null;
  }


  public String getEmail(){
    var boxInfo = findAll().firstResult();
    return boxInfo != null ? boxInfo.getSecurityEmail() : null;
  }

  @Logged
  public boolean setPasscode(String requestId, String password){
    LOG.infov("requestId: {0} , set passcode: {1}", requestId, password);
    return 1 == update("passcode = ?1 where id=1", password);
  }
  @Logged
  public String getPasscode(String requestId){
    var boxInfo = findById(1L);
    LOG.infov("requestId: {0} , get boxInfo: {1}", requestId, boxInfo);
    return boxInfo.getPasscode();
  }
}
