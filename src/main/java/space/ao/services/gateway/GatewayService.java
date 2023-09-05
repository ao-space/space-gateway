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

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.StreamingOutput;
import lombok.SneakyThrows;
import okhttp3.*;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import org.jboss.logging.Logger;
import space.ao.services.account.authorizedterminalinfo.entity.AuthorizedTerminalEntity;
import space.ao.services.account.authorizedterminalinfo.repository.AuthorizedTerminalRepository;
import space.ao.services.account.member.service.MemberManageService;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.TokenUtils;
import space.ao.services.support.log.Logged;
import space.ao.services.support.model.AccessToken;
import space.ao.services.support.platform.PlatformUtils;
import space.ao.services.support.service.ServiceError;
import space.ao.services.support.service.ServiceOperationException;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.*;

import static space.ao.services.support.RestConfiguration.REQUEST_ID;

@ApplicationScoped
@Startup
public class GatewayService {
  static final Logger LOG = Logger.getLogger("app.log");

  @Inject OkHttpClient httpClient;
  static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  static final MediaType MULTIPART_FORM = MediaType.get("multipart/form-data");
  static final MediaType OCTET = MediaType.parse("application/octet-stream");

  @Inject
  OperationUtils utils;
  @Inject
  TokenUtils tokenUtils;
  @Inject
  PlatformUtils platformUtils;
  @Inject
  MemberManageService memberManageService;
  @Inject
  ApplicationProperties properties;
  @Inject
  AuthorizedTerminalRepository authorizedTerminalRepository;

  @Inject
  Routers routers;

  @Logged
  @SneakyThrows
  public String handleCall(String requestId, RealCallRequest request, AccessToken accessToken) {
    final var router =
        routers.getServices().get(request.getServiceName()).get(request.getApiName());

    var user = checkIllegalApiAccess(accessToken, router);

    Map<String, String> query = new HashMap<>();
    if (request.getQueries() != null) {
      query.putAll(request.getQueries());
    }
    query.put(AccessToken.USER_ID, accessToken.getUserId());
    query.put("AccessToken-" +AccessToken.CLIENT_UUID, accessToken.getClientUUID());
    if (user.getSpaceLimit() != null) {
      query.put("spaceLimit", String.valueOf(user.getSpaceLimit()));
    }

    handlePlatformRequest(requestId, request, query, accessToken);

    return execCall(
        router,
        query,
        request.getHeaders(),
        request.getEntity(),
        requestId
    );
  }

  private void handlePlatformRequest(String requestId, RealCallRequest request, Map<String, String> query, AccessToken accessToken) {
    if(request.getServiceName().equals("eulixspace-platform-service")){
      var boxRegKey = platformUtils.createRegistryBoxRegKey(requestId);
      var headers = request.getHeaders();
      headers.put("Box-Reg-Key", boxRegKey);
      request.setHeaders(headers);
    }
    if(request.getServiceName().equals("eulixspace-opstage-service")){
      handleOpstagePlatformRequest(requestId, request, query, accessToken);
    }
  }

  @Logged
  public void handleOpstagePlatformRequest(String requestId, RealCallRequest request, Map<String, String> query, AccessToken accessToken) {
    var boxRegKey = platformUtils.createOpstageBoxRegKey(requestId);
    var headers = request.getHeaders();
    headers.put("Box-Reg-Key", boxRegKey);
    request.setHeaders(headers);
    if(Objects.equals(request.getApiName(), "trail_invites")){
      var body = utils.objectToMap(request.getEntity());
      body.put("userId", "aoid-" + accessToken.getUserId());
      request.setEntity(body);
    } else if (Objects.equals(request.getApiName(), "trail_invites_accept")){
      query.put("user_id", "aoid-" + accessToken.getUserId());
    }
  }


  @Logged(enablePreLog = false)
  @SneakyThrows
  public String handleUpload(String requestId, RealCallRequest request, InputStream file, AccessToken accessToken) {
    final var router =
        routers.getServices().get(request.getServiceName()).get(request.getApiName());

    checkIllegalApiAccess(accessToken, router);

    Map<String, String> query = new HashMap<>();
    if (request.getQueries() != null) {
      query.putAll(request.getQueries());
    }
    query.put(AccessToken.USER_ID, accessToken.getUserId());

    return execUpload(
        router,
        query,
        request.getHeaders(),
        request.getEntity(),
        file,
        requestId,
        accessToken
    );
  }

  @Logged
  @SneakyThrows
  public FileResult handleDownload(String requestId, RealCallRequest request, AccessToken accessToken) {
    final var router =
        routers.getServices().get(request.getServiceName()).get(request.getApiName());

    checkIllegalApiAccess(accessToken, router);

    Map<String, String> query = new HashMap<>();
    if (request.getQueries() != null) {
      query.putAll(request.getQueries());
    }
    query.put(AccessToken.USER_ID, accessToken.getUserId());

    return execDownload(
        router,
        query,
        request.getHeaders(),
        request.getEntity(),
        requestId,
        accessToken
    );
  }

  private FileResult execDownload(Routers.Router router, Map<String, String> queries, Map<String, String> headers,
      Object entity, String requestId, AccessToken accessToken) throws IOException {
    if (!"HTTP".equalsIgnoreCase(router.getProtocol())) {
      throw new ServiceOperationException(500, "execDownload no support yet for protocol - " + router.getProtocol());
    }

    var builder = createBuilder(router, queries, headers, requestId);
    Request request;
    if ("GET".equalsIgnoreCase(router.getMethod())) {
      // response 再 transfer 后才被关闭。
      request = builder.build();
    } else if("POST".equals(router.getMethod())){
      var body = RequestBody.create(
          JSON, entity == null ? "" : utils.objectToJson(entity));
      // response 再 transfer 后才被关闭。
      request = builder.post(body).build();
    } else {
      throw new ServiceOperationException(500, "execDownload invalid http method - " + router.getMethod());
    }
    var response = httpClient.newCall(request).execute();
    {
      final StreamingOutput newFile = outputStream -> {
        try (response) {
          if (response.body() != null) {
            transfer(accessToken, Okio.buffer(Okio.sink(outputStream)), response.body().source());
          }
        }
      };
      long newSize = 0;
      var contentLength = response.header("Content-Length");

      if(Objects.nonNull(contentLength)){
        // convert to encrypted file size
        var fileSize = Long.parseLong(contentLength);
        newSize = fileSize + (16 - (fileSize % 16));
      }

      Map<String, String> newHeaders = new HashMap<>();
      response.headers().names().forEach(name -> {
        if (!name.equalsIgnoreCase("Content-Length")) {
          newHeaders.put(name, response.headers().get(name));
        }
      });
      return FileResult.of(response.code(), newFile, newSize, newHeaders);
    }
  }

  @SneakyThrows
  private void transfer(AccessToken accessToken, BufferedSink sink, BufferedSource source) {
    var cipher = tokenUtils.createAndInitCipherWithAccessToken(accessToken, Cipher.ENCRYPT_MODE);
    try (sink) {
      byte[] buffer = new byte[DEFAULT_BUF_SIZE];
      int bytesRead;
      while ((bytesRead = source.read(buffer)) != -1) {
        byte[] output = cipher.update(buffer, 0, bytesRead);
        if (output != null) {
          sink.write(output);
        }
      }
      var outputBytes = cipher.doFinal();
      if (outputBytes != null) {
        sink.write(outputBytes);
      }
    } catch (Exception e) {
      LOG.error("download transfer error", e);
    }
  }

  // 2K chars (4K bytes)
  private static final int DEFAULT_BUF_SIZE = 0x800;

  private String execUpload(Routers.Router router, Map<String, String> queries, Map<String, String> headers,
                            Object entity, final InputStream file, String requestId, AccessToken accessToken)
      throws IOException
  {
    if (!"HTTP".equalsIgnoreCase(router.getProtocol())) {
      throw new ServiceOperationException(500, "execUpload no support yet for protocol - " + router.getProtocol());
    }

    if (!"PUT".equalsIgnoreCase(router.getMethod()) && !"POST".equalsIgnoreCase(router.getMethod())) {
      throw new ServiceOperationException(500, "execUpload invalid http method - " + router.getMethod());
    }

    var builder = createBuilder(router, queries, headers, requestId);
    RequestBody body = new RequestBody() {
      @Override public MediaType contentType() {
        return OCTET;
      }
      @SneakyThrows @Override public void writeTo(@SuppressWarnings("NullableProblems") BufferedSink sink) {
        var cipher = tokenUtils.createAndInitCipherWithAccessToken(accessToken, Cipher.DECRYPT_MODE);
        try (file) {
          var buffer = new byte[DEFAULT_BUF_SIZE];
          int bytesRead;
          while ((bytesRead = file.read(buffer)) != -1) {
            byte[] output = cipher.update(buffer, 0, bytesRead);
            if (output != null) {
              sink.write(output);
            }
          }
          byte[] outputBytes = cipher.doFinal();
          if (outputBytes != null) {
            sink.write(outputBytes);
          }
        }
      }
    };

    // By default, we use "multipart" as ext protocol
    if (router.getExtProtocol() == null || "multipart".equals(router.getExtProtocol())) {
      final UploadEntity ue = utils.jsonToObject(utils.objectToJson(entity), UploadEntity.class);
      // Convert "onepart" body to multipart.
      body = new MultipartBody.Builder()
          .setType(MULTIPART_FORM)
          .addFormDataPart("param", utils.objectToJson(entity))
          .addFormDataPart("file", ue.getFilename(), body)
          .build();
    }

    Request request;
    if ("PUT".equalsIgnoreCase(router.getMethod())) {
      request = builder.put(body).build();
    } else {
      request = builder.post(body).build();
    }
    try (var response = httpClient.newCall(request).execute()) {
      return response.body() == null ? null : response.body().string();
    }
  }

  private Request.Builder createBuilder(Routers.Router router, Map<String, String> queries, Map<String, String> headers,
                                        String requestId) {
    String url = router.getUrl();
    if(headers != null && headers.containsKey("Box-Reg-Key")){
      url = properties.ssplatformUrl() + router.getUrl();
    }
    var urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();

    if (queries != null && !queries.isEmpty()) {
      for (Map.Entry<String, String> pair : queries.entrySet()) {
        urlBuilder.addQueryParameter(pair.getKey(), pair.getValue());
      }
    }

    var builder =
        new Request.Builder()
            .url(urlBuilder.build())
            .addHeader(REQUEST_ID, requestId);

    if (headers != null && !headers.isEmpty()) {
      for (Map.Entry<String, String> pair : headers.entrySet()) {
        builder.addHeader(pair.getKey(), pair.getValue());
      }
    }
    return builder;
  }

  private String execCall(Routers.Router router, Map<String, String> queries, Map<String, String> headers,
                          Object entity, String requestId)
      throws IOException
  {
    if (!"HTTP".equalsIgnoreCase(router.getProtocol())) {
      throw new ServiceOperationException(500, "execCall no support yet for protocol - " + router.getProtocol());
    }

    var builder = createBuilder(router, queries, headers, requestId);

    var request = builder.build(); // build as GET by default

    if ("HEAD".equalsIgnoreCase(router.getMethod())) {
      request = builder.head().build();
    }

    else if ("TAG".equalsIgnoreCase(router.getMethod())) {
      request = builder.tag(entity).build();
    }

    else if ("POST".equalsIgnoreCase(router.getMethod())) {
      var body = RequestBody.create(
          JSON, entity == null ? "" : utils.objectToJson(entity));
      request = builder.post(body).build();
    }

    else if ("DELETE".equalsIgnoreCase(router.getMethod())) {
      var body = RequestBody.create(
          JSON, entity == null ? "" : utils.objectToJson(entity));
      request = builder.delete(body).build();
    }

    else if ("PUT".equalsIgnoreCase(router.getMethod())) {
      var body = RequestBody.create(
          JSON, entity == null ? "" : utils.objectToJson(entity));
      request = builder.put(body).build();
    }

    else if ("PATCH".equalsIgnoreCase(router.getMethod())) {
      var body = RequestBody.create(
          JSON, entity == null ? "" : utils.objectToJson(entity));
      request = builder.patch(body).build();
    }

    try (var response = httpClient.newCall(request).execute()) {
      return response.body() == null ? null : response.body().string();
    }
  }

  @Logged
  public boolean verifyClient(String clientUUID, UserEntity userEntity){
    if (Objects.equals(userEntity.getClientUUID(), clientUUID)) {
      if(Objects.isNull(userEntity.getAuthKey())){
        throw new ServiceOperationException(ServiceError.ACCESS_TOKEN_INVALID);
      }
      return true;
    }

    List<AuthorizedTerminalEntity> authorizedTerminalEntityList =
          authorizedTerminalRepository.findByUserid(userEntity.getId());

    if(!authorizedTerminalEntityList.isEmpty()){
      for (var entity: authorizedTerminalEntityList
      ) {
        if(Objects.equals(entity.getUuid(), clientUUID)){
          if(entity.getExpireAt().isBefore(OffsetDateTime.now())){
            throw new ServiceOperationException(ServiceError.ACCESS_TOKEN_INVALID);
          }
          return true;
        }
      }
    }

    return false;
  }

  private boolean inValidOpenApiScope(AccessToken accessToken, Routers.Router router) {
    final Routers.OpenApi openApi = router.getOpenApi();
    final Set<String> openApiScopes = accessToken.getOpenApiScopes();
    return !openApiScopes.contains(openApi.getScope());
  }

  private UserEntity checkIllegalApiAccess(AccessToken accessToken, Routers.Router router) {
    var userEntity = memberManageService.findByUserId(accessToken.getUserId());
    if (!verifyClient(accessToken.getClientUUID(), userEntity)) {
      throw new ServiceOperationException(500, "User id and clientUUID don't match");
    }
    if (accessToken.isOpenApi()) {
      if (router.getOpenApi() == null || inValidOpenApiScope(accessToken, router)) {
        throw new ServiceOperationException(403, "Access to data without permission is forbidden");
      }
    }
    return userEntity;
  }
}
