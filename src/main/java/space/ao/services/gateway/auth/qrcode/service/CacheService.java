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

package space.ao.services.gateway.auth.qrcode.service;

import com.google.common.base.Stopwatch;
import io.quarkus.scheduler.Scheduled;
import org.jboss.logging.Logger;
import space.ao.services.gateway.auth.qrcode.dto.CreateAuthCodeDTO;

import jakarta.enterprise.context.ApplicationScoped;
import space.ao.services.support.service.ServiceError;
import space.ao.services.support.service.ServiceOperationException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class CacheService {
    static final Logger LOG = Logger.getLogger("app.log");

    // 缓存超时时间 10分钟
    private static final Long TIME_OUT_SECOND = 10 * 60 * 1000L;

    private static final ConcurrentMap<String, CreateAuthCodeDTO> cacheMap = new ConcurrentHashMap<>();

    public void setAuthCodeInfo(CreateAuthCodeDTO createAuthCodeDTO) {
        cacheMap.put(createAuthCodeDTO.getBkey(), createAuthCodeDTO);
    }

    public CreateAuthCodeDTO getAuthCodeInfo(String bkey) {
        if (!cacheMap.containsKey(bkey)) {
            throw new ServiceOperationException(ServiceError.BOX_KEY_NOT_MATCH);
        }
        return cacheMap.get(bkey);
    }

    public boolean hasKey(String bkey) {
        return cacheMap.containsKey(bkey);
    }

    public CreateAuthCodeDTO remove(String bkey) {
        return cacheMap.remove(bkey);
    }

    @Scheduled(every = "30m") // 每半小时清理一次缓存
    @SuppressWarnings("unused") // Executing a Scheduled Task
    void cleanMap(){
        Stopwatch stopwatch = Stopwatch.createStarted();
        cacheMap.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue().getCreateTime() > TIME_OUT_SECOND);
        LOG.info("regularly clean bkey cache completed - "+stopwatch.elapsed(TimeUnit.SECONDS));
    }

}
