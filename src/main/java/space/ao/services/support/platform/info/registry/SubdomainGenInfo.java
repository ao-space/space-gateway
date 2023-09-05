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

package space.ao.services.support.platform.info.registry;

import jakarta.validation.constraints.Max;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record SubdomainGenInfo(@Schema(description = "有效期，单位秒，最长7天") @Max(604800) Integer effectiveTime) {
  public static SubdomainGenInfo of(Integer effectiveTime) {
    return new SubdomainGenInfo(effectiveTime);
  }
}

