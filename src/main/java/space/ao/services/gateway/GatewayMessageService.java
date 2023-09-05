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

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Gateway message service is used to publish gateway message to endpoint and
 * manage gateway message channel.
 */
@ApplicationScoped
public class GatewayMessageService {

  /**
   * This message emitter is only used to publish gateway message to client who
   * should have been subscribed to gateway message endpoint.
   */
  @Channel("gateway-messages-src")
  @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 256)
  Emitter<GatewayMessage> publisher;

  /**
   * Create a joint point for auditing all gateway message here.
   *
   * @param message the all gateway messages
   */
  @Incoming("gateway-messages-src")
  @Outgoing("gateway-messages")
  Multi<GatewayMessage> onMessage(Multi<GatewayMessage> message) {
    return message.log();
  }

  /**
   * It offers a way to query if the gateway channel has been established
   * before publishing message.
   *
   * @return true for ready to publish, and false for otherwise.
   */
  public boolean isChannelAvailable() {
    return publisher.hasRequests();
  }

  /**
   * Used to publish message when gateway message channel is ready to do so. Note,
   * before calling this method, you should always check if chancel is ready to write by
   * calling {@link #isChannelAvailable()}.
   *
   * @param gatewayMessage the message to be published.
   * @return the Uni result used to wait or to be notified when it completed.
   */
  public Uni<GatewayMessage> publish(GatewayMessage gatewayMessage) {
    return Uni.createFrom()
        .completionStage(publisher.send(gatewayMessage))
        .replaceWith(gatewayMessage);
  }
}

