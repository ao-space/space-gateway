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

package space.ao.services.gateway;

import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.MediaType;
import java.io.InputStream;

public class MultipartRequest {

  @RestForm("file")
  @PartType(MediaType.APPLICATION_OCTET_STREAM)
  public @NotNull InputStream file; // encrypted by secret key

  @RestForm("callRequest")
  @PartType(MediaType.APPLICATION_OCTET_STREAM)
  public @NotNull InputStream callRequest;
}