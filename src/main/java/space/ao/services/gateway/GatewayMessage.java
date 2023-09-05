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

import java.time.Instant;
import java.util.UUID;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

/**
 * It defines the gateway message structure, and it also
 * provides the definition of message type which can be used to delivery
 * broadcast message or user specified message.
 */
@Data
@RegisterForReflection
public class GatewayMessage {

  /**
   * Defines the message type.
   */
  public enum Type {
    /**
     * It's used to indicate a message as broadcast one
     * that are not user specified, which means every user
     * can receive it when it's subscribed.
     */
    BROADCAST,

    /**
     * It's used to indicate a message as user specified one,
     * which means only user who has the same user-id can receive
     * it when it's subscribed.
     */
    USER,

    /**
     * It's used to indicate a message as client specified one,
     * which means only client who has the same client-uuid can receive
     * it when it's subscribed.
     */
    CLIENT,
  }

  /**
   * used to indicate format version of this message,
   * currently it should only be: v1
   */
  private String version;

  /**
   * Used to indicate type of this message.
   */
  private Type type;

  /**
   * Used to indicate the user id that this message will be sent to.
   */
  private String userId;

  /**
   * Used to indicate the client uuid that this message will be sent to.
   */
  private String clientUuid;

  /**
   * Used to indicate the instant of this message creation.
   */
  private Instant time;

  /**
   * Used to indicate the uuid of this message.
   */
  private UUID uuid;

  /**
   * Used to indicate the topic of this message.
   */
  private String topic;

  /**
   * Used to hold the content of this message.
   */
  private Object content;

  /**
   * Used to indicate the content type of this content of this message.
   */
  private String contentType;
}
