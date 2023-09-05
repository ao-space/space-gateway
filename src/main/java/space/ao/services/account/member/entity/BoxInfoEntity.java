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

package space.ao.services.account.member.entity;

import lombok.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "box_info")
@ToString
public class BoxInfoEntity extends PanacheEntityBase{
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 平台切换到 v2 接口废弃此参数，强制升级 APP 之后删除
   */
  @Column(name = "box_regkey")
  private String boxRegKey;

  @Column(name = "passcode")
  private String passcode;

  @Column(name = "security_email")
  private String securityEmail;

  @Column(name = "security_email_host")
  private String securityEmailHost;

  @Column(name = "security_email_port")
  private String securityEmailPort;

  @Column(name = "security_email_ssl_enable")
  private Boolean securityEmailSslEnable;
}
