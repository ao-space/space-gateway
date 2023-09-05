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

package space.ao.services.support.platform.info.ability;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
public enum PlatformApiTypeEnum {
    BASIC_API("basic_api", "空间平台基础api"),
    EXTENSION_API("extension_api", "空间平台扩展api"),
    PRODUCT_SERVICE_API("product_service_api", "产品服务api"),
    ;

    @Getter
    private final String name;

    @Getter
    private final String desc;

    public static PlatformApiTypeEnum fromValue(String value) {
        return Arrays.stream(values()).filter(apiType -> apiType.getName().equals(value)).findFirst().orElseThrow();
    }
}
