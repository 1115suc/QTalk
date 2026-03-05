# 消息表----------------------------------------------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`
(
    `message_id`         bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '消息自增 id',
    `session_id`         varchar(32) NOT NULL COMMENT '会话 id',
    `message_type`       tinyint(4)  NOT NULL COMMENT '消息类型',
    `message_content`    varchar(500) DEFAULT NULL COMMENT '消息内容',
    `send_user_id`       varchar(12)  DEFAULT NULL COMMENT '发送人 id',
    `send_user_nickname` varchar(20)  DEFAULT NULL COMMENT '发送人昵称',
    `send_time`          bigint       DEFAULT NULL COMMENT '发送时间',
    `contact_id`         varchar(11) NOT NULL COMMENT '接收人 id',
    `contact_type`       tinyint(1)  NOT NULL COMMENT '接收人类型 (0:好友 1:群聊)',
    `file_name`          varchar(200) DEFAULT NULL COMMENT '文件名',
    `file_size`          bigint       DEFAULT NULL COMMENT '文件大小',
    `file_type`          tinyint(1)   DEFAULT NULL COMMENT '文件类型',
    `status`             tinyint(1)   DEFAULT NULL COMMENT '状态 (0:正在发送 1:已发送)',
    PRIMARY KEY (`message_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='消息表';

ALTER TABLE `chat_message`
    MODIFY `message_id` bigint AUTO_INCREMENT COMMENT '消息自增 id';

ALTER TABLE `chat_message`
    AUTO_INCREMENT = 1;

# 会话联系人表----------------------------------------------------------------
DROP TABLE IF EXISTS `chat_session_user`;
CREATE TABLE `chat_session_user`
(
    `session_id`   varchar(32) NOT NULL COMMENT '会话 id',
    `uid`          varchar(12) NOT NULL COMMENT '用户 id',
    `contact_id`   varchar(12) NOT NULL COMMENT '联系人 id',
    `contact_name` varchar(20) DEFAULT NULL COMMENT '联系人名称',
    `last_message`      varchar(500) DEFAULT NULL COMMENT '最后接收到的消息',
    `last_receive_time` bigint(11)   DEFAULT NULL COMMENT '最后接收到的消息（毫秒）',
    PRIMARY KEY (`session_id`) USING BTREE,
    KEY `idx_session_id` (`uid`) USING BTREE,
    KEY `idx_contact_id` (`uid`, `contact_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='联系人表';

