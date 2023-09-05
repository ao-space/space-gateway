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

package space.ao.services.account.security.utils;

import org.jboss.logging.Logger;
import space.ao.services.account.authorizedterminalinfo.repository.AuthorizedTerminalRepository;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Objects;

@Singleton
public class UserRoleUtil {

    @Inject
    UserInfoRepository userInfoRepository;

    @Inject
    AuthorizedTerminalRepository authorizedTerminalRepository;

    static final Logger LOG = Logger.getLogger("app.log");

    // 判断是否是管理员主设备
    public boolean adminBindRole(String clientUuid) {
        // 判断管理员主设备或成员主设备
        var userEntity = userInfoRepository.findByClientUUID(clientUuid);
        LOG.debug("adminBindRole, clientUuid="+clientUuid);
        LOG.debug("adminBindRole, userEntity="+userEntity);
        return userEntity!=null && userEntity.getRole()== UserEntity.Role.ADMINISTRATOR;
    }

    // 判断是否是管理员授权设备
    public boolean adminAuthRole(String userId, String clientUuid) {
        var auth = authorizedTerminalRepository.findByUseridAndUuid(Long.valueOf(userId), clientUuid); // 字段有效性
        var user = userInfoRepository.findByUserId(Long.valueOf(userId));
        LOG.debug("adminAuthRole, auth="+auth);
        LOG.debug("adminAuthRole, user="+user);
        return !Objects.equals(auth.getUuid(), user.getClientUUID()) && user.getRole() == UserEntity.Role.ADMINISTRATOR;
    }

    // 判断新设备(非绑定端、非授权端、非成员、非成员授权端 返回 true)
    public boolean newDevice(String clientUuid) {
        var userEntity = userInfoRepository.findByClientUUID(clientUuid);
        var auth = authorizedTerminalRepository.findByUuid(clientUuid);
        return userEntity==null && auth==null; // 不在两表中说明是新设备
    }
}
