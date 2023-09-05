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

package space.ao.services.account.security.service;

import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import space.ao.services.account.security.dto.SecurityPasswdResetNewDeviceReq;
import space.ao.services.account.security.utils.PushUtils;
import space.ao.services.account.security.utils.SecurityPasswordUtils;
import space.ao.services.push.dto.NotificationEnum;
import space.ao.services.support.OperationUtils;
import space.ao.services.support.task.TaskBaseEntity;

public class SecurityPasswordModifyJob implements Job {

  @Inject
  OperationUtils utils;
  @Inject
  SecurityPasswordUtils securityPasswordUtils;
  @Inject
  PushUtils pushUtils;
  static final Logger LOG = Logger.getLogger("app.log");

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

    var dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
    var taskId = Long.parseLong(dataMap.getString("taskId"));
    LOG.info("execute task: " + taskId);
    var task = TaskBaseEntity.findTaskById(taskId);
    var req = utils.jsonToObject(task.getData(), SecurityPasswdResetNewDeviceReq.class);

    if (!securityPasswordUtils.doModifyPasscode("taskId: " + taskId, req.getNewPasswd())) {
      LOG.error("task: " + taskId + ", performTask failed at:" + task.getEffectiveAt() + ", data: "
              + task.getData() + ", requestId: " + task.getRequestId());
      return;
    }

    pushUtils.doPushSucc(task.getRequestId(),
        NotificationEnum.SECURITY_PASSWD_RESET_SUCC);

    // 删除对应任务
    TaskBaseEntity.delete(taskId);

    LOG.info("task: " + taskId + ", performTask at:" + task.getEffectiveAt() + ", data: "
        + task.getData() + ", requestId: " + task.getRequestId());

  }
}
