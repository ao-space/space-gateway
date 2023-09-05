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

package space.ao.services.support.platform.temp;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.inject.Singleton;
import space.ao.services.support.log.Logged;

import jakarta.transaction.Transactional;

/**
 * @author zhichuang
 * @date 2023/3/30 0030
 **/
@Singleton
public class TempRegistryInfoRepository implements PanacheRepository<TempRegistryInfoEntity> {


  @Transactional
  @Logged
  public void insert(TempRegistryInfoEntity tempRegistryInfoEntity){
    persist(tempRegistryInfoEntity);
  }

  @Transactional
  @Logged
  public void delete(String requestId){
    delete("requestId", requestId);
  }
}
