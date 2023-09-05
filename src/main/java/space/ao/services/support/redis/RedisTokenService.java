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

import space.ao.services.support.OperationUtils;
import space.ao.services.support.model.AccessToken;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import space.ao.services.support.platform.info.push.UserIdAndClientUUID;

import java.time.ZonedDateTime;

/**
 * 使用 redis 管理短 token
 * 短 token = `TOKEN-{aoid}-{MD5(aoid + "-bp-" + secret).substring(0,20)}`, `TOKEN-` 为 网关和 nginx 加入，前端请求时不需要加入
 * <p>
 * 其中 secret 为 accessToken 对应的加密对称密钥。 例如： aoid 为 aoid1 ， 对称密钥为 IqNM95M7Iq ，
 * md5(aoid1-bp-IqNM95M7Iq)=af4f0f3627eaf087ed1568246bf053c1, Token 为 aoid1-af4f0f3627eaf087ed15
 * <p>
 * 局域网请求方式前端从协议上相当于直接对接微服务（不再通过网关 call 接口），前端请求时在 header 增加 accessToken 。
 */
@ApplicationScoped
public class RedisTokenService {
  @Inject
  OperationUtils utils;

  private static final String KEY_PREFIX = "TOKEN-";

  @Inject
  RedisCommonStringService redisCommonStringService;
  /**
   * set token to redis
   * @param token TOKEN key TOKEN-AOID-md5(secret)
   */
  public void set(String aoid, String secret, AccessToken token) {
    var expire = token.getExpiresAt().toEpochSecond() - ZonedDateTime.now().toEpochSecond();
    var key = generateKey(aoid, secret);
    var value = utils.objectToJson(UserIdAndClientUUID.of(token.getUserId(), token.getClientUUID()));
    redisCommonStringService.setex(key, value, expire);
  }

  public int delete(String aoid, String secret) {
    var key = generateKey(aoid, secret);
    return redisCommonStringService.del(key);
  }

  public void deleteByAoid(String aoid) {
    var keys = redisCommonStringService.keys(KEY_PREFIX + aoid + "*");
    for (var key: keys) {
      redisCommonStringService.del(key);
    }
  }

  public String get(String aoid, String secret) {
    var key = generateKey(aoid, secret);
    return redisCommonStringService.get(key);
  }

  String generateKey(String aoid, String secret) {
    return KEY_PREFIX + aoid + "-" + utils.encryptToMD5(aoid + "-bp-" + secret).substring(0,20);
  }
}
