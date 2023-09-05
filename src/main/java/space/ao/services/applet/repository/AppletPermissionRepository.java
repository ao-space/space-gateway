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

package space.ao.services.applet.repository;


import io.quarkus.hibernate.orm.panache.PanacheRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import space.ao.services.applet.entity.AppletPermissionEntity;

@ApplicationScoped
public class AppletPermissionRepository implements PanacheRepository<AppletPermissionEntity> {

	public AppletPermissionEntity findByAoidAndAppletid(String aoid, String appletId) {
		return this.find("aoId=?1 and appletId=?2", aoid, appletId).firstResult();
	}

	@Transactional
	public void updateAppletPermission(String aoid, String appletId, Boolean permission) {
		this.update("set permission=?1 where aoId=?2 and appletId=?3", permission, aoid, appletId);
	}

	@Transactional
	public void setAppletPermission(String aoid, String appletId, Boolean permission) {
		AppletPermissionEntity entity = new AppletPermissionEntity();
		entity.setPermission(permission);
		entity.setAppletId(appletId);
		entity.setAoId(aoid);
		this.persist(entity);
	}

}
