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

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to mark a class or method to be intercepted with logging function.
 *
 * @see LoggingInterceptor
 * @since 0.1.0
 * @author Haibo Luo
 */
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
@Documented
@Inherited
@InterceptorBinding
public @interface Logged {
  /**
   * Used to check that if it's enable to log pre execution information.
   * @return the indicator of pre execution logging.
   */
  @Nonbinding boolean enablePreLog() default true;

  /**
   * Used to check that if it's enable to log after execution information.
   * @return the indicator of after execution logging.
   */
  @Nonbinding boolean enableAfterLog() default true;
}
