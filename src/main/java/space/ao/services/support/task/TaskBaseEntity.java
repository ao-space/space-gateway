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

package space.ao.services.support.task;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import java.time.Instant;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.persistence.Entity;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "TASKS")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class TaskBaseEntity extends PanacheEntityBase {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "request_id")
  @NotNull
  private String requestId;
  @Column(name = "created_at")
  @NotNull
  private Instant createdAt;
  @Column(name = "effective_at")
  @NotNull
  private Instant effectiveAt;
  @Column(name = "data")
  @NotNull
  private String data;
  @Column(name = "type")
  @NotNull
  private String type;
  public TaskBaseEntity(String requestId , Instant effectiveAt, String data, String type){
    this.requestId = requestId;
    this.effectiveAt = effectiveAt;
    this.createdAt = Instant.now();
    this.data = data;
    this.type = type;
  }

  @Transactional
  public static void delete(Long taskId){
    findById(taskId).delete();
  }

  @Transactional
  public static TaskBaseEntity findTaskById(Long taskId){
    return findById(taskId);
  }

  @Transactional
  public static List<TaskBaseEntity> findAllTasks(){
    return find("type","passwordModify").list();
  }
}
