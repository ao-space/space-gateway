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

package space.ao.services.account.member;

import org.jboss.logging.Logger;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.ForbiddenException;

/**
 * 该拦截器用于拦截和验证被 {@link AdminCallOnly} 标识的方法是否
 * 具有管理用户 id 的参数，如果是非管理的用户 id 则直接抛出 {@link ForbiddenException}
 * 的异常。
 */
@AdminCallOnly
@Interceptor
@SuppressWarnings("unused") // Used by the framework
public class AdminCallOnlyInterceptor {

  private static final String ADMIN_ID = "1";

  private static final Logger LOG = Logger.getLogger("app.log");

  @AroundInvoke
  Object logInvocation(InvocationContext ctx) throws Exception {
    int at = ctx.getMethod().getAnnotation(AdminCallOnly.class).userIdAt();
    String userId = (String) ctx.getParameters()[at];
    if (!ADMIN_ID.equals(userId)) {
      throw new ForbiddenException("It is for administrator use only");
    }
    return ctx.proceed();
  }
}
