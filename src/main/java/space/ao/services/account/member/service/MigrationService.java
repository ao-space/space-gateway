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

import org.jboss.logging.Logger;
import space.ao.services.support.log.Logged;
import space.ao.services.support.platform.info.registry.RegistryTypeEnum;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.authorizedterminalinfo.repository.AuthorizedTerminalRepository;
import space.ao.services.account.member.dto.migration.BoxMigrationInfo;
import space.ao.services.account.member.dto.migration.ClientMigrationInfo;
import space.ao.services.account.member.dto.migration.UserMigrationInfo;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Objects;

@ApplicationScoped
public class MigrationService {
    private static final Logger LOG = Logger.getLogger("app.log");
    @Inject
    UserInfoRepository userInfoRepository;
    @Inject
    AuthorizedTerminalRepository authorizedTerminalRepository;
    @Logged
    public BoxMigrationInfo getMigrationInfo(String requestId) {
        LOG.infov("start get migration info: {0}", requestId);
        var userInfos = new ArrayList<UserMigrationInfo>();
        var allUserEntity = userInfoRepository.findAll().list();
        LOG.infov("get all user: {0}", allUserEntity);
        for (var userEntity: allUserEntity){
            var clientInfos = new ArrayList<ClientMigrationInfo>();
            var allClientEntity = authorizedTerminalRepository.findByUserid(userEntity.getId());
            LOG.infov("get user: {0}, all client: {1}", userEntity.getId(), allClientEntity);
            for (var clientEntity: allClientEntity){
                if (clientEntity.getUuid().equals(userEntity.getClientUUID())){
                    clientInfos.add(ClientMigrationInfo.of(clientEntity.getUuid(), RegistryTypeEnum.CLIENT_BIND.getName()));
                } else {
                    clientInfos.add(ClientMigrationInfo.of(clientEntity.getUuid(), RegistryTypeEnum.CLIENT_AUTH.getName()));
                }
            }
            if(userEntity.getRole().equals(UserEntity.Role.ADMINISTRATOR)){
                userInfos.add(UserMigrationInfo.of(userEntity.getAoId(), userEntity.getUserDomain(), RegistryTypeEnum.USER_ADMIN.getName(),
                        clientInfos));
            } else {
                userInfos.add(UserMigrationInfo.of(userEntity.getAoId(), userEntity.getUserDomain(), RegistryTypeEnum.USER_MEMBER.getName(),
                        clientInfos));
            }
        }
        LOG.infov("get all migration info: {0}", requestId);
        return BoxMigrationInfo.of(null, userInfos);
    }

    @Transactional
    @Logged
    public ResponseBase<BoxMigrationInfo> updateUserInfos(BoxMigrationInfo boxMigrationInfo, String requestId) {
        for (var userInfo: boxMigrationInfo.userInfos()) {
            var userEntity = userInfoRepository.findByAoId(userInfo.userId());
            if(Objects.isNull(userEntity)){
                return ResponseBase.of("ACC-404", "user : " + userInfo.userId() + " not exits",
                        requestId, null);
            }
            userEntity.setUserDomain(userInfo.userDomain());
            userEntity.persist();
        }
        return ResponseBase.okACC(requestId, boxMigrationInfo);
    }
}
