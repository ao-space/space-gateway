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

package space.ao.services.account.security.utils.token;

/**
 * 第一步验证返回的 token 的业务类型
 */
public enum SecurityTokenType {
    TOKEN_TYPE_VERIFIED_PWD_TOKEN, // 验证安全密码
    TOKEN_TYPE_VERIFIED_EMAIL_TOKEN, // 验证安保邮箱

    TOKEN_TYPE_APPLY_MODIFY_ADMIN_PWD, // 绑定端确认
    TOKEN_TYPE_MODIFY_ADMIN_PWD, // 修改端提交

    TOKEN_TYPE_APPLY_RESET_ADMIN_PWD, // 绑定端确认
    TOKEN_TYPE_RESET_ADMIN_PWD, // 修改端提交

    TOKEN_TYPE_APPLY_NEW_APP_RESET_ADMIN_PWD, // 绑定端确认
    TOKEN_TYPE_NEW_APP_RESET_ADMIN_PWD, // 修改端提交
}