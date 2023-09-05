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

package space.ao.services.push.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NotificationEntity extends PanacheEntityBase {
  @Id
  @Column(name = "message_id")
  @NotNull
  String messageId;
  @Column(name = "userid")
  @NotNull
  @JsonIgnore
  Integer userid;
  @Column(name = "client_uuid")
  @NotNull
  String clientUUID;
  @Column(name = "opt_type")
  @NotNull
  String optType;
  @Column(name = "request_id")
  @NotNull
  String requestId;
  @Column(name = "data")
  @NotNull
  String data;
  @Column(name = "read")
  @NotNull
  Boolean read;
  @Column(name = "pushed")
  Integer pushed;
  @Column(name = "create_at")
  @NotNull
  private OffsetDateTime createAt;
  public NotificationEntity(String messageId, Integer userid, String clientUUID, String optType, String requestId, String data){
    this.messageId = messageId;
    this.userid = userid;
    this.clientUUID = clientUUID;
    this.optType = optType;
    this.requestId = requestId;
    this.data = data;
    this.createAt = OffsetDateTime.now();
    this.read = false;
  }
}
