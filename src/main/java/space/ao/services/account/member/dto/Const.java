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

package space.ao.services.account.member.dto;

public final class Const {
  private Const() {}

  public static final class Admin {
    private Admin() {}
    public static final String ADMIN_AOID = "aoid-1";
    public static final String ADMIN_ID = "1";
  }

  public static final String SPACE_NAME_REG = "^[a-zA-Z0-9\\u4e00-\\u9fa5\\`~!@#$%^&*()-_+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]{1,24}$";

}
