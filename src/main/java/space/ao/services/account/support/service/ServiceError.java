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

/**
 * Used to define a error raised under the service layer.
 * It consists of following parts:
 * <ol>
 *   <li>code: used to specify the error code. It can be report to top layer for error detecting.</li>
 *   <li>message: used to specify the default error message. It would include
 *   some placeholder used to format a final string result,
 *   and the format uses {@link java.text.MessageFormat}'s style.</li>
 * </ol>
 * You can explicitly define all of service errors here for further using. <em>Note:</em> we intend to reuse
 * service error code that is greater than 0 and less than 600 as http status code.
 *
 * @see java.text.MessageFormat
 * @see ServiceOperationException
 * @since 1.0.0
 */
public enum ServiceError {
  /**
   * Indicates a unknown error that might be somehow undefined currently.
   */
  UNKNOWN(-1, "unknown error"),
  INTERRUPTED_EXCEPTION(-1,"InterruptedException"),
  WRONG_NAME_FORM(403, "Wrong name form"),
  USER_NOT_FOUND(403, "User not found"),
  INVALID_GLOBAL_ID(403, "Invalid global id"),
  INVALID_USER_ID(403, "Invalid user id"),
  INVALID_AO_ID(403, "Invalid aoid"),
  INVALID_AUTHORIZED_CLIENT(4034, "Invalid authorized client info"),
  INVITE_CODE_INVALID(403, "Invalid invite code"),
  INVITE_CODE_EXPIRED(403, "Invite code expired"),
  DATABASE_QUERY_FAILED(403, "database query failed"),
  UPLOAD_FILE_FAILED(403, "Upload profile image failed"),
  GET_IMAGE_FAILED(403, "Get image failed"),
  GET_MEMBER_STORAGE_INFO_FAILED(403, "Get member storage info failed"),
  REGISTRY_FAILED(403, "Registry failed"),
  REGISTRY_RESET_FAILED(403, "Registry reset failed"),
  NO_MODIFY_RIGHTS(403, "You do not have the rights"),
  NO_ADMIN_DELETE_RIGHT(403, "Cannot delete administrator"),
  MEMBER_NUMBER_FULL(403, "The number of number reaches limit"),
  CLIENT_HAS_REGISTERED(403, "The client has been registered"),
  CLIENT_HAS_AUTHORIZED(403, "The client has been authorized"),
  PROFILE_PHOTO_INIT_FAILED(403, "Profile photo initial failed"),
  FILE_INIT_FAILED(403, "File initial failed"),
  MEMBER_INIT_FAILED(403, "member initial failed"),
  FILE_DELETE_FAILED(403, "File delete failed"),
  CREATE_ADMIN_INIT_FAILED(403, "initial failed while creating an administrator"),
  REQ_RATE_OVER_LIMIT(410, "client request rate is over limit"),
  REVOKE_USER_CLIENT_FAILED(500, "revoke user client failed"),
  REVOKE_ADMIN_CLIENT_FAILED(500, "revoke admin client failed"),
  PLATFORM_REGISTRY_USER_FAILED(403, "Platform user registry failed"),
  PLATFORM_REGISTRY_USER_RESET_FAILED(403, "Platform user registry reset failed"),
  SUBDOMAIN_WRONG_FORMAT(4001,"subdomain wrong format"),

  EMAIL_ALREADY_BOUND(4051,"This mailbox is already bound"),
  EMAIL_VERIFICATION_TIMEOUT(4052, "Verification timeout"),
  EMAIL_VERIFICATION_TOKEN_TIMEOUT(4053, "Email verification token timeout"),
  PASSWORD_NOT_SAME(4054, "The old and new passwords are the same."),
  PASSWORD_VERIFICATION_TOKEN_TIMEOUT(4055, "password verification token timeout"),

  EMAIL_VERIFICATION_FAILED(4011, "email verification failed"),

  TOKEN_TIMEOUT(40001, "token timeout"),
  USERDOMAIN_NOT_FOUND(404, "userdomain not found"),
  PLATFORM_REGISTRY_NOT_AVAILABLE(40002, "platform registry not available"),

  FILE_NOT_FOUND(40003, "dowload file from opstage failed"),
  ;

  /**
   * The identity of an error.
   */
  private final int code;

  /**
   * The default message of an error
   */
  private final String message;

  ServiceError(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public static ServiceError valueOf(int code) {
    for (ServiceError e : ServiceError.values()) {
      if (e.code == code) {
        return e;
      }
    }
    throw new IllegalArgumentException("invalid code for service error - " + code);
  }

  /**
   * Return the code of this error.
   */
  public String getMessage() {
    return message;
  }

  /**
   * Return the default message of this error.
   */
  public int getCode() {
    return code;
  }
}
