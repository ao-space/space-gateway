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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.io.CharStreams;
import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import space.ao.services.account.member.service.PlatformRegistryService;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.platform.info.ability.PlatformApiResults;
import space.ao.services.support.platform.info.ability.PlatformApis;
import space.ao.services.support.platform.info.registry.ClientRegistryInfo;
import space.ao.services.support.platform.info.registry.ClientRegistryResult;
import space.ao.services.support.platform.info.ServiceEnum;
import space.ao.services.support.platform.info.token.TokenInfo;
import space.ao.services.support.platform.info.token.TokenResult;
import space.ao.services.support.platform.info.token.TokenCreateResults;
import space.ao.services.support.platform.info.registry.UserRegistryInfo;
import space.ao.services.support.platform.info.registry.UserRegistryResult;
import space.ao.services.support.platform.info.token.TokenVerifySignInfo;
import space.ao.services.support.security.SecurityUtils;
import space.ao.services.support.service.ServiceError;
import space.ao.services.support.service.ServiceOperationException;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

@Mock
@ApplicationScoped
@RestClient
@SuppressWarnings("unused") // test uses this mocked class
public class MockPlatformRegistryServiceRestClient implements PlatformRegistryServiceRestClient {
  static final Logger LOG = Logger.getLogger("app.log");

  private static final String boxRegKey = "box-Reg-Key";
  @Inject
  ApplicationProperties properties;
  @Inject
  OperationUtils utils;
  @Inject
  SecurityUtils securityUtils;

  @Inject
  PlatformRegistryService platformRegistryService;



  @Override
  public TokenCreateResults createTokens(TokenInfo tokenInfo, String reqId) {
    if(securityUtils.getSecurityProvider().verifySignUsingBoxPublicKey(reqId,
            utils.objectToJson(TokenVerifySignInfo.of(tokenInfo.boxUUID(), tokenInfo.serviceIds())),
            tokenInfo.sign())){
      var tokenResults = new ArrayList<TokenResult>();
      tokenResults.add(TokenResult.of(ServiceEnum.REGISTRY.getServiceId(), boxRegKey, OffsetDateTime.now().plusHours(1)));
      return TokenCreateResults.of(properties.boxUuid(), tokenResults);
    }
    return null;
  }

  @Override
  public UserRegistryResult platformRegistryUser(UserRegistryInfo userRegistryInfo, String reqId,
      String boxRegKey, String boxUUID) {
    var subdomain = userRegistryInfo.subdomain();
    return new UserRegistryResult(boxUUID, userRegistryInfo.userId(), subdomain != null ? subdomain : utils.unifiedRandomCharters(6) + ".eulix.xyz", userRegistryInfo.userType(), userRegistryInfo.clientUUID());
  }

  @Override
  public void platformResetUser(String reqId, String boxRegKey, String boxUUID, String userId) {

  }

  @Override
  public ClientRegistryResult platformRegistryClient(ClientRegistryInfo clientInfo, String reqId,
      String boxRegKey, String boxUUID, String userId) {
    return new ClientRegistryResult(boxUUID, userId, clientInfo.clientUUID(), clientInfo.clientType());
  }

  @Override
  public void platformRestClient(String reqId, String boxRegKey, String boxUUID, String userId,
      String clientUUID) {

  }

  @Override
  public PlatformApiResults ability(String requestId) {
    PlatformApis routers;
    LOG.info("start to fetch and create platform apis.");
    try (var reader = utils.getFileStreamReader("/platform/api/servicesapi.json")) {
      routers = utils.jsonToObject(CharStreams.toString(reader), PlatformApis.class);
      LOG.info("platform apis created succeed: " + routers);
    } catch (JsonProcessingException e) {
      LOG.errorv("platform apis created failed, processing json: " , e);
      throw new ServiceOperationException(ServiceError.DATABASE_ERROR);
    } catch (IOException e) {
      LOG.errorv("platform apis created failed: read file " , e);
      throw new ServiceOperationException(ServiceError.DATABASE_ERROR);
    }

    List<PlatformApis.PlatformApi> platformApiResult = new ArrayList<>();

    for (var services : routers.getServices().entrySet()) {
      for (var api :services.getValue().entrySet()) {
        platformApiResult.add(api.getValue());
      }
    }
    return new PlatformApiResults(platformApiResult);
  }

  @Override
  public PlatformStatusResult status(String requestId) {
    return PlatformStatusResult.of("ok", "1.0.0");
  }

}
