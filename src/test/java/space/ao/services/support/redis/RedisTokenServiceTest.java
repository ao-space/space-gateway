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

package space.ao.services.support.redis;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.ao.services.account.member.respository.BoxInfoRepository;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.gateway.auth.CreateTokenResult;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.TokenUtils;
import space.ao.services.support.model.AlgorithmConfig;
import space.ao.services.support.security.SecurityUtils;
import space.ao.services.support.test.TestUtils;

import javax.annotation.Nullable;
import javax.crypto.spec.IvParameterSpec;
import jakarta.inject.Inject;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Objects;

@QuarkusTest
class RedisTokenServiceTest {
  @Inject
  RedisTokenService redisTokenService;
  @Inject
  TokenUtils tokenUtils;
  @Inject
  TestUtils testUtils;
  @Inject
  BoxInfoRepository boxInfoRepository;
  @Inject
  OperationUtils utils;
  @BeforeEach
  void setUp() {
    boxInfoRepository.insertOrUpdate("boxregkey-1", utils.string2SHA256("000000"));
    testUtils.createAdmin();
  }

  @AfterEach
  @Transactional
  void tearDown() {
    testUtils.deleteAllUser();
  }


  @Test
  void test() {
    var createTokenResult = tokenUtils.createDefaultTokenResult("test-1-1-1", null, "1",
            "clientUUId", null);
    var token = createTokenResult.getAccessToken();
    var accessToken = tokenUtils.checkAccessToken("", token);
    redisTokenService.set("aoid-1", "secret", accessToken);
    redisTokenService.set("aoid-1", "secret-1", accessToken);

    redisTokenService.set("aoid-2", "secret", accessToken);
    Assertions.assertTrue(Objects.nonNull(redisTokenService.get("aoid-1", "secret")));
    Assertions.assertTrue(Objects.nonNull(redisTokenService.get("aoid-1", "secret-1")));

    redisTokenService.deleteByAoid("aoid-1");
    Assertions.assertTrue(Objects.isNull(redisTokenService.get("aoid-1", "secret")));
    Assertions.assertTrue(Objects.isNull(redisTokenService.get("aoid-1", "secret-1")));
    Assertions.assertTrue(Objects.nonNull(redisTokenService.get("aoid-2", "secret")));
  }

  @Test
  @SneakyThrows
  void TestExpire(){
    var createTokenResult = tokenUtils.createDefaultTokenResult("test-1-1-1", null, "1",
            "clientUUId", null);
    var secret = testUtils.decryptUsingClientPrivateKey(createTokenResult.getEncryptedSecret());
    Assertions.assertTrue(Objects.nonNull(redisTokenService.get("aoid-1", secret)));
    System.out.println(redisTokenService.get("aoid-1", secret));
    redisTokenService.delete("aoid-1", secret);
    Assertions.assertTrue(Objects.isNull(redisTokenService.get("aoid-1", secret)));
    var createTokenResult1 = createDefaultTokenResult("test-1-1-1", null, "1",
            "clientUUId", null);
    var secret1 = testUtils.decryptUsingClientPrivateKey(createTokenResult1.getEncryptedSecret());
    Assertions.assertTrue(Objects.nonNull(redisTokenService.get("aoid-1", secret1)));
    Thread.sleep(3000);
    var token = redisTokenService.get("aoid-1", secret1);
    Assertions.assertTrue(Objects.isNull(token));
  }

  @Inject
  OperationUtils operationUtils;
  @Inject
  SecurityUtils securityUtils;
  @Inject
  ApplicationProperties properties;
  public CreateTokenResult createDefaultTokenResult(String requestId,
                                                    @Nullable String tempEncryptedSecret, String userId, String clientUUID, @Nullable TokenUtils.OpenApiArg openApiArg) {
    final String secret = operationUtils.unifiedRandomCharters(properties.gatewayAlgInfoTransportationKeySize());
    String sharedSecret;
    byte[] ivBytes = new byte[16];
    SecureRandom random = new SecureRandom();
    random.nextBytes(ivBytes);
    var iv = new IvParameterSpec(ivBytes);

    if (tempEncryptedSecret == null) {
      sharedSecret = securityUtils.encryptUsingClientPublicKey(secret);
      ivBytes = operationUtils.createRandomBytes(16);
    } else {
      String swapSecret = securityUtils.getSecurityProvider().decryptUsingBoxPrivateKey(requestId, tempEncryptedSecret);
      sharedSecret = securityUtils.encryptWithSecret(secret, swapSecret, iv);
    }

    final String initializationVector = Base64.getEncoder().encodeToString(ivBytes);
    final ZonedDateTime expiresAt = ZonedDateTime.now().plusSeconds(3);
    final String ak = tokenUtils.createAccessToken(requestId, userId, expiresAt, secret, initializationVector, clientUUID, openApiArg);
    final String rft = tokenUtils.createRefreshToken(requestId, userId,
            ZonedDateTime.now()
                    .plusSeconds(Duration.parse(properties.gatewayTimeOfRakLife()).getSeconds()),clientUUID, openApiArg);

    final AlgorithmConfig algorithmConfig = AlgorithmConfig.of(
            properties.gatewayAlgInfoPublicKeyAlgorithm(),
            properties.gatewayAlgInfoPublicKeyKeySize(),
            properties.gatewayAlgInfoTransportationAlgorithm(),
            properties.gatewayAlgInfoTransportationKeySize(),
            properties.gatewayAlgInfoTransportationTransformation(),
            initializationVector
    );

    redisTokenService.set("aoid-" + userId, secret, tokenUtils.checkAccessToken(requestId, ak));

    return CreateTokenResult.of(ak, rft, algorithmConfig,
            sharedSecret, expiresAt.toString(), expiresAt.toEpochSecond(), requestId);
  }
}
