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

package space.ao.services.account.support.service;

public enum ServiceDefaultVar {
  DEFAULT_IMAGE_PATH("image/"),
  ACCOUNT_PASSWD_EXCEED("ACC-461"),
  ACCOUNT_PASSWD_ERROR("ACC-463"),
  ACCOUNT_USER_NOT_FOUND("ACC-505"),
  ACCOUNT_RESET_ERR("ACC-560"),
  ACCOUNT_ADMIN_REVOKED("ACC-462"),
  ACCOUNT_REPEATED_REQUEST("ACC-4051"),

  DEFAULT_IMAGE_FILE("s.png"),
  DEFAULT_AVATAR_FILE("avatar@3x.png"),
  AOID_PREFIX("aoid-"),
  DEFAULT_DATA_FILE("admin"),
  NAME_REGULAR_EXPRESS("[\\u4e00-\\u9fa5_a-zA-Z0-9!@#$%^&*()~+]+"),
  INVITE_URL_NEW_USER("/member/accept?subdomain=%s&invitecode=%s&keyfingerprint=%s&account=%s&member=%s&create=%d&expire=%d"),
  INVITE_URL_EXIST_USER("/member/accept?subdomain=%s&invitecode=%s&keyfingerprint=%s&account=%s&member=%s&create=%d&expire=%d&aoid=%s"),
  ACC_4031("ACC-4031"),
  ACC_4032("ACC-4032"),
  ACC_4033("ACC-4033"),
  ACC_4034("ACC-4034");

  private final String serviceVar;

  ServiceDefaultVar(String name) {
    this.serviceVar = name;
  }

  public String toString() {
    return serviceVar;
  }
}
