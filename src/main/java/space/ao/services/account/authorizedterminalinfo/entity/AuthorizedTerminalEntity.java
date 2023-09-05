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

package space.ao.services.account.authorizedterminalinfo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/**
 * 用户表数据结构
 * @author suqin
 * @date 2021-11-22 21:39:57
 **/
@Entity
@Getter
@Setter
@Table(name = "authorized_terminal_info")
public class AuthorizedTerminalEntity extends PanacheEntityBase {
  @Id
  @Column(name = "terminal_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "userid")
  @NotNull
  @JsonIgnore
  private Long userid;

  @Column(length = 64, name = "aoid")
  @NotNull
  private String aoid;

  @Column(length = 64, name = "uuid")
  @NotNull
  private String uuid;

  @Column(length = 64, name = "terminal_mode")
  @NotNull
  private String terminalMode;

  @Column(name = "address")
  private String address;

  @Column(length = 64, name = "terminal_type")
  private String terminalType;

  /**
   * 创建时间 create_at
   * /登录时间 login_at
   * 自动登录有效期 expire_at
   * 刷新
   */
  @Column(name = "create_at")
  @NotNull
  private OffsetDateTime createAt;

  @Column(name = "expire_at")
  @NotNull
  private OffsetDateTime expireAt;

  @Column(name = "login_at")
  @NotNull
  private OffsetDateTime loginAt;

}
