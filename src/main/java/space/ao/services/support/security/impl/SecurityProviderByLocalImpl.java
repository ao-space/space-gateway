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

package space.ao.services.support.security.impl;

import com.google.common.io.CharStreams;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jboss.logging.Logger;
import space.ao.services.support.security.inf.SecurityProvider;
import space.ao.services.account.member.respository.BoxInfoRepository;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.OperationUtils;
import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import space.ao.services.support.security.SecurityProviderFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@ApplicationScoped
public class SecurityProviderByLocalImpl implements SecurityProvider {
  @Inject
  ApplicationProperties properties;
  @Getter
  private PrivateKey boxPrivateKey;
  @Getter
  private PublicKey boxPublicKey;
  private static final Logger LOG = Logger.getLogger("app.log");
  private static final String PROVIDER_NAME = "default";
  @Inject
  BoxInfoRepository boxInfoRepository;
  @Inject
  OperationUtils utils;

  @PostConstruct
  void init() {
    LOG.infov("SecurityUtilsByLocalImpl start init");
    try {
      try (Reader reader = getFileStreamReader(properties.boxPrivateKeyLocation())) {
        this.boxPrivateKey = getRSAPrivateKey(reader);
      }

      try (Reader reader = getFileStreamReader(properties.boxPublicKeyLocation())) {
        this.boxPublicKey = getRSAPublicKey(reader);
      }

    } catch (Exception e) {
      LOG.error("SecurityUtilsByLocalImpl init failed", e);
    }
    LOG.infov("SecurityUtilsByLocalImpl init completed");
    SecurityProviderFactory.putSecurityProvider(PROVIDER_NAME, this);
  }

  @SneakyThrows
  public String encryptUsingBoxPublicKey(String requestId, String body) {
    final Cipher dc = Cipher.getInstance(properties.gatewayAlgInfoPublicKeyAlgorithm());
    dc.init(Cipher.ENCRYPT_MODE, getBoxPublicKey());
    final byte[] encrypted = dc.doFinal(body.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(encrypted);
  }
  @SneakyThrows
  public String decryptUsingBoxPrivateKey(String requestId, String body) {
    final Cipher dc = Cipher.getInstance(properties.gatewayAlgInfoPublicKeyAlgorithm());
    dc.init(Cipher.DECRYPT_MODE, getBoxPrivateKey());
    final byte[] decrypted = dc.doFinal(Base64.getDecoder().decode(body));
    return new String(decrypted, StandardCharsets.UTF_8);
  }

  @SneakyThrows
  public String signUsingBoxPrivateKey(String requestId, String data) {
    Signature privateSignature = Signature.getInstance(properties.gatewayAlgInfoSignatureAlgorithm());
    privateSignature.initSign(getBoxPrivateKey());
    privateSignature.update(Base64.getDecoder().decode(data.getBytes(StandardCharsets.UTF_8)));
    byte[] signature = privateSignature.sign();
    return Base64.getEncoder().encodeToString(signature);
  }
  @SneakyThrows
  public String signByUrlEncodeUsingBoxPrivateKey(String requestId, String data) {
    Signature privateSignature = Signature.getInstance(properties.gatewayAlgInfoSignatureAlgorithm());
    privateSignature.initSign(getBoxPrivateKey());
    privateSignature.update(Base64.getDecoder().decode(data.getBytes(StandardCharsets.UTF_8)));
    byte[] signature = privateSignature.sign();
    return Base64.getUrlEncoder().encodeToString(signature);
  }

  @SneakyThrows
  public boolean verifySignUsingBoxPublicKey(String requestId, String data, String signature) {
    Signature sign = Signature.getInstance(properties.gatewayAlgInfoSignatureAlgorithm());
    sign.initVerify(getBoxPublicKey());
    sign.update(data.getBytes(StandardCharsets.UTF_8));
    return sign.verify(Base64.getDecoder().decode(signature));
  }

  @SneakyThrows
  public PublicKey getBoxPublicKey(String requestId) {
    return boxPublicKey;
  }

  @Override
  public boolean setPasscode(String requestId, String password) {
    return boxInfoRepository.setPasscode(requestId, password);
  }

  @Override
  public String getPasscode(String requestId) {
    return boxInfoRepository.getPasscode(requestId);
  }

  @Override
  public boolean resetPasscode(String requestId) {
    return setPasscode(requestId, null);
  }

  public Reader getFileStreamReader(String resourceLocation) {
    return utils.getFileStreamReader(resourceLocation);
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

  /**
   * Read from a pem text reader and returns a RSA public key based on X.509 standard format.
   */
  public RSAPublicKey getRSAPublicKey(Reader reader) throws GeneralSecurityException, IOException {
    return utils.getRSAPublicKey(reader);
  }


}
