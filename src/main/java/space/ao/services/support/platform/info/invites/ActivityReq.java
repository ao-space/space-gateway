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
import space.ao.services.support.validator.ValueOfEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

@Data(staticConstructor = "of")
public class ActivityReq {
    @Schema(description = "name")
    @NotBlank
    private String name;

    @Schema(description = "活动规则")
    @NotBlank
    private String content;

    @Schema(description = "活动封面地址")
    private String imageUrl;

    @Schema(description = "活动奖励，元素1为第1档，元素2为第2档，元素3为第3档")
    private List<Reword> rewords;

    @Schema(description = "活动类型,trial/proposal/questionnaire")
    @ValueOfEnum(enumClass = ActivityTypeEnum.class, valueMethod = "getName")
    @NotNull
    private String type;

    @Schema(description = "开始时间")
    private OffsetDateTime startAt;

    @Schema(description = "结束时间")
    private OffsetDateTime endAt;

    @Data(staticConstructor = "of")
    public static class Reword {
        @Schema(description = "阈值")
        private Integer threshold;

        @Schema(description = "奖品名称")
        private String rewardName;

    }
}
