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

import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import space.ao.services.support.redis.message.ReceiveMessage;
import space.ao.services.support.redis.message.RedisMessageService;
import space.ao.services.support.redis.message.SendMessage;

@QuarkusTest
class RedisMessageServiceTest {

  @Inject
  RedisMessageService redisMessageService;

  @Test
  void test() {
    redisMessageService.push("test", new SendMessage("1", "1", "1", "req", "1"));
    redisMessageService.push("test", new SendMessage("1", "1", "1", "req", "1"));
    redisMessageService.push("test", new SendMessage("1", "1", "1", "req", "1"));

    var messages = redisMessageService.getMessage("test", null, 1000L);
    for (var message: messages){
      Assertions.assertTrue(readMessage(message));
    }
    messages = redisMessageService.getMessage("test", 1, 1000L);
    Assertions.assertTrue(messages.isEmpty());
  }

  boolean readMessage(ReceiveMessage message){
    Log.info("message: " + message);
    return redisMessageService.del("test", message.messageId());
  }

}
