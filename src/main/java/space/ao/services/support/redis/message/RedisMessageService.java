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

package space.ao.services.support.redis.message;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.stream.StreamCommands;
import io.quarkus.redis.datasource.stream.StreamMessage;
import io.quarkus.redis.datasource.stream.XReadArgs;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.codec.binary.Base64;
import org.jboss.logging.Logger;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ApplicationScoped
public class RedisMessageService {

  static final Logger LOG = Logger.getLogger("redis_message.log");

  private final StreamCommands<String, String, String> streamCommands;

  public RedisMessageService(RedisDataSource ds) {
    streamCommands = ds.stream(String.class);
  }

  public String push(String key, SendMessage message) {

    var map = new HashMap<String, String>();
    map.put(RedisArgsConstant.USERID.key, message.userId());
    map.put(RedisArgsConstant.CLIENT_UUID.key, message.clientUUID());
    map.put(RedisArgsConstant.OPT_TYPE.key, message.optType());
    map.put(RedisArgsConstant.REQUEST_ID.key, message.requestId());

    var base64 = new Base64();
    String messageData;
    if (message.data() == null || message.data().isEmpty()) {
      messageData = "";
    } else {
      messageData = message.data();
    }
    map.put(RedisArgsConstant.DATA.key, base64.encodeToString(messageData.getBytes(StandardCharsets.UTF_8)));

    return streamCommands.xadd(key, map);
  }

  /**
   * 获取消息
   */
  public List<ReceiveMessage> getMessage(String key, Integer count, Long milliseconds) {
    try {
      var xReadArgs = new XReadArgs();
      if(count != null) {
        xReadArgs.count(count);
      }
      if(milliseconds != null) {
        xReadArgs.block(Duration.ofMillis(milliseconds));
      }
      var messages = streamCommands.xread(key, "0", xReadArgs);
      var result = new ArrayList<ReceiveMessage>();
      for (StreamMessage<String, String, String> message: messages) {
        var messageData = message.payload().get(RedisArgsConstant.DATA.key);
        if (messageData != null) {
          var base64 = new Base64();
          message.payload().put(RedisArgsConstant.DATA.key, new String(base64.decode(messageData), StandardCharsets.UTF_8));
        }
        result.add(new ReceiveMessage(message.id(), message.payload().get(RedisArgsConstant.USERID.key),
                message.payload().get(RedisArgsConstant.CLIENT_UUID.key), message.payload().get(RedisArgsConstant.OPT_TYPE.key),
                message.payload().get(RedisArgsConstant.REQUEST_ID.key), message.payload().get(RedisArgsConstant.DATA.key)));
      }
      return result;
    } catch (Exception e) {
      LOG.errorv("获取消息异常 {0}", e);
      return new ArrayList<>();
    }
  }

  /**
   * 删除消息
   */
  public boolean del(String key, String messageId) {
    var result = streamCommands.xdel(key, messageId);
    if (result == 1) {
      LOG.infov("删除 key {0}, messageId {1} 成功", key, messageId);
      return true;
    } else if (result == 0) {
      LOG.infov("重复消费消息 key {0}, messageId {1} ", key, messageId);
      return false;
    }
    LOG.errorv("消费消息 key {0}, messageId {1} 异常", key, messageId);
    return false;
  }

}
