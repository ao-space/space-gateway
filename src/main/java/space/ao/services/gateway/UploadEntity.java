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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Used to define the upload related necessary parameters. It will be
 * wrapped as entity of {@link RealCallRequest} and used by {@link GatewayResource#upload}.
 */
@Data
@NoArgsConstructor
public class UploadEntity {
  @JsonCreator
  public UploadEntity(@JsonProperty("filename") String filename,
                      @JsonProperty("mediaType") String mediaType) {
    this.filename = filename;
    this.mediaType = mediaType;
  }

  private String filename;
  private String mediaType;
}
