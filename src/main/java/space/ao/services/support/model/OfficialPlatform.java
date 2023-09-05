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

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@AllArgsConstructor
public enum OfficialPlatform{
  PROD("https://ao.space", "ao.space"),
  PROD_SERVICES("https://services.ao.space", "ao.space"),
  RC("https://eulix.xyz",  "eulix.xyz"),
  RC_SERVICES("https://services.eulix.xyz",  "eulix.xyz"),

  DEV("https://dev.eulix.xyz", "dev-space.eulix.xyz"),
  DEV_SERVICES("https://dev-services.eulix.xyz", "dev-space.eulix.xyz"),

  QA("https://qa.eulix.xyz", "qa-space.eulix.xyz"),
  QA_SERVICES("https://qa-services.eulix.xyz", "qa-space.eulix.xyz"),

  SIT("https://sit.eulix.xyz", "sit-space.eulix.xyz"),
  SIT_SERVICES("https://sit-services.eulix.xyz", "sit-space.eulix.xyz"),

  TEST("https://test.eulix.xyz", "test-space.eulix.xyz"),
  TEST_SERVICES("https://test-services.eulix.xyz", "test-space.eulix.xyz"),

  ;

  @Getter
  private final String platformUrl;
  @Getter
  private final String userDomain;
  public static boolean isOfficialByPlatformUrl(String platformUrl){
    for (var url: OfficialPlatform.values()) {
      if(Objects.equals(url.platformUrl, platformUrl)){
        return true;
      }
    }
    return false;
  }
  public static boolean isOfficialByUserDomain(String userDomain){
    int index = userDomain.indexOf('.');
    if (index != -1) {
      userDomain = userDomain.substring(index + 1);
    }
    for (var url: OfficialPlatform.values()) {
      if(Objects.equals(url.userDomain, userDomain)){
        return true;
      }
    }
    return false;
  }

  public static OfficialPlatform getOfficialPlatformByPlatformUrl(String platformUrl){
    for (var url: OfficialPlatform.values()) {
      if(Objects.equals(url.platformUrl, platformUrl)){
        return url;
      }
    }
    return null;
  }

}

