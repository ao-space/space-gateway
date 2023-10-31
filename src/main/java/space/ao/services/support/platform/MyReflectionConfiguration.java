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

import io.github.ren2003u.authentication.model.ObtainBoxRegKeyRequest;
import io.github.ren2003u.authentication.model.ObtainBoxRegKeyResponse;
import io.github.ren2003u.migration.model.*;
import io.github.ren2003u.register.model.*;
import io.quarkus.runtime.annotations.RegisterForReflection;
import space.ao.services.account.member.dto.migration.ClientMigrationInfo;

@RegisterForReflection(targets = {RegisterClientRequest.class, RegisterClientResponse.class, RegisterDeviceRequest.class, RegisterDeviceResponse.class, RegisterUserRequest.class, RegisterUserResponse.class,
ClientMigrationInfo.class, SpacePlatformMigrationOutRequest.class, SpacePlatformMigrationOutResponse.class, SpacePlatformMigrationRequest.class, SpacePlatformMigrationResponse.class, UserDomainRouteInfo.class, UserMigrationInfo.class,
ObtainBoxRegKeyRequest.class, ObtainBoxRegKeyResponse.class})
public class MyReflectionConfiguration {
}
