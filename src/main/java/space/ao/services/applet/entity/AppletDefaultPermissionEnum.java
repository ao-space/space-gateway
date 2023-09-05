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

package space.ao.services.applet.entity;

import lombok.Getter;

public enum AppletDefaultPermissionEnum {
	ADDRESSBOOK("e5878406cbdbef46", "通讯录",true),
	;

	@Getter
	private final String appletId;

	@Getter
	private final String appletName;

	@Getter
	private final Boolean permission;


	AppletDefaultPermissionEnum(String appletId, String name, Boolean permission) {
		this.appletId = appletId;
		this.appletName = name;
		this.permission = permission;
	}

	public static AppletDefaultPermissionEnum valueFrom(String appletId) {
		for(AppletDefaultPermissionEnum e: AppletDefaultPermissionEnum.values()){
			if (appletId.matches(e.getAppletId())) {
				return e;
			}
		}
		return null;
	}
}
