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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.quarkus.test.Mock;

import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import lombok.Builder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import space.ao.services.gateway.version.CompatibleCheckRes;
import space.ao.services.gateway.version.PackageCheckRes;
import space.ao.services.support.platform.info.TrailUserRes;

@Mock
@ApplicationScoped
@RestClient
@SuppressWarnings("unused") // test uses this mocked class
public class MockPlatformOpstageServiceRestClient implements PlatformOpstageServiceRestClient {

  @Override
  public PackageCheckRes check(String requestId, String action, String appName, String appType,
      String curAppVersion, String boxName, String boxType, String curBoxVersion, String versionType) {
    PackageCheckRes packageCheckRes = new PackageCheckRes();
    packageCheckRes.setNewVersionExist(true);
    return packageCheckRes;
  }

  @Override
  public CompatibleCheckRes compatible(String requestId, String appPkgName, String appPkgType,
      String boxPkgName, String boxPkgType, String curBoxVersion, String curAppVersion, String versionType) {
    CompatibleCheckRes compatibleCheckRes = CompatibleCheckRes.of();
    compatibleCheckRes.setIsAppForceUpdate(true);
    return compatibleCheckRes;
  }

  @Override
  public PlatformStatusResult status(String requestId) {
    return PlatformStatusResult.of("ok", "1.0.0");
  }




  @Override
  public TrailUserRes trailUser(@NotBlank @HeaderParam("Request-Id") String requestId,
                                @NotNull @HeaderParam("Box-Reg-Key") String boxRegKey,
                                @QueryParam("type") String type,
                                @NotNull @QueryParam("box_uuid") String boxUUID, @QueryParam("user_id") String userId){
    var res = new TrailUserRes();
    res.setEmail("test@qq.com");
    return res;
  }

  @Builder
  record ClientInfo(String title, String description, String iconUrl, String appletId, String appletVersion,
                    String appletSecret, List<String> categories) {
  }

  // <appletid_ver, info>
  private static final ImmutableMap<String, ClientInfo> DB = ImmutableMap.<String, ClientInfo>builder()
      .put(
          "A1_v1",
          ClientInfo.builder()
              .appletId("A1")
              .appletVersion("v1")
              .appletSecret("S1")
              .categories(
                  ImmutableList.of("userinfo-readonly", "addressbook"))
              .build()
      )
      .build();
}
