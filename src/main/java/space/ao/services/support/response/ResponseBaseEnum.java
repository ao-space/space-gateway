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

package space.ao.services.support.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ResponseBaseEnum {
  NO_AUTO_LOGIN("GW-4044", "等待用户绑定端确认免扫码登录"),
  NO_AUTH("GW-4045", "没有被授权"),
  CANCEL_LOGIN("GW-4046", "用户绑定端取消登录"),
  RESOURCE_DEPLOY_FAILED("GW-5001", "资源部署失败"),
  //第三方应用错误码，统一用8开头
  DEV_OPTIONS_PARAMETER_ERRORS("GW-8001", "some paramsters error"),
  IMAGE_NOT_FOUND("GW-8404", "Image not found"),

  AUTH_CODE_NOT_MATCH("GW-4013", "auth-code was not matched! "),
  INVALID_USER("GW-4023", "user not exist"),

  SPACE_ID_NOT_UNIQUE("GW-4004", "personal space id is not unique"),

  SPACE_SERVICE_PLATFORM_ERROR("GW-5005", "space service platform connect error"),
  PRODUCT_SERVICE_PLATFORM_ERROR("GW-5006", "product service platform connect error"),

  ;

  @Getter
  private final String code;
  @Getter
  private final String message;
}
