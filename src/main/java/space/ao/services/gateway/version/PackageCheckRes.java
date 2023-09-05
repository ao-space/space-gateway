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

package space.ao.services.gateway.version;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@NoArgsConstructor
public class PackageCheckRes {
    // 是否存在更新
    @Schema(description = "是否存在更新")
    private Boolean newVersionExist = false;

    // app是否需要关联更新
    @Schema(description = "app是否需要关联更新")
    private Boolean isAppNeedUpdate = false;

    // box是否需要关联更新
    @Schema(description = "box是否需要关联更新")
    private Boolean isBoxNeedUpdate = false;

    // 最新app软件包信息
    private PackageRes latestAppPkg;

    // 最新box软件包信息
    private PackageRes latestBoxPkg;
}
