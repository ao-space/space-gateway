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

package space.ao.services.account.security.utils.token;

import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;

/**
 * 第一步验证返回的 token. (第二步验证时请求需要带上, gateway 做校验.)
 */
@Builder
@Value
public class SecurityToken {
    SecurityTokenType tokenType;
    String clientUuid;
    boolean accept;
    ZonedDateTime expiredAt;
}
