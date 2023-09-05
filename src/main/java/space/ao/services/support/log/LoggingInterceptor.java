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

package space.ao.services.support.log;

import com.google.common.base.Stopwatch;
import lombok.SneakyThrows;
import org.jboss.logging.Logger;
import space.ao.services.support.OperationUtils;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.Arrays;

/**
 * Provides the logging joint point to intercept the invocation of method or class
 * which is marked by the annotation {@code Logged}. For more information:
 * <a href="https://quarkus.io/guides/cdi#interceptors">https://quarkus.io/guides/cdi#interceptors</a>.
 *
 * @see Logged
 * @since 0.1.0
 * @author Haibo Luo
 */
@Logged
@Priority(200)
@Interceptor
@SuppressWarnings("unused") // Used by the framework
public class LoggingInterceptor {

  static final Logger LOG = Logger.getLogger("app.log");

  @Inject
  OperationUtils utils;

  @AroundInvoke
  Object logInvocation(InvocationContext context) {
    final Logged logged = context.getMethod().getAnnotation(Logged.class);
    final String mn = context.getMethod().getName();

    if (logged.enablePreLog()) {
      LOG.infov(
          "[Invoke] method: {0}(), parameters: {1}",
          mn,
          Arrays.toString(context.getParameters())
      );
    }

    Stopwatch sw = Stopwatch.createStarted();
    Object ret;
    try {
      ret = doSneakyThrowsInvoke(context);
    } catch (Exception rethrow) {
      LOG.errorv(
          rethrow,
          "[Throw] method: {0}(), exception",
          mn
      );
      throw rethrow;
    } finally {
      sw.stop();
    }

    if (logged.enableAfterLog()) {
      LOG.infov(
          "[Return] method: {0}(), result: {1}, elapsed: {2}",
          mn,
          ret,
          sw
      );
    }
    return ret;
  }

  @SneakyThrows
  Object doSneakyThrowsInvoke(InvocationContext context) {
    return context.proceed();
  }
}
