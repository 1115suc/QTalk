DROP TABLE IF EXISTS `qt_group`;
CREATE TABLE `qt_group`
(
    `id`            bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `group_id`      varchar(32) NOT NULL COMMENT '群组公开ID（业务主键）',
    `name`          varchar(64) NOT NULL COMMENT '群组名称',
    `avatar`        varchar(255)         DEFAULT NULL COMMENT '群组头像URL',
    `owner_uid`     varchar(50) NOT NULL COMMENT '群主UID（关联sys_user.uid）',
    `notice`        varchar(1024)        DEFAULT NULL COMMENT '群组公告',
    `max_count`     int(11)     NOT NULL DEFAULT '200' COMMENT '最大成员数',
    `current_count` int(11)     NOT NULL DEFAULT '1' COMMENT '当前成员数',
    `allow_invite`  tinyint(1)  NOT NULL DEFAULT '1' COMMENT '是否允许普通成员邀请(0:否, 1:是)',
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
    `group_id`      varchar(32) NOT NULL COMMENT '群组ID',
    `user_uid`      varchar(50) NOT NULL COMMENT '用户UID',
    `role`          tinyint(4)  NOT NULL DEFAULT '1' COMMENT '角色(1:普通成员, 2:管理员, 3:群主)',
    `alias`         varchar(64)          DEFAULT NULL COMMENT '群内昵称',
    `join_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入群时间',
    `join_type`     tinyint(4)  NOT NULL DEFAULT '1' COMMENT '入群方式(1:邀请, 2:搜索, 3:二维码)',
    `mute_end_time` datetime             DEFAULT NULL COMMENT '禁言截止时间(NULL表示未禁言)',
    `extra`         varchar(255)         DEFAULT NULL COMMENT '扩展字段',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_group_user` (`group_id`, `user_uid`) USING BTREE COMMENT '群成员唯一索引',
    KEY `idx_user_uid` (`user_uid`) USING BTREE COMMENT '用户群组查询索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='QT群组成员表';
