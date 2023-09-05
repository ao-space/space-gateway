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

package space.ao.services.support.security;

import lombok.Getter;
import lombok.SneakyThrows;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import space.ao.services.support.agent.AgentServiceRestClient;
import space.ao.services.support.agent.info.DeviceInfo;
import space.ao.services.support.security.inf.SecurityProvider;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.log.Logged;

import jakarta.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

@Singleton
public class SecurityUtils {
  private static final Logger LOG = Logger.getLogger("app.log");

  @Getter
  @Inject
  SecurityProvider securityProvider;
  @Inject
  ApplicationProperties properties;
  @Getter
  public PublicKey adminClientPublicKey;
  @Inject
  OperationUtils operationUtils;
  @Inject
  @RestClient
  AgentServiceRestClient agentServiceRestClient;

  private DeviceInfo deviceInfo;

  @PostConstruct
  public void init() throws IOException, GeneralSecurityException {

    LOG.infov("SecurityUtils start init");
    try {
      loadAdminClientPublicFile();
      loadDeviceInfo();
    } catch (Exception e) {
      LOG.error("SecurityUtils init failed", e);
    }
    LOG.infov("SecurityUtils init completed");
  }

  public void loadAdminClientPublicFile(){
    try (Reader reader = operationUtils.getFileStreamReader(properties.clientPublicKeyLocation())) {
      adminClientPublicKey = getRSAPublicKey(reader);
    } catch (Exception e) {
      LOG.warnv("loadAdminClientPublicFile failed，Location {0} has no public key file", properties.clientPublicKeyLocation());
    }
  }

  public void loadDeviceInfo(){
    try {
      deviceInfo = agentServiceRestClient.getDeviceVersion().results();
    } catch (Exception e) {
      LOG.warnv("loadDeviceInfo failed", properties.systemAgentUrlBase());
    }
  }

  public DeviceInfo getDeviceInfo() {
    if(Objects.isNull(deviceInfo)){
      loadDeviceInfo();
    }
    return deviceInfo;
  }

  @Logged
  public String getPasscode(String requestId){
    return securityProvider.getPasscode(requestId);
  }

  @Logged
  public boolean setPasscode(String requestId, String passcode){
    return securityProvider.setPasscode(requestId, passcode);
  }

  @Logged
  public boolean resetPasscode(String requestId){
    return securityProvider.resetPasscode(requestId);
  }

  /**
   * Read from a pem text reader and returns a RSA public key based on X.509 standard format.
   */
  public RSAPublicKey getRSAPublicKey(Reader reader) throws GeneralSecurityException, IOException {
    return operationUtils.getRSAPublicKey(reader);
  }

  @SneakyThrows
  public String decryptWithSecret(String body, String secret, IvParameterSpec iv) {
    byte[] key = secret.getBytes(StandardCharsets.UTF_8);
    SecretKeySpec secretKey = new SecretKeySpec(key,
            properties.gatewayAlgInfoTransportationAlgorithm());
    Cipher cipher = Cipher.getInstance(properties.gatewayAlgInfoTransportationTransformation());
    byte[] result = new byte[0];
    try {
      cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
      result = cipher.doFinal(Base64.getDecoder().decode(body));
    } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException |
             BadPaddingException e) {
      LOG.error("secret:" + secret + ", iv: " + Arrays.toString(iv.getIV()), e);
    }
    return new String(result, StandardCharsets.UTF_8);
  }

  @SneakyThrows
  public String encryptWithSecret(String body, String secret, IvParameterSpec iv) {
    byte[] key = secret.getBytes(StandardCharsets.UTF_8);
    SecretKeySpec secretKey = new SecretKeySpec(key,
            properties.gatewayAlgInfoTransportationAlgorithm());
    Cipher cipher = Cipher.getInstance(properties.gatewayAlgInfoTransportationTransformation());
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
    return Base64.getEncoder()
            .encodeToString(cipher.doFinal(body.getBytes(StandardCharsets.UTF_8)));
  }

  @SneakyThrows
  public String encryptUsingClientPublicKey(String body) {
    final Cipher dc = Cipher.getInstance(properties.gatewayAlgInfoPublicKeyAlgorithm());
    dc.init(Cipher.ENCRYPT_MODE, getAdminClientPublicKey());
    final byte[] encrypted = dc.doFinal(body.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(encrypted);
  }

  /**
   * 获取字符串形式盒子公钥
   */
  public String getBoxPublicKey(String requestId) {
    String publicKeyString = Base64.getEncoder().encodeToString(securityProvider.getBoxPublicKey(requestId).getEncoded());
    String[] split = publicKeyString.split("(?<=\\G.{64})");
    StringBuilder sb = new StringBuilder("-----BEGIN PUBLIC KEY-----\n");
    for (String s : split) {
      sb.append(s).append("\n");
    }
    sb.append("-----END PUBLIC KEY-----");
    publicKeyString = sb.toString();
    return publicKeyString;
  }
}
