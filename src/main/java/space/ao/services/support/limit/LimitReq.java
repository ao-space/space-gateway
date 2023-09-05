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
 * 请求频率限制的注解
 * 使用方式是在需要限制的 restful 接口函数上增加注解，示例如下，
 * <p>
 * > @POST
 * > @Path("/verify")
 * > @LimitReq(keyPrefix="SCREQRATE-")
 * <p>
 * 上述 LimitReq 注解表示此接口在 interval() 只允许调用 max() 次，超过最大限制将返回 http code 204 给客户端。
 * keyPrefix() 用于存储改接口请求次数的 key 的前缀。整个 key 会以 keyPrefix()-PATH 的形式存储到 redis。其中 PATH 是完成的请求路径.
 *
 */
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
@Documented
@Inherited
@InterceptorBinding
public @interface LimitReq {
    /**
     * @return key的前缀
     */
    @Nonbinding String keyPrefix() default "";

    /**
     * @return 多久的间隔(秒)
     */
    @Nonbinding int interval() default 600;

    /**
     * @return 最大允许的请求次数
     */
    @Nonbinding int max() default 300;
}
