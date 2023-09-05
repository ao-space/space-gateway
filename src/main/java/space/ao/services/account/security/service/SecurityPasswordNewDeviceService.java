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

import space.ao.services.account.security.dto.ApplyInfoNewDeviceReq;
import space.ao.services.account.security.dto.SecurityPasswdResetNewDeviceReq;
import space.ao.services.support.response.ResponseBase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;

/**
 * 密码逻辑服务类。
 */
@ApplicationScoped
public class SecurityPasswordNewDeviceService {

    @Inject
    SecurityPasswordCommonService securityPasswordCommonService;


    /**
     * 新设备申请重置(called by system-agent)
     * @param requestId requestId
     * @param clientUUid clientUUid
     * @param req ApplyInfoNewDeviceReq
     * @return 申请结果
     */
    public ResponseBase<Object> securityPasswdResetNewDeviceApplyInLocal(String requestId,
                                                                         String clientUUid,
                                                                         @Valid ApplyInfoNewDeviceReq req) {
        return securityPasswordCommonService.securityPasswdResetAuthorApply( requestId, req.getApplyId(),
                "1",
                clientUUid,
                req,
                true);
    }

    /**
     * 新设备提交重置(called by system-agent)
     * @param requestId requestId
     * @param clientUUid clientUUid
     * @param req SecurityPasswdResetNewDeviceReq
     * @return 重置结果
     */
    public ResponseBase<Object> securityPasswdResetNewDeviceInLocal(String requestId,
                                                            String applyId,
                                                            String clientUUid,
                                                            SecurityPasswdResetNewDeviceReq req) {
        return securityPasswordCommonService.securityPasswdResetAuthorOrNewDevice( requestId,
                applyId,
                "1",
                clientUUid,
                req,
                true);
    }

}
