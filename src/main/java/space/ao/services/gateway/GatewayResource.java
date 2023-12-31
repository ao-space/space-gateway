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

package space.ao.services.gateway;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.reactive.messaging.Channel;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import space.ao.services.support.RestConfiguration;
import space.ao.services.support.TokenUtils;
import space.ao.services.support.model.AccessToken;
import space.ao.services.support.security.SecurityUtils;
import space.ao.services.support.OperationUtils;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

import java.util.Map;
import java.util.Optional;

@Path("/v1/api/gateway")
@Tag(name = "Space Gateway Generic-call Service",
    description = "Provides overall space requests' auth, encrypt, decrypt and router service.")
public class GatewayResource {

  @Inject
  GatewayService service;

  @Inject
  OperationUtils utils;
  @Inject
  SecurityUtils securityUtils;
  @Inject
  TokenUtils tokenUtils;


  /**
   * Handles a generic unified gateway api call request and returns a unified response.
   *
   * <p><h3>Call Request & Response:</h3>
   * <ul>
   *   <li>Call Request (For the body structure of call request, please refer to {@link RealCallRequest}):
   *   <blockquote><pre>
   *   {
   *     "accessToken" "",
   *     "body": "(BASE64 + encrypted with shared symmetric secret key)"
   *   }
   *   </pre></blockquote>
   *   </li>
   *   <li>Call Response (For the body structure of call response, please refer to service specified by service name):
   *   <blockquote><pre>
   *   {
   *     "code": 200,
   *     "message: "OK",
   *     "requestId": "",
   *     "body" : "(BASE64 + encrypted with shared symmetric secret key)"
   *   }
   *   </pre></blockquote>
   *   </li>
   * </ul>
   *
   * @param requestId the request track id generated by the caller of client.
   * @param callRequest the unified api call request
   * @return the unified api call response.
   * @see CallRequest
   * @see RealCallRequest
   * @see RealCallResult
   */
  @POST
  @Path("/call")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(description =
      "Tries to handle and forward a space call request to underlying service regarding with " +
          "specified service and api name.")
  public RealCallResult call(@Valid @NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId,
                             @HeaderParam("Authorization") String bearerToken,
                             @Valid CallRequest callRequest) {
    final var accessToken = checkAccessToken(requestId, bearerToken, callRequest.accessToken());
    final var request = checkRequestBody(callRequest.body(), accessToken);

    final var handleResult = service.handleCall(requestId, request, accessToken);
    final var result = encodeResult(handleResult, accessToken);
    return RealCallResult.of(200, "OK", requestId, result);
  }

  /**
   * Used to upload a certain type of file to underlying service based on
   * HTTP multipart/form-data standard. It uses HTTP POST method and needs to
   * specify the file and callRequest parts. For the body structure of callRequest,
   * please refer to {@link RealCallRequest}.
   *
   * <p><em>Note:</em> The necessary file parameters of uploading
   * is defined by {@link UploadEntity} which is wrapped as entity of {@link RealCallRequest}.
   *
   * @param requestId the request track id generated by the caller of client.
   * @param mpr the multipart resource of HTTP request
   * @return http 200 response if succeeded.
   * @see RealCallRequest
   * @see UploadEntity
   */
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/upload")
  @Operation(description =
      "Tries to handle and forward a space file upload request to underlying service regarding with " +
          "specified service and api name.")
  public RealCallResult upload(@Valid @NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId,
                               @HeaderParam("Authorization") String bearerToken,
                               MultipartRequest mpr) {
    final var callRequestStr = utils.inputStreamToString(mpr.callRequest);
    final var callRequest = utils.jsonToObject(callRequestStr, CallRequest.class);
    final var accessToken = checkAccessToken(requestId, bearerToken, callRequest.accessToken());
    final var request = checkRequestBody(callRequest.body(), accessToken);

    final var handleResult = service.handleUpload(requestId, request, mpr.file, accessToken);
    final var result = encodeResult(handleResult, accessToken);
    return RealCallResult.of(200, "OK", requestId, result);
  }

  /**
   * Used to download a file hosted in the underlying service. It uses HTTP GET method and
   * will specify a temporary file name for attachment.
   *
   * @param requestId the request track id generated by the caller of client.
   * @param callRequest the unified api call request
   * @return http response with header: <tt>Content-Disposition: attachment;filename=file</tt>
   */
  @POST
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/download")
  @Operation(description =
      "Tries to handle and forward a space file download request to underlying service regarding with " +
          "specified service and api name.")
  public Response download(@NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId,
                           @HeaderParam("Authorization") String bearerToken,
                           @Valid CallRequest callRequest) {
    final var accessToken = checkAccessToken(requestId, bearerToken, callRequest.accessToken());
    final var request = checkRequestBody(callRequest.body(), accessToken);

    final var result = service.handleDownload(requestId, request, accessToken);
    Response.ResponseBuilder builder;
    if(result.getCode() == Response.Status.NOT_FOUND.getStatusCode()){
      builder = Response.status(Response.Status.NOT_FOUND).entity(result.getFile());
    } else if(result.getFileSize() == 0L){
      builder = Response.ok(result.getFile());
    } else {
      builder = Response.ok(result.getFile())
          .header("File-Size", String.valueOf(result.getFileSize()));
    }
    if (result.getHeaders() != null && !result.getHeaders().isEmpty()) {
      result.getHeaders().forEach(builder::header);
    }
    return builder.build();
  }

  /**
   * This channel is used to output gateway messages. Http Clients
   * can subscribe to it by calling this SSE endpoint and stream messages
   * with a push-style manner.
   */
  @Channel("gateway-messages")
  Multi<GatewayMessage> messages;

  /**
   * It offers an endpoint for serving SSE(Server-sent Events) based http message stream request.
   * From this endpoint, You can specify a topic to filter message that client is interested in.
   *
   * @param topic       the topic query used to filter message stream
   * @param clientUuid  the client uuid that belongs to the caller
   * @param requestId   the request track id generated by the caller of client.
   * @param bearerToken the Authorization token info
   * @param sse         the current sse context
   * @param sink        the current sse sink context
   * @return the SSE stream
   */
  @GET
  @Path("/sse")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public Multi<OutboundSseEvent> getSSE(@QueryParam("topic") String topic,
                                        @QueryParam("clientUuid") String clientUuid,
                                        @NotBlank @HeaderParam(RestConfiguration.REQUEST_ID) String requestId,
                                        @NotBlank @HeaderParam("Authorization") String bearerToken,
                                        @Context Sse sse, @Context SseEventSink sink) {
    final var accessToken = checkAccessToken(requestId, bearerToken, null);

    sink.send(sse.newEventBuilder().id("0").mediaType(MediaType.APPLICATION_JSON_TYPE)
        .data(Map.of("message", "Welcome!")).name(topic).build());

    return messages.filter(m -> checkMessage(topic, accessToken, m))
        .map(m -> sse.newEventBuilder()
            .id(m.getUuid().toString())
            .name(m.getTopic())
            .mediaType(MediaType.TEXT_PLAIN_TYPE)
            .data(encodeResult(utils.objectToJson(m), accessToken))
            .build())
        .onCancellation().invoke(() -> Log.infof("SSE for topic=%s cancelled", topic))
        .onCompletion().invoke(() -> Log.infof("SSE for topic=%s completed", topic))
        .onFailure().invoke(t -> Log.errorf(t, "SSE for topic=%s failed" + topic));
  }

  private static boolean checkMessage(String topic, AccessToken accessToken, GatewayMessage m) {
    if (!topic.equals(m.getTopic())) {
      return false;
    }
    if (m.getType() == GatewayMessage.Type.USER) {
      return accessToken.getUserId().equals(m.getUserId());
    }
    if (m.getType() == GatewayMessage.Type.CLIENT) {
      return accessToken.getClientUUID().equalsIgnoreCase(m.getUuid().toString());
    }
    return true;
  }

  private AccessToken checkAccessToken(String requestId, String bearerToken, String accessToken) {
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      accessToken = bearerToken.substring(7); // 优先使用 http 头部认证信息。
    }
    return Optional.ofNullable(tokenUtils.verifyAccessToken(requestId, accessToken))
        .orElseThrow(
            () -> new WebApplicationException("Invalid access token", Response.Status.UNAUTHORIZED)
        );
  }

  private RealCallRequest checkRequestBody(String body, AccessToken accessToken) {
    try {
      final String callBody = securityUtils.decryptWithSecret(
          body, accessToken.getSharedSecret(), accessToken.getSharedInitializationVector());
      return utils.jsonToObject(callBody, RealCallRequest.class);
    } catch (Exception e) {
      throw new WebApplicationException("Invalid request body", e, Response.Status.NOT_ACCEPTABLE);
    }
  }

  private String encodeResult(String result, AccessToken accessToken) {
    return result == null
        ? "" // for empty result
        : securityUtils.encryptWithSecret(result, accessToken.getSharedSecret(), accessToken.getSharedInitializationVector());
  }
}
