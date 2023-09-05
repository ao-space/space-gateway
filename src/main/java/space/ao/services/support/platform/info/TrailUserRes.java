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

package space.ao.services.support.platform.info;

import jakarta.persistence.Column;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;

@Data
public class TrailUserRes {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "邮箱")
    private String email;

    private String phoneNumber;

    @Schema(description = "姓名")
    private String userName;

    private String address;

    private Long spaceId;

    private Long memberId;

    @Schema(description = "邀请链接")
    private String inviteParam;

    @Schema(description = "试用用户类型,online/pc")
    private String type;

    @Schema(description = "是否订阅")
    private Boolean isSubscribed;

    @Schema(description = "试用用户阶段,trial/test/official")
    private String stage;

    @Schema(description = "0-正常;1-禁用;2-已过期")
    private Integer state;

    @Column(name = "extra")
    private String extra;

    @Schema(description = "创建时间")
    private OffsetDateTime createAt;

    @Schema(description = "更新时间")
    private OffsetDateTime updateAt;

    @Schema(description = "过期时间")
    private OffsetDateTime expiresAt;
}
