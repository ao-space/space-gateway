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

package space.ao.services.support;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import okhttp3.OkHttpClient;
import org.jboss.logging.Logger;
import space.ao.services.account.support.service.ServiceError;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.response.ResponseBase;
import space.ao.services.support.service.ServiceOperationException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.Duration;
import java.util.Set;

/**
 * It provides application scoped {@code Mappers}, {@code Filters} etc. configurations
 * for all the REST requests and responses, such as mapping error result or filtering &
 * dumping request or response access log. You can also add additional configurations
 * in this class.
 *
 * @since 0.1.0
 * @author Haibo Luo
 */
@ApplicationScoped
public class RestConfiguration {

  static final Logger LOG = Logger.getLogger("rest.log");

  public static final String REQUEST_ID = "Request-Id";

  @Inject
  ApplicationProperties properties;

  @Produces
  @SuppressWarnings("unused") // Used by DIC framework
  public OkHttpClient httpClient() {
    return new OkHttpClient.Builder()
        .readTimeout(Duration.parse("PT" + properties.gatewayHttpClientReadTimeout()))
        .writeTimeout(Duration.parse("PT" + properties.gatewayHttpClientWriteTimeout()))
        .build();
  }

  /**
   * It provides exception result mapping for REST responses. For more information:
   * <a href="https://developer.jboss.org/docs/DOC-48310">https://developer.jboss.org/docs/DOC-48310</a>.
   */
  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Context
    HttpServerRequest request;

    @Override
    public Response toResponse(Exception exception) {

      return convertToErrorResponse(request, exception);
    }
  }

  /**
   * 提供专门针对 ConstraintViolationException 异常的拦截，因为默认通过 {@link ErrorMapper} 无法拦截到这类异常。
   * 具体参考：<a href="https://stackoverflow.com/questions/34452996/jaxrs-jersey-2-validation-errors-does-not-invoke-the-exceptionmapper">jaxrs-jersey-2-validation-errors-does-not-invoke-the-exceptionmapper</a>
   */
  @Provider
  public static class ConstraintViolationErrorMapper implements ExceptionMapper<ConstraintViolationException> {

    @Context
    HttpServerRequest request;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
      return convertToErrorResponse(request, exception);
    }
  }

  private static Response convertToErrorResponse(HttpServerRequest request, Exception exception) {
    // 当 Rest 请求接口有未处理异常发生时，在对其错误转换之前，始终输出一条该请求的异常日志。
    LOG.error("error mapper", exception);

    var requestId = request.getHeader(REQUEST_ID);
    requestId = ((requestId == null) ? request.getParam(REQUEST_ID) : requestId);
    Response.StatusType status = Response.Status.INTERNAL_SERVER_ERROR;
    String message = exception.getMessage();
    Integer errCode = null;
    Object[] context = null;

    if (exception instanceof WebApplicationException) {
      status = ((WebApplicationException) exception).getResponse().getStatusInfo();
      var entity = ((WebApplicationException) exception).getResponse().getEntity();
      // For those WebApplicationExceptions that ready have an entity, just response them immediately.
      if (entity != null) {
        return Response.status(status).entity(entity).build();
      }
    }

    if (exception instanceof ConstraintViolationException) {
      status = Response.Status.BAD_REQUEST;
    } else if (exception instanceof ServiceOperationException) {
      errCode = ((ServiceOperationException) exception).getErrorCode();
      context = ((ServiceOperationException) exception).getMessageParameters();
    }

    // account 模块异常
    if(exception instanceof space.ao.services.account.support.service.ServiceOperationException){
      errCode = ((space.ao.services.account.support.service.ServiceOperationException) exception).getErrorCode();
      context = ((space.ao.services.account.support.service.ServiceOperationException) exception).getMessageParameters();
      LOG.error("account exception: " + errCode);
      if(errCode == ServiceError.REQ_RATE_OVER_LIMIT.getCode()){
        return ResponseBase.builder().code("GW-" + errCode)
                .requestId(requestId)
                .message(message != null ? (message) : (status.getReasonPhrase()))
                .context(context)
                .build()
                .toNormalResponse();
      }

      return ResponseBase.fromAccountErrorCode(errCode)
              .requestId(requestId)
              .message(message != null ? (message) : (status.getReasonPhrase()))
              .context(context)
              .build()
              .toResponse();
    }

    return ResponseBase.fromErrorCode(
        errCode != null ? errCode : status.getStatusCode())
        .requestId(requestId)
        .message(
            message != null ? (message) : (status.getReasonPhrase()))
        .context(context)
        .build()
        .toResponse();
  }

  private final static String STOPWATCH = "stopwatch";

  /**
   * It provides an access log recording for all REST requests. For more information:
   * <a href="https://quarkus.io/guides/rest-json"> https://quarkus.io/guides/rest-json</a>.
   */
  @Provider
  public static class LoggingRequestFilter implements ContainerRequestFilter {

    @Context
    UriInfo info;

    @Context
    HttpServerRequest request;

    @Override
    public void filter(ContainerRequestContext context) {
      context.setProperty(STOPWATCH, Stopwatch.createStarted());
      var requestId = request.getHeader(REQUEST_ID);
      requestId = ((requestId == null) ? request.getParam(REQUEST_ID) : requestId);
      if (request.params().isEmpty()) {
        LOG.infof(
            "[Request] %s %s, from ip: %s, req-id: %s",
            context.getMethod(),
            info.getPath(),
            request.remoteAddress(),
            requestId
        );
      } else {
        LOG.infof(
            "[Request] %s %s, from IP %s, params: {%s}, req-id: %s",
            context.getMethod(),
            info.getPath(),
            request.remoteAddress(),
            request.params(),
            requestId
        );
      }
    }
  }

  /**
   * It provides an access log recording for all REST responses. For more information:
   * <a href="https://quarkus.io/guides/rest-json"> https://quarkus.io/guides/rest-json</a>.
   */
  @Provider
  public static class LoggingResponseFilter implements ContainerResponseFilter {

    @Context
    UriInfo info;

    @Context
    HttpServerResponse response;

    @Context
    HttpServerRequest request;

    @Override
    public void filter(
        ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
      var requestId = request.getHeader(REQUEST_ID);
      requestId = ((requestId == null) ? request.getParam(REQUEST_ID) : requestId);
      var stopwatch = (Stopwatch) requestContext.getProperty(STOPWATCH);
      var method = requestContext.getMethod();
      var status = responseContext.getStatusInfo();
      try {
        if (stopwatch != null) { // will be null when 404 error happens
          stopwatch.stop();
        }
        LOG.infof(
            "[Response] %s %s (%d %s), elapsed: %s, req-id: %s",
            method,
            info.getPath(),
            status.getStatusCode(), status.getReasonPhrase(),
            stopwatch,
            requestId
        );
      } finally {
        requestContext.removeProperty(STOPWATCH);
        if (!Strings.isNullOrEmpty(requestId)) {
          responseContext.getHeaders().add(REQUEST_ID, requestId);
        }
      }
    }
  }

  @Provider
  public static class TokenAuthResponseFilter implements ContainerResponseFilter {

    @Context
    UriInfo info;

    @Context
    HttpServerResponse response;

    @Context
    HttpServerRequest request;

    // 提供一个请求路径白名单，用于过滤请求，并为其添加安全相关的 HTTP header。
    // 参考：https://www.rfc-editor.org/rfc/rfc6749#section-5.1
    private final static Set<String> ITEMS = Set.of(
        "/v1/api/gateway/auth/token/create",
        "/v1/api/gateway/auth/token/create/member",
        "/v1/api/gateway/auth/token/refresh",
        "/v1/api/gateway/openapi/auth/token/create",
        "/v1/api/gateway/openapi/auth/token/refresh"
    );

    @Override
    public void filter(
        ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
      var path = info.getPath();
      if (path != null && ITEMS.contains(path)) {
        response.headers().add("Cache-Control", "no-store");
        response.headers().add("Pragma", "no-cache");
      }
    }
  }
}
