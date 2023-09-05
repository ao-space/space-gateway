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

/*
 * Copyright (c) 2023 Institute of Software Chinese Academy of Sciences (ISCAS)
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

package space.ao.services.support.platform.info.invites;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;

@Data(staticConstructor = "of")
public class TrailInviteRes {
	@Schema(description = "id")
	private Long id;

	@Schema(description = "活动id")
	private Long activityId;

	@Schema(description = "email")
	private String email;

	@Schema(description = "boxUUID")
	private String boxUUID;

	@Schema(description = "userId")
	private String userId;

	@Schema(description = "试用邀请码")
	private String inviteCode;

	@Schema(description = "试用邀请链接")
	private String inviteParam;

	// 0-未使用;1-已使用
	@Schema(description = "邀请状态，0-未使用;1-已使用")
	private Integer state;

	@Schema(description = "超时时间")
	private OffsetDateTime expiresAt;

	@Schema(description = "创建时间")
	private OffsetDateTime createAt;

	@Schema(description = "更新时间")
	private OffsetDateTime updateAt;
}
