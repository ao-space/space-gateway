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

package space.ao.services.vod;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import space.ao.services.support.response.ResponseBase;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Objects;

import static space.ao.services.support.RestConfiguration.REQUEST_ID;

@Tag(name = "Space Gateway Video On Demand Service",
        description = "Provides Video On Demand APIs.")
@Path("/v1/api/vod")
public class VodResource {
  @Inject
  VodService vodService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description =
          "check vod coding status")
  @Path("/check")
  public ResponseBase<Object> check(@Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId,
                                            @Valid @NotBlank @QueryParam("userId") String userid,
                                            @Valid @NotBlank @QueryParam("uuid") String fileUUID) {
    var m3u8Info = vodService.getM3U8Info(requestId, Long.valueOf(userid), fileUUID);
    if(Objects.equals(m3u8Info, "1011")) {
      return ResponseBase.builder().code("VOD-404").requestId(requestId).message("file not exits").build();
    } else if(Objects.equals(m3u8Info, "1002")) {
      return ResponseBase.builder().code("VOD-400").requestId(requestId).message("Field validation for file uuid").build();
    } else if(m3u8Info.contains("segment-1-v1-a1.m4s")){
      return ResponseBase.builder().code("VOD-4001").requestId(requestId).message("The H265 encoding format is not supported").build();
    } else {
      return ResponseBase.builder().code("VOD-200").requestId(requestId).message("OK").build();
    }

  }

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description =
          "get vod m3u8 file")
  @Path("/m3u8")
  public Response get(@Valid @NotBlank @HeaderParam(REQUEST_ID) String requestId,
                      @Valid @NotBlank @QueryParam("userId") String userid,
                      @Valid @NotBlank @QueryParam("uuid") String fileUUID) {
    return vodService.getM3U8(requestId, Long.valueOf(userid), fileUUID);
  }
}
