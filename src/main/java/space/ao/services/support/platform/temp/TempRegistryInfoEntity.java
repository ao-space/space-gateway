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

package space.ao.services.support.platform.temp;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * @author zhichuang
 * @date 2023/3/30 0030
 **/

@Entity
@Getter
@Setter
@Table(name = "temp_registry_info")
public class TempRegistryInfoEntity extends PanacheEntityBase {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "request_id")
  @NotNull
  String requestId;

  @Column(name = "type")
  @NotNull
  private String type;

  @Column(name = "userid")
  @NotNull
  private Long userId;

  @Column(name = "client_uuid")
  @NotNull
  private String clientUUID;

  @Column(name = "temp_info")
  @NotNull
  private String tempInfo;

  @Column(name = "create_at")
  @NotNull
  private OffsetDateTime createAt;
}
