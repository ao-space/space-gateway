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

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.google.common.base.Stopwatch;
import lombok.Getter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import space.ao.services.support.platform.info.ServiceEnum;
import space.ao.services.support.platform.info.ability.PlatformApis;
import space.ao.services.support.platform.info.token.TokenCreateResults;
import space.ao.services.support.platform.info.token.TokenInfo;
import space.ao.services.support.platform.info.token.TokenResult;
import space.ao.services.support.platform.info.token.TokenVerifySignInfo;
import space.ao.services.support.security.SecurityUtils;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.log.Logged;

@ApplicationScoped
public class PlatformUtils {

  static final Logger LOG = Logger.getLogger("app.log");

  @Inject
  @RestClient
  PlatformRegistryServiceRestClient platformRegistryServiceRestClient;
  @Inject
  @RestClient
  PlatformOpstageBoxRegKeyServiceRestClient platformOpstageBoxRegKeyServiceRestClient;
  @Inject
  @RestClient
  PlatformOpstageServiceRestClient platformOpstageServiceRestClient;
  @Inject
  ApplicationProperties properties;

  @Inject
  OperationUtils utils;
  @Inject
  SecurityUtils securityUtils;
  @Getter
  private PlatformApis platformApis;
  static final ConcurrentMap<String, TokenResult> boxRegKeyCache = new ConcurrentHashMap<>();

  public void setRegistryServiceRestClient(PlatformRegistryServiceRestClient platformRegistryServiceRestClient){
    this.platformRegistryServiceRestClient = platformRegistryServiceRestClient;
  }
  public String createRegistryBoxRegKey(String requestId){
    var token = boxRegKeyCache.get(ServiceEnum.REGISTRY.getServiceId());
    if (Objects.nonNull(token) && token.expiresAt().isAfter(OffsetDateTime.now().plusMinutes(5))){
      return token.boxRegKey();
    }
    var serviceIds = new ArrayList<String>();
    serviceIds.add(ServiceEnum.REGISTRY.getServiceId());
    return createBoxRegKey(requestId, serviceIds).tokenResults().get(0).boxRegKey();
  }


  @Logged
  public String createOpstageBoxRegKey(String requestId){
    var token = boxRegKeyCache.get(ServiceEnum.OPSTAGE.getServiceId());
    if (Objects.nonNull(token) && token.expiresAt().isAfter(OffsetDateTime.now().plusMinutes(5))){
      return token.boxRegKey();
    }
    var serviceIds = new ArrayList<String>();
    serviceIds.add(ServiceEnum.OPSTAGE.getServiceId());
    TokenCreateResults tokenResults = null;
    try {
      var sign = securityUtils.getSecurityProvider().signUsingBoxPrivateKey(requestId,
              Base64.getEncoder().encodeToString(
                      utils.objectToJson(TokenVerifySignInfo.of(properties.boxUuid(), serviceIds))
                              .getBytes(StandardCharsets.UTF_8)));

      tokenResults= platformOpstageBoxRegKeyServiceRestClient.createTokens(TokenInfo.of(properties.boxUuid(), serviceIds, sign), requestId);
      for (var opstageToken : tokenResults.tokenResults()) {
        boxRegKeyCache.put(opstageToken.serviceId(), opstageToken);
      }
    } catch (Exception e){
      LOG.errorv("createOpstageBoxRegKey error: {0}", e.getMessage());
    }
    return tokenResults == null ? null : tokenResults.tokenResults().get(0).boxRegKey();
  }

  /**
   * v1 老版本使用 boxUUID /  v2 新版本使用私钥加密 sign
   * @param requestId requestId
   * @return Box Reg Key
   */
  @Logged
  public TokenCreateResults createBoxRegKey(String requestId, List<String> serviceIds){
    var sign = securityUtils.getSecurityProvider().signUsingBoxPrivateKey(requestId,
            Base64.getEncoder().encodeToString(
                    utils.objectToJson(TokenVerifySignInfo.of(properties.boxUuid(), serviceIds))
                            .getBytes(StandardCharsets.UTF_8)));
    var tokenResults= platformRegistryServiceRestClient.createTokens(TokenInfo.of(properties.boxUuid(), serviceIds, sign), requestId);
    for (var token : tokenResults.tokenResults()) {
      LOG.infov("service id {0} create new boxRegKey, {1}", token.serviceId(), token.boxRegKey());
      boxRegKeyCache.put(token.serviceId(), token);
    }
    return tokenResults;
  }

  public TokenResult getTokenResult(String serviceId){
    return boxRegKeyCache.get(serviceId);
  }
  /**
   * 查询空间服务平台是否可用
   */
  public boolean isRegistryPlatformAvailable(String requestId) {
    try {
      platformRegistryServiceRestClient.status(requestId);
    } catch (Exception e){
      LOG.errorv("Registry Service Platform is not available - request-id: {0}, error: {1}, utils.getEnableInternetAccess(): {2}",
              requestId, e.getMessage(), utils.getEnableInternetAccess());
      return false;
    }
    return true;
  }

  /**
   * 查询产品运营平台是否可用
   */
  public boolean isOpstagePlatformAvailable(String requestId) {
    try {
      platformOpstageServiceRestClient.status(requestId);
      return true;
    } catch (Exception e){
      LOG.warnv("Opstage Platform V2 is not available - request-id: {0}, error: {1}", requestId, e.getMessage());
    }
    return false;
  }

  /**
   * 查询平台接口变化更新缓存
   */
  @PostConstruct
  public void queryPlatformAbility() {
    if(Boolean.TRUE.equals(utils.getEnableInternetAccess())){
      Stopwatch stopwatch = Stopwatch.createStarted();
      var requestId = utils.createRandomType4UUID();
      try {
        var apis = platformRegistryServiceRestClient.ability(requestId);
        var status = platformRegistryServiceRestClient.status(requestId);

        LOG.infov("Platform Ability has changed - request-id: {0}, PlatformApis: {1}", requestId, apis);

        platformApis = new PlatformApis(status.version(), new HashMap<>());

        Map<String, Map<String, PlatformApis.PlatformApi>> services = platformApis.getServices();
        LOG.infov("eulixplatform-registry-service: {0}", toPlatformList(apis.platformApis()));
        services.put("eulixplatform-registry-service", toPlatformList(apis.platformApis()));
        platformApis.setServices(services);
        platformApis.setVersion(status.version());
        LOG.infov("regularly query Platform Ability: {0} - request-id: {1}", stopwatch.elapsed(TimeUnit.SECONDS), requestId);
      } catch (Exception e){
        LOG.errorv("regularly query Platform Ability failed - request-id: {0}, error: {1}", requestId, e.getMessage());
      }
    }
  }

  /**
   * List<PlatformApi> platformApis; -> Map<String, PlatformApi>
   */
  public Map<String, PlatformApis.PlatformApi> toPlatformList(List<PlatformApis.PlatformApi> platformApis){
    var result = new HashMap<String, PlatformApis.PlatformApi>();
    for(var api: platformApis){
      result.put(api.briefUri().replace("/","_") + "_" + api.method(), api);
    }
    return result;
  }

  /**
   * 平台是否支持推送
   */
  public boolean isPlatformSupportPush() {
    if (Objects.isNull(platformApis)) {
      queryPlatformAbility();
    }
    if (Objects.isNull(platformApis) || Objects.isNull(platformApis.getServices()) || Objects.isNull(platformApis.getServices().get("eulixplatform-registry-service"))) {
      return false;
    }
    return platformApis.getServices().get("eulixplatform-registry-service").containsKey("message_push_post");
  }
}
