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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.HeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.deviceinfo.dto.DeviceStorageInfo;

@RegisterRestClient(configKey="device-storage-api")
public interface DeviceStorageService {
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  ResponseBase<DeviceStorageInfo> getStorageInfo(@HeaderParam("Request-Id") @NotBlank String requestId);
}
