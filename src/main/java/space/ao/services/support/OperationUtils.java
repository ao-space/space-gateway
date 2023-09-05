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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;
import org.jboss.logging.Logger;
import org.jose4j.json.JsonUtil;
import org.jose4j.lang.JoseException;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.Util;
import space.ao.services.config.ApplicationProperties;
import space.ao.services.support.model.OfficialPlatform;
import space.ao.services.support.service.ServiceError;
import space.ao.services.support.service.ServiceOperationException;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Duration;

@Singleton
public class OperationUtils {

  private static final Logger LOG = Logger.getLogger("app.log");

  private static Random random;
  @Inject
  ObjectMapper objectMapper;

  @Inject
  ApplicationProperties properties;

  @Getter
  public PublicKey clientPublicKey;

  @Getter
  public Map<String, Object> sharedInfo;

  private static Boolean enableInternetAccess;

  @PostConstruct
  public void init() throws IOException, GeneralSecurityException, JoseException {
    LOG.infov("utils start init");
    try {
      try (Reader reader = getFileStreamReader(properties.sharedInfoFile())) {
        sharedInfo = JsonUtil.parseJson(CharStreams.toString(reader));
        LOG.infov("read shared info completed, shared info:{0}", sharedInfo);
      }
      loadInternetServiceConfig("utils start init");
    } catch (Exception e) {
      LOG.error("OperationUtils init failed", e);
    }
    LOG.infov("utils init completed");
  }

  public void loadInternetServiceConfig(String requestId) {
    synchronized (OperationUtils.class){
      try (Reader reader = getFileStreamReader(properties.internetServiceConfig())) {
        var internetServiceConfig = JsonUtil.parseJson(CharStreams.toString(reader));
        enableInternetAccess = (Boolean) internetServiceConfig.get("enableInternetAccess");
        LOG.infov("load internet service config completed, requestId:{0}, enableInternetAccess:{1}", requestId, enableInternetAccess);
      } catch (Exception e) {
        enableInternetAccess = Objects.isNull(enableInternetAccess) || enableInternetAccess;
        LOG.errorv("load internet service config failed, requestId:{0}, error: {1}", requestId, getErrorInfoFromException(e));
      }
    }
  }

  public String getBoxVersion(){
    try (Reader reader = getFileStreamReader(properties.sharedInfoFile())) {
      sharedInfo = JsonUtil.parseJson(CharStreams.toString(reader));
      LOG.infov("read shared info completed, shared info:{0}", sharedInfo);
    } catch (IOException | JoseException e) {
      throw new RuntimeException(e);
    }
    var boxVersion = String.valueOf(sharedInfo.get("boxVersion"));
    if(StringUtils.isBlank(boxVersion)){
      boxVersion = properties.boxVersion();
    }
    return boxVersion;
  }
  public static Random getRandom() {
    if (random == null) {
      random = new SecureRandom();
    }
    return random;
  }

  public Reader getFileStreamReader(String resourceLocation) {
    try {
      final File file = ResourceUtils.getFile(resourceLocation);
      return new BufferedReader(
          new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
    } catch (Exception fallback) {
      InputStream in = getClass().getResourceAsStream(resourceLocation);
      return new BufferedReader(
          new InputStreamReader(Objects.requireNonNull(in), StandardCharsets.UTF_8));
    }
  }

  public RSAPublicKey getRSAPublicKey(Reader reader) throws GeneralSecurityException, IOException {
    final String key = CharStreams.toString(reader);

    final String pem = key.replaceAll("[\\n\\r]", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "");

    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(pem));
    return (RSAPublicKey) keyFactory.generatePublic(keySpec);
  }

  @SneakyThrows
  public String objectToJson(Object object) {
    return objectMapper.writeValueAsString(object);
  }

  @SneakyThrows
  public <T> T jsonToObject(String json, Class<T> clz) {
    return objectMapper.readValue(json, clz);
  }

  @SneakyThrows
  public <T> T mapToObject(Map<String, String> map, Class<T> clz) {
    var json = objectMapper.writeValueAsString(map);
    return objectMapper.readValue(json, clz);
  }

  @SneakyThrows
  public <T> T jsonToObject(String json, TypeReference<T> valueTypeRef) {
    return objectMapper.readValue(json, valueTypeRef);
  }

  @SneakyThrows
  public <T> Map<String, T> objectToMap(Object object) {
    var json = objectMapper.writeValueAsString(object);
    return objectMapper.<HashMap<String, T>>readValue(json, new TypeReference<>() {});
  }

  @SneakyThrows
  public Map<String, String> stringToMap(String json) {
    return objectMapper.<HashMap<String, String>>readValue(json, new TypeReference<>() {});
  }
  @SneakyThrows
  public <T> T objectToResponseBaseResult(Object object, Class<T> clz) {
    var json = objectMapper.writeValueAsString(object);
    return objectMapper.readValue(json, clz);
  }

  public String unifiedRandomCharters(int length) {
    int startChar = '0';
    int endChar = 'z';
    return getRandom().ints(startChar, endChar + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(length)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

  public String unifiedRandomHexCharters(int length) {
    int startChar = '0';
    int endChar = 'f';
    return getRandom().ints(startChar, endChar + 1)
            .filter(i -> (i <= 57 || i >97) )
            .limit(length)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
  }

  public String createRandomType4UUID() {
    return UUID.randomUUID().toString();
  }

  public String createRandomNumbers(int length) {
    int startChar = '0';
    int endChar = '9';

    return getRandom().ints(startChar, endChar + 1)
        .limit(length)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

  public byte[] createRandomBytes(int length) {
    byte[] bytes = new byte[length];
    getRandom().nextBytes(bytes);
    return bytes;
  }


  /**
   *  利用Apache的工具类实现SHA-256加密
   *  所需jar包下載 <a href="http://pan.baidu.com/s/1nuKxYGh">...</a>
   * @param str 加密前的报文
   */
  @SneakyThrows
  public String string2SHA256(String str){
    MessageDigest messageDigest;
    var encodeStr = "";
    messageDigest = MessageDigest.getInstance("SHA-256");
    byte[] hash = messageDigest.digest(str.getBytes(StandardCharsets.UTF_8));
    encodeStr = Hex.encodeHexString(hash);

    return encodeStr;
  }

  @SneakyThrows
  public String encryptToMD5(String str) {
    var md5 = MessageDigest.getInstance("md5");
    var digest = md5.digest(str.getBytes(StandardCharsets.UTF_8));
    return String.format("%032x", new BigInteger(1, digest));
  }

  public boolean delAllFile(String path){
    File file = new File(path);
    if(!file.exists()) {return true;}
    if(!file.isDirectory()) {return false;}
    String[] fileLists = file.list();
    if(fileLists == null){
      return true;
    }
    for (String s : fileLists){
      var temp = new File(s);
      if(temp.isFile()) { return temp.delete();}
    }
    return file.delete();
  }

  public void deleteFileDir(String path)  {
    try (Stream<Path> walk = Files.walk(Paths.get(path))) {
      walk.sorted(Comparator.reverseOrder())
          .forEach(OperationUtils::deleteDirectoryStream);
    } catch (Exception e) {
      LOG.errorv("resources delete failed! , {0}", e);
    }
  }

  public static void deleteDirectoryStream(Path path) {
    try {
      Files.delete(path);
    } catch (IOException e) {
      LOG.errorv("无法删除的路径 {0}\n{1}", path, e);
    }
  }

  /**
   * 向文件写信息
   */
  public void writeToFile(File file, Map<String, String> mapInfo){
    try(var fos  = new FileOutputStream(file)) {
      fos.write(objectToJson(mapInfo).getBytes());
    }
    catch (Exception ie){
      LOG.error("write admin into file failed :" + mapInfo);
    }
  }

  /**
   * 读取文件信息
   */
  public Map<String, String> readFromFile(File file) {
    String line;
    StringBuilder buff = new StringBuilder();
    try(var reader = new BufferedReader(new FileReader(file))){
      while ((line = reader.readLine()) != null){
        buff.append(line);
      }
    } catch (IOException e) {
      LOG.error(e);
    }
    HashMap<String, String> fileInfoMap;
    try {
      fileInfoMap = objectMapper.readValue(buff.toString(), new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new ServiceOperationException(ServiceError.UNKNOWN);
    }
    return fileInfoMap;
  }

  /**
   * 根据IP地址获取城市
   * @param ip ip
   * @return 城市
   */
  public String getCityInfo(String ip) {
    //db
    String dbPath = Objects.requireNonNull(
        OperationUtils.class.getResource(properties.ip2regionLocation())).getPath();
    var file = new File(dbPath);

    if (!file.exists()) {
      LOG.info("地址库文件不存在,进行其他处理");
      
      String tmpDir = System.getProperties().getProperty("java.io.tmpdir");
      dbPath = tmpDir + File.separator + "ip2region.db";
      LOG.infov("临时文件路径:{0}", dbPath);
      
      file = new File(dbPath);
      if (!file.exists() || (System.currentTimeMillis() - file.lastModified() > 86400000L)) {
        LOG.info("文件不存在或者文件存在时间超过1天进入...");
        try (var inputStream = getClass().getResourceAsStream(properties.ip2regionLocation())) {
          if(inputStream == null) {
            return null;
          }
          try (OutputStream stream = new FileOutputStream(file)){
            ByteStreams.copy(inputStream, stream);
          } catch (IOException ioe) {
            LOG.error(ioe);
          }
        } catch (IOException ioe) {
          LOG.error(ioe);
        }
      }
    }

    //查询算法
    //B-tree
    //DbSearcher.BINARY_ALGORITHM //Binary
    //DbSearcher.MEMORY_ALGORITYM //Memory
    try {
      var config = new DbConfig();
      var searcher = new DbSearcher(config, file.getPath());
      Method method;
      method = searcher.getClass().getMethod("btreeSearch", String.class);
      DataBlock dataBlock;
      if (!Util.isIpAddress(ip)) {
        LOG.error("Error: Invalid ip address");
        return null;
      }
      dataBlock  = (DataBlock) method.invoke(searcher, ip);
      LOG.infov("result : {0}", dataBlock.getRegion());
      return dataBlock.getRegion();
    } catch (Exception e) {
      LOG.error(e);

    }
    return null;
  }

  public long get100YearSeconds(){
    return Duration.ZERO.plusDays(36500).toSeconds();
  }

  public String getErrorInfoFromException(Exception e) {
    try {
      var sw = new StringWriter();
      var pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      return "\r\n" + sw + "\r\n";
    } catch (Exception e2) {
      return "bad getErrorInfoFromException";
    }
  }

  public Boolean createFolder(String path) {
    File fileFolder = new File(path);
    //创建存储zip包路径
    if(!fileFolder.exists()){
      var createDir = fileFolder.mkdirs();
      if(!createDir){
        LOG.error("failed to create folder");
        return false;
      }
    }
    return true;
  }

  public String inputStreamToString(InputStream inputStream) {
    try {
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new ServiceOperationException(ServiceError.UNKNOWN);
    }
  }
  public boolean isLocalAddress(String ipAddress) {
    InetAddress addr;
    try {
      addr = InetAddress.getByName(ipAddress);
    } catch (UnknownHostException e) {
      LOG.errorv("unknown host {0}", ipAddress);
      return false;
    }
    if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) {
      return true;
    }
    String name = addr.getHostName();
    if (name.equals(addr.getHostAddress()) && !name.equals("")) {
      return true;
    }
    return addr.isSiteLocalAddress();
  }

  public String getUserDomainSuffix() {
    var platformInfo = OfficialPlatform.getOfficialPlatformByPlatformUrl(properties.ssplatformUrl());
    String userDomainSuffix;
    if(Objects.nonNull(platformInfo)){
      userDomainSuffix = platformInfo.getUserDomain();
    } else {
      userDomainSuffix = properties.ssplatformUserDomainSuffix();
    }
    return userDomainSuffix;
  }

  public Boolean getEnableInternetAccess(){
    if(Objects.isNull(enableInternetAccess)){
      loadInternetServiceConfig("enableInternetAccess");
    }
    return enableInternetAccess;
  }
}
