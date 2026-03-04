DROP TABLE IF EXISTS `qt_group`;
CREATE TABLE `qt_group`
(
    `id`            bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `group_id`      varchar(11) NOT NULL COMMENT '群组公开ID（业务主键）',
    `name`          varchar(64) NOT NULL COMMENT '群组名称',
    `avatar`        varchar(255)         DEFAULT NULL COMMENT '群组头像URL',
    `owner_uid`     varchar(50) NOT NULL COMMENT '群主UID（关联sys_user.uid）',
    `notice`        varchar(1024)        DEFAULT NULL COMMENT '群组公告',
    `max_count`     int(11)     NOT NULL DEFAULT '200' COMMENT '最大成员数',
    `current_count` int(11)     NOT NULL DEFAULT '1' COMMENT '当前成员数',
    `allow_invite`  tinyint(1)  NOT NULL DEFAULT '1' COMMENT '是否允许普通成员邀请(0:否, 1:是)',
    `join_type`     tinyint(4)  NOT NULL DEFAULT '0' COMMENT '入群方式(0:同意后加入, 1:直接加入, 2:邀请加入, 3:拒绝任何人加入)',
    `status`        tinyint(4)  NOT NULL DEFAULT '0' COMMENT '群组状态(0:正常, 1:解散, 2:封禁)',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version`       int(11)     NOT NULL DEFAULT '1' COMMENT '乐观锁版本号',
    `extra`         varchar(255)         DEFAULT NULL COMMENT '扩展字段（JSON格式，预留未来功能）',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_group_id` (`group_id`) USING BTREE COMMENT '群组ID唯一索引',
    KEY `idx_owner_uid` (`owner_uid`) USING BTREE COMMENT '群主查询索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='QT群组信息表';

DROP TABLE IF EXISTS `qt_group_member`;
CREATE TABLE `qt_group_member`
(
    `id`            bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `group_id`      varchar(11) NOT NULL COMMENT '群组ID',
    `user_uid`      varchar(11) NOT NULL COMMENT '用户UID',
    `role`          tinyint(4)  NOT NULL DEFAULT '1' COMMENT '角色(1:普通成员, 2:管理员, 3:群主)',
    `alias`         varchar(64)          DEFAULT NULL COMMENT '群内昵称',
    `is_top`        tinyint(1)  NOT NULL DEFAULT '0' COMMENT '是否置顶(0:否, 1:是)',
    `is_disturb`    tinyint(1)  NOT NULL DEFAULT '0' COMMENT '是否免打扰(0:否, 1:是)',
    `last_read_seq` bigint(20)  NOT NULL DEFAULT '0' COMMENT '最后阅读消息序列号',
    `mute_end_time` datetime             DEFAULT NULL COMMENT '禁言截止时间(NULL表示未禁言)',
    `join_type`     tinyint(4)  NOT NULL DEFAULT '1' COMMENT '入群方式(1:邀请, 2:搜索, 3:二维码)',
    `is_quit`       tinyint(1)  NOT NULL DEFAULT '0' COMMENT '是否退出(0:否, 1:是)',
    `join_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入群时间',
    `leave_time`    datetime             DEFAULT NULL COMMENT '离群时间(NULL表示未离群)',
    `extra`         varchar(255)         DEFAULT NULL COMMENT '扩展字段',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_group_user` (`group_id`, `user_uid`) USING BTREE COMMENT '群成员唯一索引',
    KEY `idx_user_uid` (`user_uid`) USING BTREE COMMENT '用户群组查询索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='QT群组成员表';

DROP TABLE IF EXISTS `qt_friend`;
CREATE TABLE `qt_friend`
(
    `id`          bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `user_uid`    varchar(11) NOT NULL COMMENT '用户UID',
    `friend_uid`  varchar(11) NOT NULL COMMENT '好友UID',
    `remark`      varchar(64)          DEFAULT NULL COMMENT '好友备注',
    `source`      tinyint(4)  NOT NULL DEFAULT '1' COMMENT '来源(1:搜索, 2:群聊, 3:名片, 4:扫码)',
    `status`      tinyint(4)  NOT NULL DEFAULT '0' COMMENT '状态(0:正常, 1:删除, 2:拉黑)',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `extra`       varchar(255)         DEFAULT NULL COMMENT '扩展字段',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_friend` (`user_uid`, `friend_uid`) USING BTREE COMMENT '好友关系唯一索引',
    KEY `idx_friend_uid` (`friend_uid`) USING BTREE COMMENT '好友查询索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='QT好友关系表';

DROP TABLE IF EXISTS `qt_contact_request`;
CREATE TABLE `qt_contact_request`
(
    `id`          bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `from_uid`    varchar(11) NOT NULL COMMENT '发送方UID',
    `to_id`       varchar(11) NOT NULL COMMENT '接收方ID',
    `to_type`     tinyint(4)  NOT NULL DEFAULT '1' COMMENT '接收方类型(1:用户, 2:群组)',
    `contact_id`  varchar(32)          DEFAULT NULL COMMENT '联系人ID（用户：好友UID，群组：群主和管理员的UID）',
    `reason`      varchar(128)         DEFAULT NULL COMMENT '申请理由',
    `status`      tinyint(4)  NOT NULL DEFAULT '0' COMMENT '状态(0:待处理, 1:已同意, 2:已拒绝, 3:已忽略)',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `handle_time` datetime             DEFAULT NULL COMMENT '处理时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_to_id` (`to_id`) USING BTREE COMMENT '接收方查询索引',
    KEY `idx_from_uid` (`from_uid`) USING BTREE COMMENT '发送方查询索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='QT联系人申请表';

create index qt_contact_request_from_uid_to_id_contact_id_index
    on qt_contact_request (from_uid, to_id, contact_id);