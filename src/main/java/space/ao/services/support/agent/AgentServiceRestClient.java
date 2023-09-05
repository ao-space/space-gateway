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

package space.ao.services.support.agent;

import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import space.ao.services.support.agent.info.DeviceInfo;
import space.ao.services.support.agent.info.DidDocResult;
import space.ao.services.support.agent.info.IpAddressInfo;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.support.log.Logged;

import jakarta.ws.rs.core.MediaType;
import java.util.List;

@RegisterRestClient(configKey = "system-agent-api")
public interface AgentServiceRestClient {
  @GET
  @Path("/device/localips")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  ResponseBase<List<IpAddressInfo>> getIpAddressInfo(@HeaderParam("Request-Id") @NotBlank String requestId);

  @GET
  @Path("/device/version")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Logged
  ResponseBase<DeviceInfo> getDeviceVersion();

  @GET
  @Path("/did/document")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Logged
  @Operation(description = "入参 did(可选)，aoId(可选)。如果传 did，则直接返回 did 对应的 didDoc; 如果不传 did，但是传了 aoid 则取出该用户的 didDoc 返回；如果都没传则报参数错误 AG-400。")
  ResponseBase<DidDocResult> getDidDocument(@HeaderParam("Request-Id") @NotBlank String requestId, @QueryParam("did") String did,
                              @QueryParam("aoId") String aoid);

  @PUT
  @Path("/did/document/method")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Logged
  @Operation(description = "入参 did(可选)，aoid(可选)。如果传 did，则直接返回 did 对应的 didDoc; 如果不传 did，但是传了 aoid 则取出该用户的 didDoc 返回；如果都没传则报参数错误 AG-400。")
  ResponseBase<DidDocResult> changePasswordDidDocument(@HeaderParam("Request-Id") @NotBlank String requestId, @QueryParam("did") String did,
                                                       @QueryParam("aoId") String aoid, @QueryParam("newPassword") String newPassword);
}
