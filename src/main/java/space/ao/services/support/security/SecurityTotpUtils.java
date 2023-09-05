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

import jakarta.enterprise.context.ApplicationScoped;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.SneakyThrows;
import space.ao.services.gateway.auth.qrcode.dto.TotpAuthCode;
import space.ao.services.support.log.Logged;

import java.time.Duration;
import java.time.Instant;

@ApplicationScoped
public class SecurityTotpUtils {

  @Logged
  public String generateTotpSecret(){
    SecretGenerator secretGenerator = new DefaultSecretGenerator();
    return secretGenerator.generate();
  }

  /**generate
   * 生成基于时间的一次性密码
   */
  @SneakyThrows
  @Logged
  public TotpAuthCode generateCode(String secret) {
    CodeGenerator codeGenerator = new DefaultCodeGenerator();
    var authCodeTotalExpiresAt = Duration.ofSeconds(30L).toMillis();
    return TotpAuthCode.of(codeGenerator.generate(secret, Instant.now().toEpochMilli() / authCodeTotalExpiresAt),
            authCodeTotalExpiresAt - Instant.now().toEpochMilli() % authCodeTotalExpiresAt,
            authCodeTotalExpiresAt);
  }

  public Boolean verifyCode(String secret, String code){
    TimeProvider timeProvider = new SystemTimeProvider();
    CodeGenerator codeGenerator = new DefaultCodeGenerator();
    CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    return verifier.isValidCode(secret, code);
  }
}
