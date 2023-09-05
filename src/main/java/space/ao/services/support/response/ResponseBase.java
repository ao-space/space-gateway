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

package space.ao.services.support.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Builder
@Schema(description = "网关接口的统一格式的响应")
public record ResponseBase<T>(@Schema(description = "返回码，格式为 GW-xxx。") String code, @Schema(description =
        "错误信息，格式为 MessageFormat： {0} xx {1}， " +
                "参考：https://docs.oracle.com/javase/8/docs/api/java/text/MessageFormat.html 。") String message,
                              @Schema(description = "请求标识 id，用于跟踪业务请求过程。") String requestId, T results,
                              @Schema(description = "错误信息中的上下文信息，用于通过 MessageFormat 格式化 message。") Object[] context) {

  public final static String CODE_PREFIX = "GW-";

  public ResponseBase(String code, String message, String requestId, T results, Object[] context) {
    this.code = code;
    this.message = message;
    this.requestId = requestId;
    this.results = results;
    this.context = context;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ResponseBase<?> that = (ResponseBase<?>) o;
    return Objects.equals(code, that.code) && Objects.equals(message, that.message) && Objects.equals(requestId, that.requestId) && Objects.equals(results, that.results) && Arrays.equals(context, that.context);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(code, message, requestId, results);
    result = 31 * result + Arrays.hashCode(context);
    return result;
  }

  @Override
  public String toString() {
    return "ResponseBase{" +
            "code='" + code + '\'' +
            ", message='" + message + '\'' +
            ", requestId='" + requestId + '\'' +
            ", results=" + results +
            ", context=" + Arrays.toString(context) +
            '}';
  }

  /**
   * 创建和返回一个表示请求成功（200 OK）的结果响应 Builder。
   *
   * @param requestId 请求 id
   * @param results   请求响应结果
   * @param <T>       请求响应结果的类型
   * @return 一个表示成功的结果响应 Builder。
   */
  public static <T> ResponseBaseBuilder<T> ok(String requestId, T results) {
    return ResponseBase.<T>builder()
            .code(CODE_PREFIX + "200").message("OK").requestId(requestId).results(results);
  }

  /**
   * 创建和返回一个表示请求被禁止（403 Forbidden）的结果响应 Builder。
   *
   * @param requestId 请求 id
   * @param <T>       请求响应结果的类型
   * @return 一个表示请求被禁止的结果响应 Builder。
   */
  public static <T> ResponseBaseBuilder<T> forbidden(String requestId) {
    return ResponseBase.<T>builder()
            .code(CODE_PREFIX + "403").message("Not allowed request").requestId(requestId);
  }

  /**
   * 创建和返回一个表示请求不合法（406 Not Acceptable）的结果响应 Builder。
   *
   * @param requestId 请求 id
   * @param <T>       请求响应结果的类型
   * @return 一个表示表示请求不合法的结果响应 Builder。
   */
  public static <T> ResponseBaseBuilder<T> notAcceptable(String requestId) {
    return ResponseBase.<T>builder()
            .code(CODE_PREFIX + "406").message("Not qualified request").requestId(requestId);
  }

  /**
   * 创建和返回一个表示请求资源未找到（404 Not Found）的结果响应 Builder。
   *
   * @param requestId 请求 id
   * @param <T>       请求响应结果的类型
   * @return 一个表示表示请求资源未找到的结果响应 Builder。
   */
  public static <T> ResponseBaseBuilder<T> notFound(String requestId) {
    return ResponseBase.<T>builder()
            .code(CODE_PREFIX + "404").message("Resource not found").requestId(requestId);
  }

  /**
   * 创建和返回通过数字 error code 用于构建的响应 Builder。
   *
   * @param errCode 指定响应的数字 error code
   * @param <T>     请求响应结果的类型
   * @return 包含数据 error code 的响应 Builder。
   */
  public static <T> ResponseBaseBuilder<T> fromErrorCode(int errCode) {
    return ResponseBase.<T>builder().code(CODE_PREFIX + errCode);
  }

  public static <T> ResponseBaseBuilder<T> fromAccountErrorCode(int errCode) {
    return ResponseBase.<T>builder().code("ACC-" + errCode);
  }

  /**
   * 从 响应枚举中获取 用于构建的响应 Builder。
   *
   * @return 响应枚举 对应的响应 Builder。
   */
  public static <T> ResponseBaseBuilder<T> fromResponseBaseEnum(String requestId, ResponseBaseEnum responseBaseEnum) {
    return ResponseBase.<T>builder().code(responseBaseEnum.getCode()).message(
        responseBaseEnum.getMessage()).requestId(requestId);
  }

  public Response toResponse() {
    final Response.Status status = Response.Status.fromStatusCode(getErrorCode());
    return Response
            .status(status != null ? status : INTERNAL_SERVER_ERROR)
            .entity(this)
            .build();
  }

  public Response toNormalResponse() {
    final Response.Status status = Response.Status.OK;
    return Response
            .status(status)
            .entity(this)
            .build();
  }

  public WebApplicationException toWebException() {
    return new WebApplicationException(toResponse());
  }

  @JsonIgnore
  public String getFormattedMessage() {
    return MessageFormat.format(message, context);
  }

  @JsonIgnore
  public int getErrorCode() {
    return Integer.parseInt(code.split("-")[1]);
  }

  // 兼容 网关和account 二合一之前的 account
  public static <T> ResponseBase<T> forbidden(String message, String requestId) {
    return ResponseBase.of("ACC-403", message, requestId, null);
  }
  public static <T> ResponseBase<T> internalError(String message, String requestId) {
    return ResponseBase.of("ACC-500", message, requestId, null);
  }

  public static <T> ResponseBase<T> okACC(String requestId, T results) {
    return ResponseBase.of("ACC-200", "OK", requestId, results);
  }

  public static <T> ResponseBase<T> of(String code, String message, String requestId, T results) {
    return new ResponseBase<>(code, message, requestId, results, null);
  }
}