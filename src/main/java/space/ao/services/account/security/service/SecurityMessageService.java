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

import io.quarkus.logging.Log;
import space.ao.services.support.redis.SecurityMessageRedisService;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.security.dto.SecurityMessagePollReq;
import space.ao.services.account.security.dto.SecurityMessageRsp;
import space.ao.services.account.security.dto.SecurityMessageStore;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.OperationUtils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class SecurityMessageService {
    @Inject
    SecurityMessageRedisService securityMessageRedisService;
    @Inject
    OperationUtils utils;

    @Inject
    ApplicationProperties properties;

    private final String redisKeyPrefix = "SCP-";

//    map<String toClientUUid,  List<SecurityMessageStore>>
    public void storeMessage(String toClientUUid, SecurityMessageStore securityMessageStore) {
        if (toClientUUid==null || toClientUUid.length()<1 || securityMessageStore==null) {
            return;
        }

        String k = redisKeyPrefix + toClientUUid;
        securityMessageRedisService.rpush(k, utils.objectToJson(securityMessageStore));

        var l= Duration.parse(properties.gatewayTimeOfSecurityPasswdAkLife()).toSeconds();
        securityMessageRedisService.expire(k, l);
    }

    public List<SecurityMessageStore> retriveMessage(String toClientUUid) {

        List<SecurityMessageStore> lst = new ArrayList<>();

        String k = redisKeyPrefix+toClientUUid;
        securityMessageRedisService.lrange(k,0, -1).forEach((e) -> {
            var msg =  utils.jsonToObject(e, SecurityMessageStore.class);
            lst.add(msg);
        });

        if (lst.size()>0) {
            securityMessageRedisService.ltrim(k,lst.size(),-1);
        }

        return lst;
    }

    public ResponseBase<List<SecurityMessageRsp>> poll(String requestId,
                                                       String userId,
                                                       String clientUUid,
                                                       SecurityMessagePollReq req) {

        List<SecurityMessageRsp> res = new ArrayList<>();

        var lst = retriveMessage(clientUUid);
        int n = 0;
        // redis 中数据为空时, 调用不会阻塞. 所有这里暂时增加重试.
        while (lst.size()<1 && n++<5) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.infov("security message poll error: {0},\n userid: {1}, clientUUID: {2}, " +
                                "SecurityMessagePollReq: {3}", e, userId, clientUUid, req);
                Thread.currentThread().interrupt();
            }
            lst = retriveMessage(clientUUid);
        }

        for (SecurityMessageStore msg : lst) {
            if (ZonedDateTime.now().isBefore(msg.getExpiresAt())) {
                res.add(msg.getSecurityMessageRsp());
            }
        }

        return ResponseBase.okACC(requestId, res);
    }


    public ResponseBase<List<SecurityMessageRsp>> pollInLocal(String requestId,
                                                              SecurityMessagePollReq req) {

        return poll( requestId,
                 "1",
                 req.getClientUuid(),
                 req);
    }
}
