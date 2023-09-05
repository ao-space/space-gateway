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

package space.ao.services.account.member.service;

import io.quarkus.runtime.Startup;
import space.ao.services.applet.repository.AppletPermissionRepository;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Startup
public class DevOptionsService {

  @Inject
  AppletPermissionRepository permissions;

  private static final String APP_ID = "DEV-OPTIONS-SPECIFIC-APP-ID-XYZ";
  private static final String AO_ID = "aoid-1";

  @PostConstruct
  public void init() {
    var p = permissions.findByAoidAndAppletid(AO_ID, APP_ID);
    if (p == null) {
      permissions.setAppletPermission(AO_ID, APP_ID, false);
    }
  }

  public String getPermissionStatus() {
    var p = permissions.findByAoidAndAppletid(AO_ID, APP_ID);
    return p.getPermission() ? "on" : "off";
  }

  public void setPermissionStatus(String status) {
    permissions.updateAppletPermission(AO_ID, APP_ID, "ON".equalsIgnoreCase(status));
  }

}
