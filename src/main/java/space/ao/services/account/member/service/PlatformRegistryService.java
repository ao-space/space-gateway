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

package space.ao.services.account.member.service;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import space.ao.services.account.member.dto.PlatformInfo;
import space.ao.services.account.support.service.ServiceError;
import space.ao.services.account.support.service.ServiceOperationException;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.StringUtils;
import space.ao.services.support.log.Logged;
import space.ao.services.support.platform.PlatformRegistryServiceRestClient;
import space.ao.services.support.platform.PlatformUtils;
import space.ao.services.support.platform.info.registry.*;
import space.ao.services.support.platform.temp.RequestTypeEnum;
import space.ao.services.support.platform.temp.TempRegistryInfoEntity;
import space.ao.services.support.platform.temp.TempRegistryInfoRepository;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Objects;

@ApplicationScoped
public class PlatformRegistryService {

  @Inject
  PlatformUtils platformUtils;
  @Inject
  ApplicationProperties properties;
  @Inject
  OperationUtils utils;
  @Inject
  TempRegistryInfoRepository tempRegistryInfoRepository;
  static final Logger LOG = Logger.getLogger("app.log");

  @Inject
  @RestClient
  PlatformRegistryServiceRestClient platformRegistryServiceRestClient;
  static String ssplatformUrl;

  public void createRestClient() {
    if(StringUtils.isBlank(ssplatformUrl)){
      ssplatformUrl = properties.ssplatformUrl();
    }
    platformRegistryServiceRestClient =  QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create(ssplatformUrl))
            .build(PlatformRegistryServiceRestClient.class);
  }

  public PlatformInfo setPlatform(String ssplatformUrl){
    if(!StringUtils.isBlank(ssplatformUrl)){
      PlatformRegistryService.ssplatformUrl = ssplatformUrl;
      createRestClient();
      platformUtils.setRegistryServiceRestClient(platformRegistryServiceRestClient);
    }
    return PlatformInfo.of(ssplatformUrl);
  }

  /**
   * 向平台注册用户
   *
   * @param requestId        请求id
   * @param userRegistryInfo 向平台注册用户信息
   * @return 注册结果
   * @author suqin
   * @date 2021-10-12 21:38:33
   **/
  @Logged
  @Transactional
  public UserRegistryResult registryUser(String requestId, UserRegistryInfo userRegistryInfo, Boolean platformRegistry) {
    if (Boolean.TRUE.equals(platformRegistry) && platformUtils.isRegistryPlatformAvailable(requestId)) {
      var boxRegKey = platformUtils.createRegistryBoxRegKey(requestId);
      return platformRegistryServiceRestClient.platformRegistryUser(userRegistryInfo, requestId, boxRegKey, properties.boxUuid());
    } else {
      TempRegistryInfoEntity tempRegistryInfoEntity = new TempRegistryInfoEntity();
      tempRegistryInfoEntity.setRequestId(requestId);
      tempRegistryInfoEntity.setClientUUID(userRegistryInfo.clientUUID());
      tempRegistryInfoEntity.setUserId(Long.valueOf(userRegistryInfo.userId().split("-")[1]));
      tempRegistryInfoEntity.setType(RequestTypeEnum.REGISTRY_USER.getName());
      tempRegistryInfoEntity.setTempInfo(utils.objectToJson(userRegistryInfo));
      tempRegistryInfoEntity.setCreateAt(OffsetDateTime.now());
      tempRegistryInfoRepository.insert(tempRegistryInfoEntity);
      LOG.warnv("registry user failed, Unable to connect to the platform, delay registration to connectable platform, userRegistryInfo: {0}", userRegistryInfo);
      return new UserRegistryResult(properties.boxUuid(), userRegistryInfo.userId(), null, RegistryTypeEnum.USER_ADMIN.getName(), userRegistryInfo.clientUUID());
    }

  }

  /**
   * 向平台解注册用户
   *
   * @param requestId 请求id
   * @param aoid      解注册用户 aoid
   * @author suqin
   * @date 2021-10-12 21:38:33
   **/
  @Logged
  public void platformRegistryUserReset(String requestId, String aoid) {
    var boxRegKey = platformUtils.createRegistryBoxRegKey(requestId);
    try {
      platformRegistryServiceRestClient.platformResetUser(requestId, boxRegKey, properties.boxUuid(), aoid);
    } catch (WebApplicationException e) {
      if(Objects.equals(Status.NOT_FOUND.getStatusCode(), e.getResponse().getStatusInfo().getStatusCode())){
        LOG.errorv("platform Registry User Reset: {0}", utils.getErrorInfoFromException(e));
      } else {
        throw e;
      }
    }
  }


  /**
   * 向平台注册客户端
   *
   * @param requestId          请求id
   * @param clientRegistryInfo 向平台注册客户端信息
   * @param aoid               客户端对应 aoid
   * @return 注册结果
   * @author suqin
   * @date 2021-10-12 21:38:33
   **/
  @Logged
  @Transactional
  public ClientRegistryResult registryClient(String requestId, ClientRegistryInfo clientRegistryInfo, String aoid) {

    if(Boolean.TRUE.equals(utils.getEnableInternetAccess()) && platformUtils.isRegistryPlatformAvailable(requestId)){
      var boxRegKey = platformUtils.createRegistryBoxRegKey(requestId);
      return platformRegistryServiceRestClient.platformRegistryClient(clientRegistryInfo, requestId, boxRegKey, properties.boxUuid(), aoid);
    } else {
      TempRegistryInfoEntity tempRegistryInfoEntity = new TempRegistryInfoEntity();
      tempRegistryInfoEntity.setRequestId(requestId);
      tempRegistryInfoEntity.setClientUUID(clientRegistryInfo.clientUUID());
      tempRegistryInfoEntity.setUserId(Long.valueOf(aoid.split("-")[1]));
      tempRegistryInfoEntity.setType(RequestTypeEnum.REGISTRY_AUTH_CLIENT.getName());
      tempRegistryInfoEntity.setTempInfo(utils.objectToJson(clientRegistryInfo));
      tempRegistryInfoEntity.setCreateAt(OffsetDateTime.now());
      tempRegistryInfoRepository.insert(tempRegistryInfoEntity);
      LOG.warnv("registry client failed, Unable to connect to the platform, delay registration to connectable platform, " +
              "clientRegistryInfo: {0}", clientRegistryInfo);
      return null;
    }
  }

  /**
   * 向平台解注册客户端
   *
   * @param requestId  请求id
   * @param aoid       客户端对应 aoid
   * @param clientUUID 客户端 clientUUID
   * @author suqin
   * @date 2021-10-12 21:38:33
   **/
  @Logged
  public void platformRegistryClientReset(String requestId, String aoid, String clientUUID) {
    if(Boolean.TRUE.equals(utils.getEnableInternetAccess()) && platformUtils.isRegistryPlatformAvailable(requestId)){
      var boxRegKey = platformUtils.createRegistryBoxRegKey(requestId);
      try {
        platformRegistryServiceRestClient.platformRestClient(requestId, boxRegKey, properties.boxUuid(), aoid, clientUUID);
      } catch (WebApplicationException e){
        LOG.errorv("platform Registry Client Reset: {0}", utils.getErrorInfoFromException(e));
        if(Objects.equals(Status.NOT_FOUND.getStatusCode(), e.getResponse().getStatusInfo().getStatusCode())){
          LOG.errorv("platform Registry Client Reset not found, clientUUID : {0}", clientUUID);
        } else {
          throw e;
        }
      }
    } else {
      TempRegistryInfoEntity tempRegistryInfoEntity = new TempRegistryInfoEntity();
      tempRegistryInfoEntity.setRequestId(requestId);
      tempRegistryInfoEntity.setClientUUID(clientUUID);
      tempRegistryInfoEntity.setUserId(Long.valueOf(aoid.split("-")[1]));
      tempRegistryInfoEntity.setType(RequestTypeEnum.RESET_AUTH_CLIENT.getName());
      tempRegistryInfoEntity.setTempInfo(utils.objectToJson(ClientRegistryInfo.of(clientUUID, aoid)));
      tempRegistryInfoEntity.setCreateAt(OffsetDateTime.now());
      tempRegistryInfoRepository.insert(tempRegistryInfoEntity);
      LOG.warnv("reset client failed, Unable to connect to the platform, delay registration to connectable platform, " +
              "clientUUID: {0}", clientUUID);
    }
  }

  public void checkRegistryPlatformAvailable(String requestId) {
    if (!platformUtils.isRegistryPlatformAvailable(requestId)) {
      throw new ServiceOperationException(ServiceError.PLATFORM_REGISTRY_NOT_AVAILABLE);
    }
  }
}
