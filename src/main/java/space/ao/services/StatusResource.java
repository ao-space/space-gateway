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

package space.ao.services;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.log.Logged;
import space.ao.services.support.model.StatusResult;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Tag(name = "Space Gateway Status Service",
    description = "Provides gateway service status related APIs.")
@Path("/status")
public class StatusResource {

    @Inject
    ApplicationProperties properties;
    @GET
    @Logged
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Try to ask the current status of server.")
    public StatusResult status() {
        return StatusResult.of("ok", properties.version(), "I am good.", StatusResult.PlatformInfo.of(
                properties.ssplatformUrl()
        ));
    }
}
