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
package space.ao.services.support.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.security.inf.SecurityProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhichuang
 * @date 2023/7/25 0025
 **/
@ApplicationScoped
public class SecurityProviderFactory {

  private static final String PROVIDER_NAME = "default";
  private static final Map<String, SecurityProvider> map = new ConcurrentHashMap<>();

  @Inject
  ApplicationProperties properties;

  public static void putSecurityProvider(String name, SecurityProvider securityProvider){
    map.put(name, securityProvider);
  }

  @Produces
  public SecurityProvider getSecurityProvider() {
    String securityMode = properties.securityMode();
    if (map.containsKey(securityMode)) {
      return map.get(securityMode);
    } else {
      return map.get(PROVIDER_NAME);
    }
  }
}
