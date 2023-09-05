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


import io.quarkus.runtime.Startup;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import space.ao.services.account.security.service.SecurityPasswordModifyJob;

@ApplicationScoped
@Startup
public class ScheduledService {
  @Inject
  Scheduler quartz;
  JobDetail job;
  static final Logger LOG = Logger.getLogger("app.log");

  public <T extends Job> void onStart(String requestId, TaskBaseEntity task, Class<T> jobClass) {
    job = JobBuilder.newJob(jobClass)
        .withIdentity(task.getType(), "Task" + "-" + task.getId())
        .usingJobData("taskData", task.getData())
        .usingJobData("taskId", String.valueOf(task.getId()))
        .usingJobData("requestId", requestId)
        .build();


    Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity(task.getType(), "Task" + "-" + task.getId())
        .startAt(new Date(task.getEffectiveAt().toEpochMilli())).build();
    try {
      quartz.scheduleJob(job, trigger);
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
    LOG.info("task: "+ task.getId() + ", start at: " + task.getCreatedAt() + ", data: " + task.getData() + ", requestId: " + requestId);

  }

  @PostConstruct
  @SuppressWarnings("unused") // 开机自启动，运行修改密码定时任务
  public void checkPasswordModifyTask(){
    List<TaskBaseEntity> tasks = TaskBaseEntity.findAllTasks();
    for (var task: tasks){
      if (task.getEffectiveAt().isBefore(Instant.now())){
        TaskBaseEntity.delete(task.getId());
      } else {
        onStart(task.getRequestId(), task, SecurityPasswordModifyJob.class);
      }
    }
  }
}

