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

package space.ao.services.support.agent.info;

import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class DeviceInfo {
  private DeviceAbility deviceAbility;
  private String deviceLogoUrl;
  private String deviceName;
  private String deviceNameEn;
  private String generationEn;
  private String generationZh;
  private String osVersion;
  private String productModel;
  private List<ServiceDetail> serviceDetail;
  private List<ServiceVersion> serviceVersion;
  private String snNumber;
  private String spaceVersion;

  @Data
  static class DeviceAbility {
    private int deviceModelNumber;
    private boolean innerDiskSupport;
    private boolean securityChipSupport;
    private String snNumber;
    private boolean supportUSBDisk;
  }

  @Data
  static class ServiceDetail {
    private int containers;
    private int created;
    private String id;
    private Map<String, String> labels;
    private String parentId;
    private List<String> repoDigests;
    private String repoTag;
    private List<String> repoTags;
    private int sharedSize;
    private int size;
    private int virtualSize;
  }

  @Data
  static class ServiceVersion {
    private int created;
    private String serviceName;
    private String version;
  }
}




