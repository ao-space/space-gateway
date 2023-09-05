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

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.gateway.version.dto.PkgVersionTypeEnum;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.RestConfiguration;
import space.ao.services.support.StringUtils;
import space.ao.services.support.log.Logged;
import space.ao.services.support.platform.PlatformOpstageServiceRestClient;
import space.ao.services.support.platform.check.CheckPlatformStatus;
import space.ao.services.support.platform.check.PlatformTypeEnum;
import space.ao.services.support.response.ResponseBase;

@Path("/v1/api/gateway")
@Tag(name = "Space Gateway Version-checking Service",
    description = "提供 app、box 版本查询相关接口.")
public class VersionResource {

  @Inject
  @RestClient
  PlatformOpstageServiceRestClient platformOpstageServiceRestClient;

  @Inject
  ApplicationProperties properties;

  @Inject
  OperationUtils utils;

  /**
   * @param appName appName
   * @param appType ios、android
   * @param version 版本
   * @return 检查结果
   */
  @GET
  @Logged
  @Path("/version/app")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(description =
      "app 版本检查， 转发平台侧 /v2/service/packages/version/check")
  @CheckPlatformStatus(type = PlatformTypeEnum.PRODUCT, isNeedCheckNetworkChannel = false)
  public ResponseBase<PackageCheckRes> app(@Valid @NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId,
                                           @Valid @NotBlank @QueryParam("appName") String appName,
                                           @Valid @NotBlank @QueryParam("appType") String appType,
                                           @Valid @NotBlank @QueryParam("version") String version,
                                           @QueryParam("version_type") String versionType) {
    if(StringUtils.isBlank(versionType)){
      versionType = PkgVersionTypeEnum.OPEN_SOURCE.getName();
    }
    PackageCheckRes packageCheckRes;
    packageCheckRes = platformOpstageServiceRestClient.check(requestId, "app_check", appName,
          appType, version, properties.boxName(), properties.boxType(), utils.getBoxVersion(), versionType);

    return ResponseBase.ok(requestId, packageCheckRes).build();
  }

  @GET
  @Logged
  @Path("/version/box")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(description =
      "box 版本检查， 转发平台侧 /v2/service/packages/version/check")
  @CheckPlatformStatus(type = PlatformTypeEnum.PRODUCT, isNeedCheckNetworkChannel = false)
  public ResponseBase<PackageCheckRes> box(@Valid @NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId,
                                           @Valid @NotBlank @QueryParam("appName") String appName,
                                           @Valid @NotBlank @QueryParam("appType") String appType,
                                           @Valid @NotBlank @QueryParam("version") String version) {
    PackageCheckRes packageCheckRes;
    packageCheckRes = platformOpstageServiceRestClient.check(requestId, "box_check", appName,
          appType, version, properties.boxPkgName(), properties.boxType(), utils.getBoxVersion(),
            PkgVersionTypeEnum.OPEN_SOURCE.getName());
    return ResponseBase.ok(requestId, packageCheckRes).build();
  }


  @GET
  @Logged
  @Path("/version/compatible")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(description =
      "app、box版本兼容性 转发平台侧 /v2/service/packages/compatibility/check")
  @CheckPlatformStatus(type = PlatformTypeEnum.PRODUCT, isNeedCheckNetworkChannel = false)
  public ResponseBase<CompatibleCheckRes> compatible(@Valid @NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId,
                                                     @Valid @NotBlank @QueryParam("appName") String appName,
                                                     @Valid @NotBlank @QueryParam("appType") String appType,
                                                     @Valid @NotBlank @QueryParam("version") String version) {
    CompatibleCheckRes packageCheckRes;
    packageCheckRes = platformOpstageServiceRestClient.compatible(requestId, appName,
        appType,  properties.boxPkgName(), properties.boxType(), utils.getBoxVersion(), version, PkgVersionTypeEnum.OPEN_SOURCE.getName());

    return ResponseBase.ok(requestId, packageCheckRes).build();
  }

  @GET
  @Logged
  @Path("/version/box/current")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(description =
      "获取 box 当前版本")
  public ResponseBase<String> box(@Valid @NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId) {
    return ResponseBase.ok(requestId, utils.getBoxVersion()).build();
  }
}
