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

package space.ao.services.account.security.service;

import org.jboss.logging.Logger;
import space.ao.services.account.security.dto.*;
import space.ao.services.account.security.utils.SecurityPasswordUtils;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.security.utils.token.SecurityTokenType;
import space.ao.services.account.security.utils.token.SecurityTokenUtils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * 密码逻辑服务类。
 */
@ApplicationScoped
public class SecurityPasswordBinderService {

    @Inject
    SecurityTokenUtils securityTokenUtils;

    @Inject
    SecurityPasswordUtils securityPasswordUtils;

    static final Logger LOG = Logger.getLogger("app.log");

    /**
     * 验证安全密码(获取 SecurityTokenRes). 其他两步验证需要先调用本接口作为第一步验证.
     * @param requestId requestId
     * @param clientUUid clientUUid
     * @param req SecurityPasswdCheckReq
     * @return SecurityTokenRes
     */
    public ResponseBase<SecurityTokenRes> securityPasswdVerify(String requestId, String clientUUid,
                                                               SecurityPasswdCheckReq req) {

        if (!securityPasswordUtils.doVerifyPasscode(requestId, req.getOldPasswd())){
            return ResponseBase.forbidden("password error", requestId);
        }
        LOG.debug("doVerifyPasswd succ, req="+req);
        var result = securityTokenUtils.create(SecurityTokenType.TOKEN_TYPE_VERIFIED_PWD_TOKEN,
                clientUUid);
        return ResponseBase.okACC(requestId, result);
    }

    /**
     * 绑定端直接修改(使用原密码)
     * @param requestId requestId
     * @param req SecurityPasswdModifyByBinderReq
     * @return 修改结果
     */
    public ResponseBase<Object> securityPasswdModifyByBinder(String requestId, SecurityPasswdModifyByBinderReq req) {

        if (securityPasswordUtils.doVerifyAndModifyPasswd(requestId, req.getOldPasswd(), req.getNewPasswd())) {
            LOG.debug("doVerifyAndModifyPasswd succ, req="+req);
            return ResponseBase.okACC(requestId, null);
        }
        return ResponseBase.forbidden("password error", requestId);
    }

    /**
     * 绑定端直接重置(使用密保邮箱)
     * @param requestId requestId
     * @param clientUUid clientUUid
     * @param req SecurityPasswdResetByBinderReq
     * @return 重置结果
     */
    public ResponseBase<Object> securityPasswdResetByBinder(String requestId, String clientUUid,
                                                            SecurityPasswdResetByBinderReq req) {

        if(!securityTokenUtils.verifyEmailToken(requestId, req.getSecurityToken(), clientUUid)) {
            return ResponseBase.forbidden("verifyEmailToken error", requestId);
        }
        LOG.debug("verifyPwdToken succ, req="+req);
        if (!securityPasswordUtils.doModifyPasscode(requestId, req.getNewPasswd())) {
            return ResponseBase.forbidden("doModifyPasswd error", requestId);
        }
        return ResponseBase.okACC(requestId, null);
    }

    /**
     * 绑定端直接重置(called by system-agent)
     * @param requestId requestId
     * @param req SecurityPasswdResetByBinderInLocalReq
     * @return 重置结果
     */
    public ResponseBase<Object> securityPasswdResetByBinderInLocal(String requestId,
                                                           SecurityPasswdResetByBinderInLocalReq req) {

        LOG.debug("verifySign succ, req="+req);
        if (!securityPasswordUtils.doModifyPasscode(requestId, req.getNewPasswd())) {
            return ResponseBase.forbidden("security token error", requestId);
        }
        return ResponseBase.okACC(requestId, null);
    }

}
