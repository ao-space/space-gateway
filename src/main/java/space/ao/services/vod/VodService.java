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

package space.ao.services.vod;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import space.ao.services.account.personalinfo.repository.UserInfoRepository;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.agent.AgentServiceRestClient;
import space.ao.services.support.file.FileServiceRestClient;
import space.ao.services.support.file.info.UUIDInfo;
import space.ao.services.support.log.Logged;
import space.ao.services.support.service.ServiceOperationException;
import space.ao.services.support.FileUtils;
import space.ao.services.support.service.ServiceError;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Files;
import java.util.Objects;

@ApplicationScoped
public class VodService {
  private static final Logger LOG = Logger.getLogger("app.log");

  @Inject
  @RestClient
  VodServiceRestClient vodServiceRestClient;
  @Inject
  UserInfoRepository userInfoRepository;
  @Inject
  @RestClient
  AgentServiceRestClient agentServiceRestClient;
  @Inject
  @RestClient
  FileServiceRestClient fileServiceRestClient;
  @Inject
  ApplicationProperties properties;

  @Logged
  public Response getM3U8(String requestId, Long userid, String fileUUID) {
    var user = userInfoRepository.findByUserId(userid);
    var url = user.getUserDomain();
    var m3u8Info = getM3U8Info(requestId, userid, fileUUID);
    if(m3u8Info.contains("segment-1-v1-a1.m4s")){
      throw new ServiceOperationException(ServiceError.VOD_SERVICE_NOT_SUPPORT_VIDEO_CODING);
    }
    var resultFile = FileUtils.zipFiles(handleM3U8(requestId, m3u8Info, url), "index");
    return Response.ok(resultFile)
            .header("Content-Disposition", "attachment;filename=" + fileUUID +".zip")
            .header("Content-Length", resultFile.length())
            .build();
  }

  @Logged
  String getM3U8Info(String requestId, Long userid, String fileUUID) {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      LOG.info(e);
      // Restore interrupted state...
      Thread.currentThread().interrupt();
    }
    var symlinkResult = fileServiceRestClient.createSoftLink(userid, UUIDInfo.of(fileUUID));
    LOG.warnv("symlinkResult: {0}", symlinkResult);
    if(!Objects.equals(symlinkResult.code(), "200")){
      return symlinkResult.code();
    }
    String m3u8Info;
    try(var response = vodServiceRestClient.getM3U8(requestId, symlinkResult.results().getLinkName())){
      var entity = response.readEntity(InputStream.class);
      var tempFilePath = Files.createTempFile(fileUUID + "-index",  ".m3u8");
      var tempFile = tempFilePath.toFile();
      FileUtils.getFileFromInputStream(entity, tempFile);
      m3u8Info = Files.readString(tempFilePath);
    } catch (IOException e) {
      LOG.error("read m3u8 file error", e);
      throw new ServiceOperationException(ServiceError.VOD_SERVICE_ERROR);
    }
    return m3u8Info;
  }

  public File[] handleM3U8(String requestId, String m3u8Info, String userDomain) {
    File file1 = null;
    File file2 = null;
    try {
      var tempPath = Files.createTempDirectory("temp");
      file1 = new File(tempPath + File.separator + "index-lan.m3u8");
      file2 = new File(tempPath + File.separator + "index-wan.m3u8");
    } catch (IOException e) {
      LOG.error("create temp file error", e);
    }
    var ipAddressInfo = agentServiceRestClient.getIpAddressInfo(requestId);
    LOG.debugv("IpAddressInfo: {0}", ipAddressInfo);
    String lanIp = "";
    if(!ipAddressInfo.results().isEmpty()){
      lanIp = ipAddressInfo.results().get(0).getIp();
    }
    LOG.debugv("IpAddressInfo: {0}", lanIp);
    var lanM3U8Info = m3u8Info.replace(properties.appVodUrl().split("/")[2], lanIp);
    FileUtils.writeToFile(file1, lanM3U8Info);
    if(Objects.nonNull(userDomain)){
      var wanM3U8Info = m3u8Info.replace("http://" + properties.appVodUrl().split("/")[2], "https://" + userDomain);
      FileUtils.writeToFile(file2, wanM3U8Info);
      LOG.debugv("wanM3U8Info: {0}, \n lanM3U8Info: {1}", wanM3U8Info, lanM3U8Info);
    }
    return new File[]{ file1, file2, };
  }


}
