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

package space.ao.services.account.personalinfo.entity;

import lombok.Getter;
import lombok.Setter;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.ToString;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

/**
 * 用户表数据结构
 * @author suqin
 * @date 2021-10-08 21:39:57
 **/
@Entity
@Getter
@Setter
@ToString
@Table(name = "userinfo")
public class UserEntity extends PanacheEntityBase {
  @Id
  @Column(name = "userid")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "role")
  @NotNull
  @Enumerated(EnumType.STRING)
  private Role role;

  @Column(length = 24, name = "personal_name")
  @NotNull
  private String personalName;

  @Column(name = "personal_sign", length = 120)
  private String personalSign;

  @Column(name = "image")
  private String image;

  @Column(name = "aoid")
  @NotNull
  private String aoId;

  @Column(name = "authkey")
  private String authKey;

  @Column(name = "userdomain")
  private String userDomain;

  @Column(name = "client_uuid")
  private String clientUUID;

  @Column(name = "create_at")
  @NotNull
  private OffsetDateTime createAt;

  @Column(name = "image_md5")
  private String imageMd5;

  @Column(name = "phone_model")
  private String phoneModel;

  @Column(name = "phone_type")
  private String phoneType;

  @Column(name = "apply_email")
  private String applyEmail;

  @Column(name = "space_limit")
  private Long spaceLimit;

  public UserEntity() {

  }


  @Getter
  public enum Role {
    ADMINISTRATOR,
    GUEST
  }

  public UserEntity(Role role, String name, String domain, String aoId) {
    this.role = role;
    this.aoId = aoId;
    this.personalName = name;
    this.userDomain = domain ;
  }
}
