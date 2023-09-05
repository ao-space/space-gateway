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
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data(staticConstructor = "of")
@Schema(description = "软件包信息")
public class PackageRes {
    // 软件包名称
    @Schema(description = "软件包标识符")
    private final String pkgName;

    // 软件包类型 ios、android、box
    @Schema(description = "软件包类型", enumeration = {"android", "ios", "box"})
    private final String pkgType;

    // 版本号 长度0-20个字符
    @Schema(description = "软件包版本")
    private final String pkgVersion;

    // 版本文件大小(字节)，最大10GB
    private final Long pkgSize;

    // 下载url
    private final String downloadUrl;

    // 更新文案/版本特性 长度0-10000个字符
    @Schema(description = "版本特性")
    private final String updateDesc;

    // md5
    private final String md5;

    // 是否强制更新 1-强制更新;0-可选更新
    @Schema(description = "是否强制更新")
    private final Boolean isForceUpdate;

    @Schema(description = "兼容的最小App版本,用于box版本")
    private final String minAndroidVersion;

    @Schema(description = "兼容的最小App版本,用于box版本")
    private final String minIOSVersion;

    // 所需的最小盒子版本
    @Schema(description = "所需的最小盒子版本,用于app版本")
    private final String minBoxVersion;

    @Schema(description = "是否需要重启")
    private final Boolean restart;
}
