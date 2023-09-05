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

package space.ao.services.push.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@RegisterForReflection
public enum NotificationEnum {

  LOGOUT("下线提醒", "您的登录已失效。请重新进行扫码授权 >>","logout", AfterOpenAction.GO_ACTIVITY),
  MEMBER_DEL("空间注销提醒", "您的空间已被注销，将无法继续使用，请联系管理员。","member_delete", AfterOpenAction.GO_ACTIVITY),
  MEMBER_SELF_DEL("空间注销提醒", "您的空间已注销，将无法继续使用。","member_self_delete", AfterOpenAction.GO_ACTIVITY),
  REVOKE("下线提醒", "您的登录已失效。请重新进行扫码授权 >>","revoke", AfterOpenAction.GO_ACTIVITY),
  MEMBER_JOIN("成员加入提醒", "","member_join", AfterOpenAction.GO_ACTIVITY),
  LOGIN("登录提醒", "", "login", AfterOpenAction.GO_ACTIVITY),
  LOGIN_CONFIRM("免扫码登录", "", "login_confirm", AfterOpenAction.GO_ACTIVITY),

  START("盒子启动", "你的盒子已成功启动", "start", AfterOpenAction.GO_ACTIVITY),
  SECURITY_PASSWD_MOD_APPLY("账户安全提醒：", "您正在终端 %s 上进行安全密码相关操作，请及时确认 \n" +
          "请注意保护空间内的数据安全", "security_passwd_mod_apply", AfterOpenAction.GO_ACTIVITY),
  // 下面这条是安保专用的，非传统推送!!!
  SECURITY_PASSWD_PARTICULAR_MOD_ACCEPT("账户安全提醒：", "安保专用的poll推送允许结果", "security_passwd_mod_accept", AfterOpenAction.GO_ACTIVITY),
  SECURITY_PASSWD_MOD_SUCC("账户安全提醒：", "安全密码修改成功，请知晓！ \n" +
          "若非本人操作，请在【我的-设置-安全设置】里重置安全密码", "security_passwd_mod_succ", AfterOpenAction.GO_ACTIVITY),
  SECURITY_PASSWD_RESET_APPLY("账户安全提醒：", "您正在终端 %s 上进行安全密码相关操作，请及时确认 \n" +
      "请注意保护空间内的数据安全", "security_passwd_reset_apply", AfterOpenAction.GO_ACTIVITY),
  // 下面这条是安保专用的，非传统推送!!!
  SECURITY_PASSWD_PARTICULAR_RESET_ACCEPT("账户安全提醒：", "安保专用的poll推送允许结果", "security_passwd_reset_accept", AfterOpenAction.GO_ACTIVITY),
  SECURITY_PASSWD_RESET_SUCC("账户安全提醒：", "安全密码重置成功，请知晓！ \n" +
          "若非本人操作，请在【我的-设置-安全设置】里重置安全密码", "security_passwd_reset_succ", AfterOpenAction.GO_ACTIVITY),
  SECURITY_EMAIL_SET_SUCC("账户安全提醒：", """
          您已成功绑定密保邮箱 %s！\s
          点击“查看详情”，查看更多信息
          查看详情；""", "security_email_set_succ", AfterOpenAction.GO_ACTIVITY),
  SECURITY_EMAIL_MOD_SUCC("账户安全提醒：", """
          您已绑定新的密保邮箱 %s！\s
          点击“查看详情”，查看更多信息
          查看详情；""", "security_email_mod_succ", AfterOpenAction.GO_ACTIVITY),
  // 升级成功
  UPGRADE_SUCCESS("系统升级提醒", "傲空间系统已经升级到最新版本啦，点击查看 >>","upgrade_success", AfterOpenAction.GO_ACTIVITY),
  // 下载成功
  UPGRADE_DOWNLOAD_SUCCESS("已下载未安装提醒", "傲空间 %s可用于您的设备，且已经可以安装，点击去安装>>","upgrade_download_success", AfterOpenAction.GO_ACTIVITY),
  // 安装中
  UPGRADE_INSTALLING("正在安装系统", "正在安装系统更新，傲空间设备可能无法正常访问，升级完成后将自动恢复使用 >>","upgrade_installing", AfterOpenAction.GO_ACTIVITY),
  // 盒子重启推送
  UPGRADE_RESTART("系统升级提醒","正在重启设备，请在重启完成后使用","upgrade_restart", AfterOpenAction.GO_ACTIVITY),
  //备份进度和恢复进度
  BACKUP_PROGRESS("","", "backup_progress", AfterOpenAction.GO_ACTIVITY),
  //恢复成功
  RESTORE_SUCCESS("数据恢复完成","您的空间数据已完成恢复操作，如有疑问，请联系管理员。", "restore_success", AfterOpenAction.GO_ACTIVITY),

  TODAY_IN_HIS("历史上的今天", "小傲帮您整理了今天的一些记忆，去重温下吧~","today_in_his", AfterOpenAction.GO_ACTIVITY),
  MEMORIES("回忆", "小傲帮您整理了一些过往回忆，去重温下吧~","memories", AfterOpenAction.GO_ACTIVITY),


  ABILITY_CHANGE("平台API发生变化", "平台API发生变化","ability_change", AfterOpenAction.GO_ACTIVITY),

  HISTORY_RECORD("文件状态发生变化", "文件状态发生变化","HISTORY_RECORD", AfterOpenAction.GO_CUSTOM),

  ;


  private String title;
  private String text;

  private final String type;
  @Getter
  private final AfterOpenAction afterOpenAction;

  NotificationEnum(String title, String text, String type, AfterOpenAction afterOpenAction) {
    this.title = title;
    this.text = text;
    this.type = type;
    this.afterOpenAction = afterOpenAction;
  }

  public String getTitle() {
    return title;
  }

  public NotificationEnum setTitle(String title) {
    this.title = title;
    return this;
  }

  public String getText() {
    return text;
  }

  public NotificationEnum setText(String text) {
    this.text = text;
    return this;
  }

  public String getType() {
    return type;
  }

  public NotificationEnum setMemberJoin(String name){
    MEMBER_JOIN.text = name + " 接受了您的邀请并创建了傲空间。点击查看 >>";
    return MEMBER_JOIN;
  }

  public NotificationEnum setLogin(String name){
    LOGIN.text = "您的空间已在设备：" + name +"登录，点击查看 >>";
    return LOGIN;
  }

  public NotificationEnum setInnerLogin(String name){
    LOGIN.text = "您的空间已在 " + name +" 登陆";
    return LOGIN;
  }

  public NotificationEnum setLoginConfirm(String name, String userdomain){
    LOGIN.text = name + " 申请登录您的傲空间（https://" + userdomain + "）, 点击确认是否允许>>";
    return LOGIN;
  }

  public NotificationEnum getSecurityPasswdModApply(String email){
    SECURITY_PASSWD_MOD_APPLY.text = "您正在终端 " + email + " 上进行安全密码相关操作，请及时确认 \n" +
            "请注意保护空间内的数据安全";
    return SECURITY_PASSWD_MOD_APPLY;
  }

  public NotificationEnum getSecurityPasswdResetApply(String email){
    SECURITY_PASSWD_RESET_APPLY.text = "您正在终端 " + email + " 上进行安全密码相关操作，请及时确认 \n" +
            "请注意保护空间内的数据安全";
    return SECURITY_PASSWD_RESET_APPLY;
  }
  public NotificationEnum getSecurityEmailSetSucc(String email){
    SECURITY_EMAIL_SET_SUCC.text = "您已成功绑定密保邮箱 " + email+ "！ \n" +
            "点击“查看详情”，查看更多信息\n";
    return SECURITY_EMAIL_SET_SUCC;
  }
  public NotificationEnum getSecurityEmailModSucc(String email){
    SECURITY_EMAIL_MOD_SUCC.text = "您已绑定新的密保邮箱 " + email + "！ \n" +
            "点击“查看详情”，查看更多信息\n" +
            "查看详情；";
    return SECURITY_EMAIL_MOD_SUCC;
  }
  public NotificationEnum getUpgradeDownloadSuccess(String version){
    UPGRADE_DOWNLOAD_SUCCESS.text = "傲空间 " + version + "可用于您的设备，且已经可以安装，点击去安装>>";
    return UPGRADE_DOWNLOAD_SUCCESS;
  }


  public static NotificationEnum of(String type){
    for (var notification: NotificationEnum.values()){
      if(notification.getType().equals(type)){
        return notification;
      }
    }
    return null;
  }

  public static List<String> sendOnlyOnline(){
    var sendOnlyOnline = new ArrayList<String>();
    sendOnlyOnline.add(NotificationEnum.ABILITY_CHANGE.type);
    sendOnlyOnline.add(NotificationEnum.UPGRADE_RESTART.type);

    return sendOnlyOnline;
  }

  public static List<String> noNeedToSave(){
    var noNeedToSave = new ArrayList<String>();
    noNeedToSave.add(NotificationEnum.ABILITY_CHANGE.type);
    return noNeedToSave;
  }

}
