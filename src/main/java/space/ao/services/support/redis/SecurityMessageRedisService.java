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

/*
 * Copyright (c) 2023 Institute of Software Chinese Academy of Sciences (ISCAS)
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

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.list.ListCommands;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * @author zhichuang
 * @date 2023/7/26 0026
 **/
@ApplicationScoped
public class SecurityMessageRedisService {
  private final KeyCommands<String> keyCommands;
  private final ListCommands<String, String> listCommands;
  public SecurityMessageRedisService(RedisDataSource ds) {
    listCommands = ds.list(String.class);
    keyCommands = ds.key();
  }

  public void rpush(String key, String value) {
    listCommands.rpush(key, value);
  }

  public void expire(String key, long l) {
    keyCommands.expire(key, l);
  }

  public List<String> lrange(String k, long start, long stop) {
    return listCommands.lrange(k, start, stop);
  }

  public void ltrim(String k, long start, long stop) {
    listCommands.ltrim(k, start, stop);
  }
}
