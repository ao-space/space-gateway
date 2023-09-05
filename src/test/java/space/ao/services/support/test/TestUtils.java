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

package space.ao.services.support.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import io.quarkus.logging.Log;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import space.ao.services.account.authorizedterminalinfo.repository.AuthorizedTerminalRepository;
import space.ao.services.account.member.dto.ClientPairStatusEnum;
import space.ao.services.account.member.dto.Const;
import space.ao.services.account.support.service.AdminInfoFileDTO;
import space.ao.services.account.support.service.ServiceDefaultVar;
import space.ao.services.account.support.service.ServiceError;
import space.ao.services.support.FileUtils;
import space.ao.services.support.security.impl.SecurityProviderByLocalImpl;
import space.ao.services.account.member.service.MemberManageService;
import space.ao.services.account.personalinfo.entity.UserEntity;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.gateway.CallRequest;
import space.ao.services.gateway.RealCallRequest;
import space.ao.services.gateway.RealCallResult;
import space.ao.services.gateway.auth.CreateTokenInfo;
import space.ao.services.gateway.auth.CreateTokenResult;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.model.AccessToken;
import space.ao.services.support.security.SecurityUtils;

import javax.crypto.Cipher;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static space.ao.services.support.RestConfiguration.REQUEST_ID;

@Singleton
public class TestUtils {
  @Inject
  SecurityUtils securityUtils;
  @Inject
  MemberManageService memberManageService;
  @Inject
  UserInfoRepository userInfoRepository;
  @Inject
  AuthorizedTerminalRepository authorizedTerminalRepository;
  @Inject
  OperationUtils utils;

  @Inject
  ObjectMapper objectMapper;
  @Inject
  ApplicationProperties properties;
  @Getter
  public UserEntity adminInfo;

  @Inject
  SecurityProviderByLocalImpl securityProvider;

  @Transactional
  public UserEntity createAdmin() {
    var userEntityAdmin = userInfoRepository.findByRole(UserEntity.Role.ADMINISTRATOR);

    if (userEntityAdmin != null) {
      userEntityAdmin.setAuthKey("authKey");
      userEntityAdmin.setClientUUID("clientUUID");
      userEntityAdmin.setPhoneModel("android");
    } else {
      userEntityAdmin = new UserEntity(UserEntity.Role.ADMINISTRATOR, "test", "domain.eulix.xyz", "aoid-1");
      userEntityAdmin.setAuthKey("authKey");
      userEntityAdmin.setClientUUID("clientUUID");
      userEntityAdmin.setPhoneModel("android");
      userEntityAdmin.setPersonalName("hello");

      //创建管理员头像路径
      var adminImageFile = new File(properties.accountImageLocation() + Const.Admin.ADMIN_AOID);
      if(adminImageFile.exists() || adminImageFile.mkdirs()) {
        var imagePath = ServiceDefaultVar.DEFAULT_IMAGE_PATH + ServiceDefaultVar.DEFAULT_AVATAR_FILE.toString();
        var defaultImage = new File(adminImageFile, ServiceDefaultVar.DEFAULT_AVATAR_FILE.toString());
        FileUtils.saveFileToLocal(imagePath, defaultImage);
        try{
          userEntityAdmin.setImageMd5(DigestUtils.md5Hex(new FileInputStream(defaultImage)));
        } catch (Exception e) {
          Log.error("get admin default image md5 failed");
          userEntityAdmin.setImageMd5(null);
        }
        userEntityAdmin.setImage(defaultImage.getPath());
      } else {
        Log.errorv("create admin image path failed, path: {0}", adminImageFile.getPath());
      }

      userEntityAdmin = userInfoRepository.insertAdminUser(userEntityAdmin);
      userEntityAdmin.setId(1L);
      userInfoRepository.update("set id=1 where role=?1", UserEntity.Role.ADMINISTRATOR);
      userEntityAdmin.setAoId("aoid-" + userEntityAdmin.getId());
    }

    createAdminInfoFile();
    memberManageService.writeToAdminFile(AdminInfoFileDTO.of(
            userEntityAdmin.getClientUUID(), userEntityAdmin.getAuthKey(), userEntityAdmin.getPhoneModel(), properties.boxName(),
            ClientPairStatusEnum.CLIENT_PAIRED.getStatus(), userEntityAdmin.getApplyEmail(), userEntityAdmin.getUserDomain()));
    return userEntityAdmin;
  }

  public void createAdminInfoFile(){
    var file = new File(properties.accountDataLocation());
    if(file.exists() || file.mkdirs()) {
      Log.error(ServiceError.CREATE_ADMIN_INIT_FAILED);
    }
    var data = new File(properties.accountDataLocation() + ServiceDefaultVar.DEFAULT_DATA_FILE);
    if(!data.exists()) {
      Log.info("admin file create");
      try {
        if(!data.createNewFile()){
          Log.error(ServiceError.CREATE_ADMIN_INIT_FAILED);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      Map<String, String> mapInfo = new HashMap<>();
      mapInfo.put("status", "1");
      utils.writeToFile(data, mapInfo);
    }
  }

  @Transactional
  public void deleteAllUser() {
    userInfoRepository.deleteAll();
    authorizedTerminalRepository.deleteAll();
  }

  @Transactional
  public void cleanData() {
    try {
      utils.deleteFileDir(properties.accountDataLocation());
      utils.deleteFileDir(properties.accountImageLocation());
    } catch (Exception e){
      Log.warn(e);
    }
  }

  public CreateTokenInfo getAdminCreateTokenTestInfo(String requestId) {
    adminInfo = memberManageService.findByUserId(Const.Admin.ADMIN_ID);
    CreateTokenInfo info = new CreateTokenInfo();
    {
      info.setEncryptedAuthKey(securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(requestId, adminInfo.getAuthKey()));
      info.setEncryptedClientUUID(securityUtils.getSecurityProvider().encryptUsingBoxPublicKey(requestId, adminInfo.getClientUUID()));
    }
    return info;
  }

  public CreateTokenResult getCreateTokenResult(String reqId) {
    final CreateTokenInfo info = getAdminCreateTokenTestInfo(reqId);
    final Response tokenResp = given()
            .header(REQUEST_ID, utils.createRandomType4UUID())
            .body(info)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/gateway/auth/token/create");

    return tokenResp.body().as(CreateTokenResult.class);
  }

  public  <T> T call(AccessToken ak, RealCallRequest real, String reqId, TypeReference<T> valueTypeRef) throws JsonProcessingException {
    CallRequest call = new CallRequest(securityUtils.encryptWithSecret(
            utils.objectToJson(real), ak.getSharedSecret(), ak.getSharedInitializationVector()), ak.getToken());

    final Response callResp = given()
            .header(REQUEST_ID, reqId)
            .body(call)
            .contentType(ContentType.JSON)
            .when()
            .post("/v1/api/gateway/call");

    final RealCallResult callRet = callResp.body().as(RealCallResult.class);
    String result = securityUtils.decryptWithSecret(callRet.body(), ak.getSharedSecret(), ak.getSharedInitializationVector());
    return objectMapper.readValue(result, valueTypeRef);
  }

  @SneakyThrows
  public String decryptUsingClientPrivateKey(String body) {
    final Cipher dc = Cipher.getInstance(properties.gatewayAlgInfoPublicKeyAlgorithm());
    dc.init(Cipher.DECRYPT_MODE, getClientPrivateKey());
    final byte[] decrypted = dc.doFinal(Base64.getDecoder().decode(body));
    return new String(decrypted, StandardCharsets.UTF_8);
  }
  @SneakyThrows
  public String encryptUsingBoxPublicKey(String requestId, String body) {
    final Cipher dc = Cipher.getInstance(properties.gatewayAlgInfoPublicKeyAlgorithm());
    dc.init(Cipher.ENCRYPT_MODE, getBoxPublicKey(requestId));
    final byte[] encrypted = dc.doFinal(body.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(encrypted);
  }

  @SneakyThrows
  private PrivateKey getClientPrivateKey() {
    try (Reader reader = utils.getFileStreamReader(properties.clientPrivateKeyLocation())) {
      return getRSAPrivateKey(reader);
    }
  }
  @SneakyThrows
  private PublicKey getBoxPublicKey(String requestId) {
    return securityProvider.getBoxPublicKey(requestId);
  }

  /**
   * Read from pem text reader and returns a RSA private key based on PKCS#8 standard format.
   */
  public RSAPrivateKey getRSAPrivateKey(Reader reader)
          throws GeneralSecurityException, IOException {
    final String key = CharStreams.toString(reader);

    final String pem = key.replaceAll("[\\n\\r]", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "");

    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pem));
    return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
  }
}
