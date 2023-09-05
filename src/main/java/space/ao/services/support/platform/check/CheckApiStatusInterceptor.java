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
import space.ao.services.support.platform.PlatformUtils;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;

@CheckApiStatus
@Priority(220)
@Interceptor
@SuppressWarnings("unused") // Used by the framework
public class CheckApiStatusInterceptor {
  static final Logger LOG = Logger.getLogger("app.log");
  @Inject
  PlatformUtils platformUtils;
  @AroundInvoke
  Object checkApiStatus(InvocationContext context) {
    final CheckApiStatus checkApiStatus = context.getMethod().getAnnotation(CheckApiStatus.class);

    Parameter[] paramsName = context.getMethod().getParameters();
    Object[] paramsValues = context.getParameters();

    String requestId = Arrays.stream(paramsName)
            .filter(param -> "Request-Id".equals(param.getName()))
            .findFirst()
            .map(param -> paramsValues[Arrays.asList(paramsName).indexOf(param)].toString())
            .orElse("CheckPlatformStatus");
    if(Objects.isNull(platformUtils.getPlatformApis())){
      platformUtils.queryPlatformAbility();
    }
    var serviceName = platformUtils.getPlatformApis().getServices().get(checkApiStatus.serviceName());
    if(Objects.isNull(serviceName)){
      LOG.errorv("[Throw] method: {0}(), exception: {1}, requestId: {2}", context.getMethod().getName(),
              "Service not found", requestId);
      return null;
    }
    if(!serviceName.containsKey(checkApiStatus.apiName())){
      LOG.errorv("[Throw] method: {0}(), exception: {1}, requestId: {2}", context.getMethod().getName(),
              "Api not found", requestId);
      return null;
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
