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
package space.ao.services.account.deviceinfo.dto;

import space.ao.services.account.authorizedterminalinfo.entity.AuthorizedTerminalResult;
import space.ao.services.account.personalinfo.dto.AccountInfoResult;

import java.util.List;

public class SettingInfo {
  private NetworkChannelInfo networkChannelInfo;
  private DeviceInfoResult deviceInfo;
  private List<AccountInfoResult> memberList;
  private List<AuthorizedTerminalResult> authorizedTerminalResults;
  private AccountInfoResult binderInfoResult;

  public void setNetworkChannelInfo(NetworkChannelInfo networkChannelInfo) {
    this.networkChannelInfo = networkChannelInfo;
  }

  public void setDeviceInfo(DeviceInfoResult deviceInfo) {
    this.deviceInfo = deviceInfo;
  }

  public void setAccountInfoResults(List<AccountInfoResult> memberList) {
    this.memberList = memberList;
  }

  public void setAuthorizedTerminalResults(List<AuthorizedTerminalResult> authorizedTerminalResults) {
    this.authorizedTerminalResults = authorizedTerminalResults;
  }

  public void setBinderInfoResult(AccountInfoResult binderInfoResult) {
    this.binderInfoResult = binderInfoResult;
  }

  @Override
  public String toString() {
    return "SettingInfo{" +
            "networkChannelInfo=" + networkChannelInfo +
            ", deviceInfo=" + deviceInfo +
            ", memberList=" + memberList +
            ", authorizedTerminalResults=" + authorizedTerminalResults +
            ", binderInfoResult=" + binderInfoResult +
            '}';
  }
}
