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

package space.ao.services.support.platform;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import space.ao.services.support.platform.info.TrailUserRes;
import space.ao.services.gateway.version.CompatibleCheckRes;
import space.ao.services.gateway.version.PackageCheckRes;
import space.ao.services.support.log.Logged;

@Path("/v2/service")
@RegisterRestClient(configKey = "psplatform-api")
@Tag(name = "Product Platform Service",
        description = "Provides product platform related APIs.")
public interface PlatformOpstageServiceRestClient {


  @GET
  @Path("/packages/version/check")
  @Produces(MediaType.APPLICATION_JSON)
  PackageCheckRes check(@HeaderParam("Request-Id") String requestId,
      @QueryParam("action") String action,
      @QueryParam("app_pkg_name") String appName,
      @QueryParam("app_pkg_type") String appType,
      @QueryParam("cur_app_version") String curAppVersion,
      @QueryParam("box_pkg_name") String boxName,
      @QueryParam("box_pkg_type") String boxType,
      @QueryParam("cur_box_version") String curBoxVersion,
      @QueryParam("version_type") String versionType
  );


  @GET
  @Path("/packages/compatibility/check")
  @Produces(MediaType.APPLICATION_JSON)
  CompatibleCheckRes compatible(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
      @NotBlank @Parameter(required = true) @QueryParam("app_pkg_name") String appPkgName,
      @Parameter(required = true, schema = @Schema(enumeration = {"android", "ios"}))
      @QueryParam("app_pkg_type") String appPkgType,
      @NotBlank @Parameter(required = true) @QueryParam("box_pkg_name") String boxPkgName,
      @Parameter(required = true, schema = @Schema(enumeration = {"box"}))
      @QueryParam("box_pkg_type") String boxPkgType,
      @NotNull @Pattern(regexp = "[a-zA-Z\\d.-]{0,50}") @QueryParam("cur_box_version") String curBoxVersion,
      @NotNull @Pattern(regexp = "[a-zA-Z\\d.-]{0,50}") @QueryParam("cur_app_version") String curAppVersion,
      @QueryParam("version_type") String versionType
  );

  @GET
  @Path("/status")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description = "查询空间平台状态")
  PlatformStatusResult status(@NotBlank @HeaderParam("Request-Id") String requestId);


  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Logged
  @Path("/trail/boxuser")
  @Operation(description = "根据用户userid查询注册信息")
  TrailUserRes trailUser(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                         @NotNull @HeaderParam("Box-Reg-Key") String boxRegKey,
                         @Parameter(description = "试用用户类型,online/pc/pc_open")  @QueryParam("type") String type,
                         @NotNull @QueryParam("box_uuid") String boxUUID,
                         @QueryParam("user_id") String userId);

}
