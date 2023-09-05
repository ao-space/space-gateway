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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import space.ao.services.support.security.SecurityUtils;
import space.ao.services.support.StringUtils;

/**
 * 签名工具类, system-agent 调用 gateway 的接口不经过 call 和 access_toke，所有使用此签名来简单的验证 system-agent 的身份。
 */
@Singleton
public class SignUtil {

    @Inject
    SecurityUtils securityUtils;

    static final Logger LOG = Logger.getLogger("app.log");

    /*
    * 验证签名( system-agent 请求时带过来的)
     */
    public boolean verifySign(String requestId, String clientUUid, String clientUuidSign) {
        LOG.debug("verifySign, clientUuidSign=" + clientUuidSign);
        if(StringUtils.isBlank(clientUUid)){
            return false;
        }
        if (!securityUtils.getSecurityProvider().verifySignUsingBoxPublicKey(requestId, clientUUid, clientUuidSign)) {
            LOG.debug("failed verifySignUsingBoxPublicKey, clientUuidSign=" + clientUuidSign);
            return false;
        }
        return true;
    }
}
