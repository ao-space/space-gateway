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

package space.ao.services.account.backupandrestore.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import space.ao.services.account.member.service.MemberManageService;
import space.ao.services.support.log.Logged;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.account.personalinfo.entity.UserEntity;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/v1/api")
@Tag(name = "Backup and restore service", description = "Provides account backup and restore.")
public class BackupAndRestoreResource {

  @Inject
  MemberManageService memberManageService;

  /**
   * 空间成员创建接口。
   *
   * @return 空间成员创建结果。
   * @since 0.3.0
   */
  @GET
  @Logged
  @Path("/accountinfo")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  @Operation(description = "Tries to get all member info. Provide only for file service")
  public ResponseBase<List<UserEntity>> getAccountMember(@Valid @NotBlank @HeaderParam("Request-Id") String requestId){
    List<UserEntity> userList = memberManageService.findAll().list();
    return ResponseBase.okACC(requestId, userList);
  }
}
