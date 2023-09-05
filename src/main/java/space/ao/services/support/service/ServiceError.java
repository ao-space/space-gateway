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

package space.ao.services.support.service;

/**
 * Used to define an error raised under the service layer.
 * It consists of following parts:
 * <ol>
 *   <li>code: used to specify the error code. It can be report to top layer for error detecting.</li>
 *   <li>message: used to specify the default error message. It would include
 *   some placeholder used to format a final string result,
 *   and the format uses {@link java.text.MessageFormat}'s style.</li>
 * </ol>
 * You can explicitly define all service errors here for further using. <em>Note:</em> we intend to reuse
 * service error code that is greater than 0 and less than 600 as http status code.
 *
 * @see java.text.MessageFormat
 * @see ServiceOperationException
 * @since 1.0.0
 */
public enum ServiceError {
  /**
   * Indicates an unknown error that might be somehow undefined currently.
   */
  UNKNOWN(-1, "unknown error"),
  AUTH_KEY_NOT_MATCH(4011, "auth key was not matched"),
  CLIENT_UUID_NOT_MATCH(4012, "client uuid was not matched"),
  AUTH_CODE_NOT_MATCH(4013, "auth-code was not matched! "),
  BOX_KEY_NOT_MATCH(4014, "bkey was not matched! "),
  ACCESS_TOKEN_INVALID(4015, "access token invalid"),
  REFRESH_TOKEN_INVALID(4016, "refresh token invalid"),
  INIT_UTIL_FAILED(4017, "init util failed, read pem error"),
  CLIENT_UUID_NOT_FOUND(4018, "client uuid not found"),
  APPLET_NOT_FOUND(4019, "applet not found"),
  APPLET_ALREADY_EXIST(4020, "applet has already exist"),
  VCFILE_UPLOAD_ERROR(4021, "vcfile upload error"),
  ADDRESSBOOK_NOT_PRESENT(4022, "addressbook not present"),
  INVALID_USER(4023, "user not exist"),
  INVALID_APPLET(4024, "applet invalid"),
  MESSAGE_PUSHED(5001,"Message push repeat "),
  MESSAGE_PUSHED_TIMEOUT(5002, "Message push request timed out"),
  DATABASE_ERROR(-1, "database exception"),
  REFRESH_TOKEN_TIMEOUT(6016, "refresh token invalid"),
  NO_MODIFY_RIGHTS(403, "You do not have the rights"),
  MESSAGE_DELETE_FAILED(5005, "delete failed message not exist, messageId: "),
  SIGNATURE_FAILED(5006, "signature failed"),

  NO_NOTIFICATION_ENUM(404, "not found enum"),
  API_JSON_EXCEPTION(2057, "apis Json Processing Exception"),
  API_IO_EXCEPTION(2058, "apis Json Processing Exception"),
  NO_OFFICE_PLATFORM(4404, "This feature is not supported on unofficial platforms"),
  VOD_SERVICE_ERROR(7001, "vod service error"),
  VOD_SERVICE_NOT_SUPPORT_VIDEO_CODING(7002, "vod service not support Video coding"),
  DEV_OPTIONS_DOMAIN_PREFIX_CONFLICT(8001, "this domain prefix already exists"),
  DEV_OPTIONS_SERVICE_NAME_CONFLICT(8002, "this service name already exists"),
  UNSUPPORTED_ALGORITHM(3404, "TOTP algorithm not supported"),
  PLATFORM_TYPE_ERROR(404, "platform type error"),
  PLATFORM_SERVICE_NOT_FOUND(404, "platform service not found"),
  PLATFORM_API_NOT_FOUND(404, "platform api not found")

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
