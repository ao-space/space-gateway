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

package space.ao.services.gateway;

import com.google.common.io.CharStreams;
import io.quarkus.runtime.Startup;
import lombok.SneakyThrows;
import org.jboss.logging.Logger;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.OperationUtils;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@Startup
@ApplicationScoped
public class GatewayBeansFactory {

  static final Logger LOG = Logger.getLogger("app.log");

  @Inject
  OperationUtils utils;

  @Inject
  ApplicationProperties properties;

  @PostConstruct
  @SuppressWarnings("unused") // Initialize data
  void init() {
    LOG.info("start init GatewayBeansFactory");

  }

  @Produces
  @SneakyThrows
  @SuppressWarnings("unused") // Used by DIC framework
  public Routers routers() {
    Routers routers;
    LOG.info("start to fetch and create gateway routers.");
    try (var reader = utils.getFileStreamReader(properties.gatewayRoutersLocation())) {
      var routesJson = CharStreams.toString(reader);
      var result = routesJson.replace("{{aospace-all-in-one}}", extractAddress(properties.systemAgentUrlBase()));
      routers = utils.jsonToObject(result, Routers.class);
      LOG.info("gateway routers created succeed: " + routers);
    } catch (Exception e) {
      LOG.error("gateway routers created failed", e);
      throw e;
    }
    return routers;
  }

  private String extractAddress(String input) {
    if(input == null) {
      return "172.17.0.1";
    }
    String[] parts = input.split(":");
    if (parts.length > 1) {
      String host = parts[1].substring(2);
      int index = host.indexOf('/');
      if (index > 0) {
        host = host.substring(0, index);
      }
      return host;
    }
    return "172.17.0.1";
  }

}
