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

package space.ao.services.support.platform;

import io.quarkus.test.Mock;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.platform.info.ServiceEnum;
import space.ao.services.support.platform.info.token.TokenCreateResults;
import space.ao.services.support.platform.info.token.TokenInfo;
import space.ao.services.support.platform.info.token.TokenResult;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.OffsetDateTime;
import java.util.ArrayList;

@Mock
@ApplicationScoped
@RestClient
@SuppressWarnings("unused") // test uses this mocked class
public class MockPlatformOpstageBoxRegKeyServiceRestClient implements PlatformOpstageBoxRegKeyServiceRestClient {

  static final Logger LOG = Logger.getLogger("app.log");

  private static final String boxRegKey = "box-Reg-Key";
  @Inject
  ApplicationProperties properties;
  @Inject
  OperationUtils utils;
  @Override
  public TokenCreateResults createTokens(TokenInfo tokenInfo, String reqId) {
    var tokenResults = new ArrayList<TokenResult>();
    tokenResults.add(TokenResult.of(ServiceEnum.REGISTRY.getServiceId(), boxRegKey, OffsetDateTime.now().plusHours(1)));
    return TokenCreateResults.of(properties.boxUuid(), tokenResults);
  }

}
