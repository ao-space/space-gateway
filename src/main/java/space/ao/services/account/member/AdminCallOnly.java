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

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 该注解用于标示和拦截基于网关 call 调用的的请求 method，使其调用仅限
 * 于管理员。
 */
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
@Documented
@Inherited
@InterceptorBinding
public @interface AdminCallOnly {
  /**
   * 用户 id 在参数列表的位置（index），默认为 1。
   * @return 返回用户 id 在参数列表中的位置（index）。
   */
  int userIdAt() default 1;
}
