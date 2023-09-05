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

package space.ao.services.support.limit;


import lombok.SneakyThrows;
import org.jboss.logging.Logger;
import space.ao.services.gateway.auth.qrcode.dto.EncryptAuthInfo;
import space.ao.services.support.redis.RedisCommonStringService;
import space.ao.services.support.security.SecurityUtils;
import space.ao.services.account.support.service.ServiceError;
import space.ao.services.account.support.service.ServiceOperationException;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.Path;

import java.lang.reflect.Parameter;
import java.util.Objects;
/**
 *
 * 请求频率限制的注解
 * 使用方式是在需要限制的 restful 接口函数上增加注解。
 * 详细使用方式请参见 public @interface LimitReq 定义。
 */
@LimitReq
@Priority(210)
@Interceptor
@SuppressWarnings("unused") // Used by the framework
public class LimitReqInterceptor {
    static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    RedisCommonStringService redisCommonStringService;
    @Inject
    SecurityUtils securityUtils;

    @AroundInvoke
    Object limitReqInvocation(InvocationContext context) {
        Object ret;

        final LimitReq limitReq = context.getMethod().getAnnotation(LimitReq.class);

        // 这里取类上面的 Path 注解中的路径.
        String pathClass = null;
        Path annotationClass = context.getMethod().getDeclaringClass().getAnnotation(Path.class);
        if(annotationClass != null){
            pathClass = annotationClass.value();
        }

        // 取函数上面的 Path  注解中的路径.
        String pathMethod =null;
        Path annotationMethod = context.getMethod().getAnnotation(Path.class);
        if(annotationMethod != null){
            pathMethod = annotationMethod.value();
        }

        var clientUUid = "";
        var requestId = "";
        Parameter[] paramsName = context.getMethod().getParameters();
        Object[] paramsValues = context.getParameters();
        for (int idx=0;idx<paramsName.length;idx++) {
            var parmName = paramsName[idx];
            LOG.info("parmName: " + parmName.getName());
            switch (parmName.getName().toLowerCase()) {
                case "requestid" -> {
                    requestId = (String) paramsValues[idx];
                }
                case "clientuuid" -> {
                    clientUUid = (String) paramsValues[idx];
                }
                case "encryptauthinfo" -> {
                    clientUUid = ((EncryptAuthInfo) paramsValues[idx]).getClientUUID();
                    if (clientUUid.length() > 36) {
                        clientUUid = securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, clientUUid);
                    }
                }
                default -> {
                }
            }
        }

        LOG.debugv("clientUUid", clientUUid);

        if (Objects.nonNull(pathClass) && Objects.nonNull(pathMethod)) {
            String fullPath = pathClass+pathMethod; // http restful 路径
            // 这里 key 的生成方式写在了拦截器内部，这样实现不太好。
            // 更好的方式应该是外部传入，这样不同的模块使用本拦截器时可以自己根据业务需要来定义 key，
            // 但是目前注解的形式使用本拦截器可能不支持以变量方式传入，故暂时就用 http 请求的完整的路径加上一个前缀来作为 key。
            String key = limitReq.keyPrefix()+fullPath+"-"+clientUUid;
            // 存入 redis
            LOG.infov("limit key is: {0}",key);
            var reqTimes = increaseCounter(key, limitReq.interval());
            LOG.warnv("request limit! reqTimes={0}", reqTimes);

            if (reqTimes>limitReq.max()) {
                LOG.warnv("fullPath over request limit! reqTimes={0}", reqTimes);
                throw new ServiceOperationException(ServiceError.REQ_RATE_OVER_LIMIT);
            }
        }

        try {
            ret = doSneakyThrowsInvoke(context);
        } catch (Exception rethrow) {
            LOG.errorv(rethrow,"[Throw] method: {0}(), exception");
            throw rethrow;
        } finally {
            LOG.warnv("request ", pathClass);
        }
        LOG.warnv("request ", pathClass);

        return ret;
    }

    @SneakyThrows
    Object doSneakyThrowsInvoke(InvocationContext context) {
        return context.proceed();
    }

    public Integer increaseCounter(String key, int expiredSeconds){
        int count = (int) redisCommonStringService.incr(key);
        if (count==1) {
            redisCommonStringService.expire(key, expiredSeconds);
        }
        return count;
    }
    public void resetCounter(String key){
        redisCommonStringService.del(key);
    }
}