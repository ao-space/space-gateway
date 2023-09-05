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

package space.ao.services.account.personalinfo.dto;

import jdk.jfr.Description;
import org.jboss.resteasy.reactive.PartType;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import java.io.InputStream;

/**
 * 上传的成员头像信息
 * @author suqin
 * @date 2021-10-08 21:39:57
 **/
public class MultiPersonalInfo {
  @FormParam("param")
  @Description("文件参数,json格式，包括filename，例如{'filename':'profile.png'}")
  @PartType(MediaType.APPLICATION_JSON)
  public @NotNull String param;

  @FormParam("file")
  @Description("文件的二进制流")
  @PartType(MediaType.APPLICATION_OCTET_STREAM)
  public @NotNull InputStream inputStream;
}
