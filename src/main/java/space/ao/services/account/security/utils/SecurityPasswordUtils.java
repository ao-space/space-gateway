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

import java.util.Objects;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import space.ao.services.account.member.dto.Const;
import space.ao.services.support.agent.AgentServiceRestClient;
import space.ao.services.support.security.SecurityUtils;
import space.ao.services.account.support.service.ServiceError;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import space.ao.services.account.support.service.ServiceOperationException;
import space.ao.services.support.OperationUtils;

/**
 * 安全密码工具类, 提供安全密码的修改和查验功能.
 */
@Singleton
public class SecurityPasswordUtils {

    @Inject
    SecurityUtils securityUtils;
    @Inject
    OperationUtils operationUtils;
    @RestClient
    @Inject
    AgentServiceRestClient agentServiceRestClient;
    /**
     * 实际验证安全密码
     * @param passcode passcode
     * @return result
     */
    public boolean doVerifyPasscode(String requestId, String passcode) {
        passcode = passcode.length() == 6 ? operationUtils.string2SHA256(passcode) : passcode;
        return Objects.equals(passcode, securityUtils.getPasscode(requestId));
    }

    /**
     * 实际验证并修改安全密码
     * @param oldPasswd old password
     * @param newPasswd new password
     * @return result
     */
    @Transactional
    public boolean doVerifyAndModifyPasswd(String requestId, String oldPasswd, String newPasswd) {
        if(Objects.equals(oldPasswd, newPasswd)){
            throw new ServiceOperationException(ServiceError.PASSWORD_NOT_SAME);
        }
        if(doVerifyPasscode(requestId, oldPasswd)){
            return doModifyPasscode(requestId, newPasswd);
        }
        return false;
    }

    @Transactional
    public boolean doModifyPasscode(String requestId, String newPasswd) {
        var newPasswdSHA265 = newPasswd.length() == 6 ? operationUtils.string2SHA256(newPasswd) : newPasswd;
        var oldPasswd = securityUtils.getPasscode(requestId);
        if(securityUtils.setPasscode(requestId, newPasswdSHA265)) {
            try {
                agentServiceRestClient.changePasswordDidDocument(requestId, null, Const.Admin.ADMIN_AOID, newPasswd);
            } catch (Exception e) {
                if(Objects.nonNull(oldPasswd)){
                    doModifyPasscode(requestId, oldPasswd);
                }
                throw new ServiceOperationException(ServiceError.PASSWORD_NOT_SAME);
            }
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public boolean doModifyPasscodeNotChangeDidDocument(String requestId, String newPasswd) {
        newPasswd = newPasswd.length() == 6 ? operationUtils.string2SHA256(newPasswd) : newPasswd;
        return securityUtils.setPasscode(requestId, newPasswd);
    }

    @Transactional
    public boolean doResetPasscode(String requestId) {
        return securityUtils.resetPasscode(requestId);
    }
}
