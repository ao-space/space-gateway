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

/*
 * Copyright (c) 2023 Institute of Software Chinese Academy of Sciences (ISCAS)
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

package space.ao.services.support.platform.check;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import lombok.SneakyThrows;
import org.jboss.logging.Logger;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.support.response.ResponseBaseEnum;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.platform.PlatformUtils;
import space.ao.services.support.service.ServiceError;
import space.ao.services.support.service.ServiceOperationException;

import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * @author zhichuang
 * @date 2023/4/7 0007
 **/

@CheckPlatformStatus
@Priority(220)
@Interceptor
@SuppressWarnings("unused") // Used by the framework
public class CheckPlatformStatusInterceptor {
  static final Logger LOG = Logger.getLogger("app.log");
  @Inject
  PlatformUtils platformUtils;
  @Inject
  OperationUtils operationUtils;
  @AroundInvoke
  Object checkPlatformStatus(InvocationContext context) {
    final CheckPlatformStatus checkPlatformStatus = context.getMethod().getAnnotation(CheckPlatformStatus.class);

    Parameter[] paramsName = context.getMethod().getParameters();
    Object[] paramsValues = context.getParameters();

    String requestId = Arrays.stream(paramsName)
            .filter(param -> "RequestId".equalsIgnoreCase(param.getName()))
            .findFirst()
            .map(param -> paramsValues[Arrays.asList(paramsName).indexOf(param)].toString())
            .orElse("CheckPlatformStatus");


    switch (checkPlatformStatus.type()) {
      case SPACE -> {
        var spaceStatus = platformUtils.isRegistryPlatformAvailable(requestId);
        if (!spaceStatus) {
          return ResponseBase.fromResponseBaseEnum(requestId, ResponseBaseEnum.SPACE_SERVICE_PLATFORM_ERROR).build();
        }
        if (checkPlatformStatus.isNeedCheckNetworkChannel() && Boolean.FALSE.equals(operationUtils.getEnableInternetAccess())) {
          return ResponseBase.fromResponseBaseEnum(requestId, ResponseBaseEnum.SPACE_SERVICE_PLATFORM_ERROR).build();
        }
      }
      case PRODUCT -> {
        var productStatus = platformUtils.isOpstagePlatformAvailable(requestId);
        if (!productStatus) {
          return ResponseBase.fromResponseBaseEnum(requestId, ResponseBaseEnum.PRODUCT_SERVICE_PLATFORM_ERROR).build();
        }
        if (checkPlatformStatus.isNeedCheckNetworkChannel() && Boolean.FALSE.equals(operationUtils.getEnableInternetAccess())) {
          return ResponseBase.fromResponseBaseEnum(requestId, ResponseBaseEnum.PRODUCT_SERVICE_PLATFORM_ERROR).build();
        }
      }
      default -> throw new ServiceOperationException(ServiceError.PLATFORM_TYPE_ERROR);
    }


    Object ret;
    try {
      ret = doSneakyThrowsInvoke(context);
    } catch (Exception rethrow) {
      LOG.errorv(rethrow,"[Throw] method: {0}(), exception");
      throw rethrow;
    }
    return ret;
  }

  @SneakyThrows
  Object doSneakyThrowsInvoke(InvocationContext context) {
    return context.proceed();
  }
}
