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

package space.ao.services.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Global application configuration properties.
 *
 * <p>Impl ref:
 * <a _href=https://quarkus.io/guides/config-reference>https://quarkus.io/guides/config-reference</a>
 */
@ConfigMapping(prefix = "app")
public interface ApplicationProperties {

  @WithName("version")
  String version();

  @WithName("account.url")
  String accountUrl();

  @WithName("ssplatform.url")
  String ssplatformUrl();

  @WithName("ssplatform.user-domain.suffix")
  String ssplatformUserDomainSuffix();

  @WithName("psplatform.url")
  String psplatformUrl();

  @WithName("file-api.url")
  String fileapiUrl();

  @WithName("radicale-api.url")
  String radicaleapiUrl();

  @WithName("radicale-api.auth")
  String radicaleapiAuth();

  @WithName("system-agent.url.base")
  String systemAgentUrlBase();

  @WithName("client.public-key.location")
  String clientPublicKeyLocation();

  @WithName("client.private-key.location")
  String clientPrivateKeyLocation();

  @WithName("security-api.url")
  String securityApiUrl();

  @WithName("box.uuid")
  String boxUuid();

  @WithName("box.btid")
  String boxBtid();

  @WithName("box.username")
  String boxUserName();

  @WithName("box.version")
  String boxVersion();

  @WithName("box.name")
  String boxName();

  @WithName("box.pkg.name")
  String boxPkgName();

  @WithName("box.type")
  String boxType();

  @WithName("box.endpoint")
  String boxEndpoint();

  @WithName("box.private-key.location")
  String boxPrivateKeyLocation();

  @WithName("box.smime-sign.location.pem")
  String boxSmimeSignLocationPem();

  @WithName("box.smime-sign.location.p12")
  String boxSmimeSignLocationP12();

  @WithName("box.public-key.location")
  String boxPublicKeyLocation();

  @WithName("box.keyfingerprint")
  String keyFingerprint();

  @WithName("box.support.security.chip")
  boolean securityChipSupport();

  @WithName("box.support.security.chip.unix-socket")
  boolean securityChipSupportUnixSocket();
  @WithName("box.deploy.method")
  String deployMethod();

  @WithName("box.device.model.number")
  Long boxDeviceModelNumber();

  @WithName("box.support.security.mode")
  String securityMode();

  @WithName("box.support.security.chip.key")
  boolean securityChipKeySupport();

  @WithName("gateway.routers.location")
  String gatewayRoutersLocation();

  @WithName("gateway.metadata.location")
  String gatewayMetadataLocation();

  @WithName("gateway.http-client.read-timeout")
  String gatewayHttpClientReadTimeout();

  @WithName("gateway.http-client.write-timeout")
  String gatewayHttpClientWriteTimeout();

  @WithName("gateway.auth.time-of-ak-life")
  String gatewayTimeOfAkLife();

  @WithName("gateway.auth.time-of-rak-life")
  String gatewayTimeOfRakLife();

  @WithName("gateway.auth.login.time-of-qr-ak-life")
  String gatewayTimeOfQrAkLife();

  @WithName("gateway.auth.login.time-of-allow-automatic-login")
  String timeOfAllowAutoLogin();

  @WithName("gateway.auth.login.time-of-allow-login")
  String timeOfAllowLogin();

  @WithName("gateway.auth.alg-info.publicKey.algorithm")
  String gatewayAlgInfoPublicKeyAlgorithm();


  @WithName("gateway.auth.alg-info.signature.algorithm")
  String gatewayAlgInfoSignatureAlgorithm();

  @WithName("gateway.auth.alg-info.publicKey.keySize")
  Integer gatewayAlgInfoPublicKeyKeySize();

  @WithName("gateway.auth.alg-info.transportation.algorithm")
  String gatewayAlgInfoTransportationAlgorithm();

  @WithName("gateway.auth.alg-info.transportation.keySize")
  Integer gatewayAlgInfoTransportationKeySize();

  @WithName("gateway.auth.alg-info.transportation.transformation")
  String gatewayAlgInfoTransportationTransformation();

  @WithName("gateway.security.passwd.time-of-ak-life")
  String gatewayTimeOfSecurityPasswdAkLife();

  @WithName("gateway.security.passwd.modify-take-effect-for-new-app")
  String gatewayTimeOfSecurityPasswdModifyTakeEffectForNewApp();

  @WithName("gateway.security.email.time-of-ak-life")
  String gatewayTimeOfSecurityEmailAkLife();

  @WithName("gateway.security.email-provider.configuration-file.location")
  String gatewaySecurityEmailProviderConfigurationFileLocation();

  @WithName("gateway.file.system.location")
  @WithDefault("/mnt/d/website/media/")
  String gatewayFileSystemLocation();

  @WithName("shared.info.file")
  String sharedInfoFile();

  @WithName("ip2region.location")
  String ip2regionLocation();

  @WithName("account.image.location")
  String accountImageLocation();

  @WithName("account.data.location")
  String accountDataLocation();

  @WithName("account.invite.maxmemberlimit")
  int inviteMaxMemberLimit();

  @WithName("account.invite.expirationsec")
  long inviteExpirationSec();

  @WithName("account.invite.param")
  String inviteParam();

  @WithName("account.subdomain.edit.limit.times")
  int subdomainEditLimitTimes();

  @WithName("account.subdomain.edit.limit.years")
  int subdomainEditLimitYears();

  @WithName("account.subdomain.edit.limit.mouths")
  int subdomainEditLimitMouths();

  @WithName("account.subdomain.edit.limit.days")
  int subdomainEditLimitDays();

  @WithName("account.subdomain.edit.limit.hours")
  int subdomainEditLimitHours();

  @WithName("account.subdomain.edit.limit.minutes")
  int subdomainEditLimitMinutes();

  @WithName("applet.location")
  String appletLocation();

  @WithName("applet.web.location")
  String appletWebLocation();

  @WithName("applet.upload.location")
  String appletUploadLocation();

  @WithName("push.timeout")
  String pushTimeout();

  @WithName("push.mq.main")
  String pushMqMain();

  @WithName("push.mq.platform")
  String pushMqPlatform();

  @WithName("push.mq.client.prefix")
  String pushMqClientPrefix();

  @WithName("push.app.activity")
  String pushAppActivity();

  @WithName("ssplatform.api-resources.location")
  String getSSPlatformApisLocation();

  @WithName("appstore.appapi.url")
  String appstoreAppapiUrl();

  @WithName("appstore.appsign.url")
  String appstoreAppsignUrl();

  @WithName("vod.url")
  String appVodUrl();

  @WithName("deploy-api.url")
  String deployApiUrl();

  @WithName("gateway.cron.cache-clean.clean-expired-ak")
  String gatewayCronCacheCleanCleanExpiredAk();

  @WithName("gateway.log.file.path")
  String quarkusLogFilePath();

  @WithName("dockerhub.url")
  String dockerhubUrl();

  @WithName("internet.service.config")
  String internetServiceConfig();
}
