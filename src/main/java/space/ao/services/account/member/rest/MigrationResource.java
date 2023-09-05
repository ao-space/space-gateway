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

package space.ao.services.account.member.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import space.ao.services.account.member.service.MigrationService;
import space.ao.services.support.log.Logged;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.member.dto.migration.BoxMigrationInfo;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/v1/api")
@Tag(name = "Member manage service", description = "Provides member migration requests.")
public class MigrationResource {

    @Inject
    MigrationService migrationService;

    @GET
    @Logged
    @Path("/user/migration")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "获取 migration 信息")
    public ResponseBase<BoxMigrationInfo> migration(@Valid @NotBlank @HeaderParam("Request-Id") String requestId) {
        return ResponseBase.okACC(requestId, migrationService.getMigrationInfo(requestId));
    }

    @PUT
    @Logged
    @Path("/user/migration")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "migration 更新域名信息")
    public ResponseBase<BoxMigrationInfo> migrationRoutes(@Valid @NotBlank @HeaderParam("Request-Id") String requestId,
                                                @Valid BoxMigrationInfo boxMigrationInfo) {
        return migrationService.updateUserInfos(boxMigrationInfo, requestId);
    }

}
