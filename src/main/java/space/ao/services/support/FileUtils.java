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

package space.ao.services.support;

import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import org.jboss.logging.Logger;

import com.google.common.io.ByteStreams;

import space.ao.services.account.support.service.ServiceError;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Singleton
public class FileUtils {

  private static final Logger LOG = Logger.getLogger("app.log");

  private FileUtils() {
  }

  @SneakyThrows
  public static File zipFiles(File[] srcFiles, String zipFileName) {
    var tempPath = Files.createTempDirectory("temp");

    // 创建压缩后的文件对象
    var zipFile = new File(tempPath.toFile(), zipFileName + ".zip");
    try(FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)){
      // 创建 FileInputStream 对象

      // 创建 ZipEntry 对象
      ZipEntry zipEntry;
      // 遍历源文件数组
      for (File srcFile : srcFiles) {
        if(!srcFile.exists()){
          continue;
        }
        // 将源文件数组中的当前文件读入 FileInputStream 流中
        try(FileInputStream fileInputStream = new FileInputStream(srcFile)){
          // 实例化 ZipEntry 对象，源文件数组中的当前文件
          zipEntry = new ZipEntry(srcFile.getName());
          zipOutputStream.putNextEntry(zipEntry);
          // 该变量记录每次真正读的字节个数
          int len;
          // 定义每次读取的字节数组
          byte[] buffer = new byte[1024];
          while ((len = fileInputStream.read(buffer)) > 0) {
            zipOutputStream.write(buffer, 0, len);
          }
        }
      }

    }
    return zipFile;
  }

  @SneakyThrows
  public static String unzipAppletFile(String filePath) {
    var path = Files.createTempDirectory("temp");
    try (ZipInputStream zip = new ZipInputStream(new FileInputStream(filePath))) {
      ZipEntry zipEntry;
      while ((zipEntry = zip.getNextEntry()) != null) {
        String fileNameZip = zipEntry.getName();

        var file = new File(path.toFile(), fileNameZip);
        if (fileNameZip.endsWith("/")) {
          var createResult = file.mkdirs();
          if(!createResult){
            LOG.error("failed to create folder while extracting the file");
          }
        } else {
          try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            byte[] byteS = new byte[1024];
            int num;
            while ((num = zip.read(byteS, 0, byteS.length)) > 0) {
              outputStream.write(byteS, 0, num);
            }
          }
        }
      }
    }
    return path.toString();
  }


  public static void getFileFromInputStream(InputStream inputStream, File file){
    if(file.exists()){
      OperationUtils.deleteDirectoryStream(Paths.get(file.getPath()));
    }
    try(inputStream;FileOutputStream outputStream = new FileOutputStream(file)) {
      byte[] b = new byte[2048];
      int bLength;
      while((bLength = inputStream.read(b)) != -1) {
        outputStream.write(b, 0, bLength);
      }
    } catch (Exception e) {
      LOG.errorv("save file to local error: {0}", e.getMessage());
    }
  }

  @SneakyThrows
  public static void writeToFile(File file, String info){
    try(var fos  = new FileOutputStream(file)) {
      fos.write(info.getBytes());
    }
  }

  public static File readDefaultFile(String path){
    var dir = path.substring(0, path.lastIndexOf("/"));
    var fileName = path.substring(path.lastIndexOf("/") + 1);

    var tmpDir = Paths.get(System.getProperties().getProperty("java.io.tmpdir"), dir);
    if (Files.notExists(tmpDir)) {
      try {
        Files.createDirectories(tmpDir);
      } catch (IOException e) {
        LOG.errorv("failed to create directory: {0}", e.getMessage());
      }
    }

    File file = new File(tmpDir.toFile(), fileName);
    saveFileToLocal(path, file);
    return file;
  }

  /**
   * 保存默文件到盒子
   */
  @SneakyThrows
  public static void saveFileToLocal(String resourceLocation, File file){
    if(!file.exists()){
      try (var inputStream = FileUtils.class.getClassLoader().getResourceAsStream(resourceLocation)) {
        try (OutputStream stream = new FileOutputStream(file)) {
          if (inputStream == null) {
            LOG.errorv("failed to get file from resource: {0}", file.getPath());
            throw new NullPointerException();
          }
          ByteStreams.copy(inputStream, stream);
        } catch (IOException e) {
          LOG.errorv("failed to copy file from resource to local: {0}", e.getMessage());
        }
      }
    }
  }

  /**
   * 文件不存在则新建
   */
  @SneakyThrows
  public static void createFileIfNotExists(String imagePath){
    File file = new File(imagePath);
    if(!file.exists()) {
      var parentFile = file.getParentFile();
      if(!parentFile.exists()){
        boolean i = parentFile.mkdirs();
        if (i) {
          LOG.info("文件夹创建成功！");
        } else {
          LOG.error("文件夹创建失败！");
        }
      }
      LOG.info("create：" + imagePath);
      if(!file.createNewFile()){
        LOG.error(ServiceError.FILE_INIT_FAILED);
      }
    }
  }
}
