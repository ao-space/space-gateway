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

import lombok.Builder;
import lombok.Value;

import javax.crypto.spec.IvParameterSpec;
import java.time.ZonedDateTime;
import java.util.Set;

@Builder
@Value
public class AccessToken {
  public static final String SHARED_SECRET_KEY = "sharedSecret";
  public static final String SHARED_IV_KEY = "sharedInitializationVector";
  public static final String USER_ID = "userId";
  public static final String CLIENT_UUID = "clientUUID";
  public static final String AK_CLIENT_UUID = "AccessToken-clientUUID";

  public static final String JWT_ID = "jti";

  String userId;
  String clientUUID;
  String endpoint;
  String sharedSecret;
  IvParameterSpec sharedInitializationVector;
  ZonedDateTime expiresAt;
  String openApiAppletId;
  String openApiAppletVersion;
  Set<String> openApiScopes;
  boolean openApi; // judge if it is an open api token
  String token; // the original string based access token
}
