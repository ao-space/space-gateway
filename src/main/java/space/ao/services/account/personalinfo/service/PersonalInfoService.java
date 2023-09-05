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

package space.ao.services.account.personalinfo.service;

import java.util.Objects;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import space.ao.services.account.personalinfo.dto.PersonalInfo;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.support.StringUtils;
import space.ao.services.support.log.Logged;

@ApplicationScoped
public class PersonalInfoService {
  @Inject
  UserInfoRepository userInfoRepository;


  /**
   * 验证 personalName 是否已使用
   * @param personalName 用户名
   * @return 是否已使用
   */
  @Transactional
  public boolean isPersonalNameUsed(String personalName, String userid) {
    var userEntities = userInfoRepository.findByPersonalName(personalName);
    if(userEntities.isEmpty()){
      return false;
    } else {
      return userEntities.stream().map(UserEntity::getId).noneMatch(Long.valueOf(userid)::equals);
    }
  }

  /**
   * 验证 personalName 是否已使用
   * @param personalName 用户名
   * @return 是否已使用
   */
  @Transactional
  public boolean isPersonalNameUsed(String personalName) {
    var userEntities = userInfoRepository.findByPersonalName(personalName);
    return !userEntities.isEmpty();
  }

  /**
   * 修改用户信息不包括子域名
   * @param userModify 用户信息数据库实体
   * @param personalInfo 要修改的用户信息
   */
  @Transactional
  @Logged
  public void updatePersonalInfo(UserEntity userModify, PersonalInfo personalInfo){
    if (!StringUtils.isBlank(personalInfo.getPersonalName())){
      userModify.setPersonalName(personalInfo.getPersonalName());
    }
    if (!Objects.isNull(personalInfo.getPersonalSign())){
      userModify.setPersonalSign(personalInfo.getPersonalSign());
    }
    if (!StringUtils.isBlank(personalInfo.getPhoneModel())){
      userModify.setPhoneModel(personalInfo.getPhoneModel());
    }
  }
}
