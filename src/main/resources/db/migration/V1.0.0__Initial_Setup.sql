-- Copyright (c) 2022 Institute of Software Chinese Academy of Sciences (ISCAS)
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

CREATE TABLE IF NOT EXISTS userinfo
(
    userid        serial       NOT NULL,
    image         VARCHAR(255),
    personal_name VARCHAR(24)  NOT NULL,
    personal_sign VARCHAR(255),
    aoid          VARCHAR(64),
    client_uuid   VARCHAR(255),
    role          VARCHAR(255) NOT NULL,
    authkey       VARCHAR(255),
    phone_model   VARCHAR(255),
    userdomain    VARCHAR(255),
    image_md5     VARCHAR(255),
    phone_type    VARCHAR(255),
    space_limit   BIGINT       DEFAULT null,
    apply_email   VARCHAR(128) DEFAULT null,
    create_at     timestamp    DEFAULT current_timestamp,
    PRIMARY KEY (userid)
);
CREATE TABLE IF NOT EXISTS authorized_terminal_info
(
    terminal_id   SERIAL PRIMARY KEY,
    userid        INTEGER     NOT NULL,
    aoid          VARCHAR(64) NOT NULL,
    uuid          VARCHAR(64) NOT NULL,
    terminal_mode VARCHAR(64),
    address       VARCHAR(64),
    terminal_type VARCHAR(64),
    login_at      timestamp,
    create_at     timestamp DEFAULT current_timestamp,
    expire_at     timestamp
);

CREATE TABLE IF NOT EXISTS box_info
(
    id                        SERIAL NOT NULL,
    box_regkey                VARCHAR(64),
    passcode                  VARCHAR(64),
    security_email            VARCHAR(255) DEFAULT '',
    security_email_host       VARCHAR(64)  DEFAULT '',
    security_email_port       VARCHAR(16)  DEFAULT '',
    security_email_ssl_enable BOOLEAN      DEFAULT true,
    PRIMARY KEY (id)
);
CREATE TABLE notification
(
    message_id  VARCHAR(32) PRIMARY KEY NOT NULL,
    userid      INTEGER                 NOT NULL,
    client_uuid VARCHAR(255)            NOT NULL,
    opt_type    VARCHAR(64),
    request_id  VARCHAR(64),
    data        VARCHAR,
    read        BOOLEAN   DEFAULT false,
    pushed      INTEGER   DEFAULT NULL,
    create_at   timestamp DEFAULT current_timestamp
);
comment on column notification.pushed is 'null :未推送 , 0: 客户端,  1: 平台';

CREATE TABLE IF NOT EXISTS applet_permission_info
(
    id         SERIAL      NOT NULL,
    aoid       VARCHAR(32) NOT NULL,
    applet_id  VARCHAR(32) NOT NULL,
    permission BOOLEAN DEFAULT true,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX index_applet_user_id ON applet_permission_info (aoid, applet_id);

CREATE TABLE IF NOT EXISTS TASKS
(
    id           SERIAL       NOT NULL,
    request_id   VARCHAR(120) NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    effective_at TIMESTAMP    NOT NULL,
    data         VARCHAR      NOT NULL,
    type         VARCHAR(120) NOT NULL,
    PRIMARY KEY (id)
);

-- 增加 totp 表
CREATE TABLE IF NOT EXISTS totp
(
    userid        INTEGER PRIMARY KEY NOT NULL,
    totp_secret   VARCHAR(64)         NOT NULL,
    authenticator BOOLEAN   DEFAULT false,
    create_at     timestamp DEFAULT current_timestamp
);

-- 局域网注册增加临时信息表
CREATE TABLE IF NOT EXISTS temp_registry_info
(
    id          SERIAL        NOT NULL,
    request_id  VARCHAR(64)   NOT NULL,
    userid      INTEGER       NOT NULL,
    client_uuid VARCHAR(255)  NOT NULL,
    type        VARCHAR(64)   NOT NULL,
    temp_info   VARCHAR(1024) NOT NULL,
    create_at   timestamp DEFAULT current_timestamp
);