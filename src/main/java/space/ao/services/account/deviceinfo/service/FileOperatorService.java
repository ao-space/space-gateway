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

package space.ao.services.account.deviceinfo.service;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.deviceinfo.dto.UserStorageInfo;

@RegisterRestClient(configKey="file-api")
public interface FileOperatorService {
  @GET
  @Path("/user/storage")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  ResponseBase<UserStorageInfo> getUserStorageInfo(@HeaderParam("Request-Id") @NotBlank String requestId,
                                                   @Valid @NotBlank @QueryParam("userId") String userid,
                                                   @Valid @NotBlank @QueryParam("targetUserId") String targetUserId);


  @POST
  @Path("/user/init")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  ResponseBase<String> fileUserInitial(@HeaderParam("Request-Id") @NotBlank String requestId,
                                       @Valid @NotNull @QueryParam("userId") Long userid,
                                       @QueryParam("spaceLimit") Long spaceLimit);

  @POST
  @Path("/user/delete")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  ResponseBase<String> fileUserDelete(@HeaderParam("Request-Id") @NotBlank String requestId,
                                      @Valid @NotBlank @QueryParam("userId") String userid,
                                         String targetUserId);
}
