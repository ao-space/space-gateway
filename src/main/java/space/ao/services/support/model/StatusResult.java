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

package space.ao.services.support.model;

import lombok.Data;

/**
 * Used to define a REST response for querying the server {@code status}.
 */
@Data(staticConstructor = "of")
public class StatusResult {
  private final String status;
  private final String version;
  private final String message;
  private final PlatformInfo platformInfo;
  @Data
  public static class PlatformInfo{
    private final String platformUrl;
    private final boolean official;
    public static PlatformInfo of(String platformUrl) {
      return new PlatformInfo(platformUrl, OfficialPlatform.isOfficialByPlatformUrl(platformUrl));
    }

  }
}
