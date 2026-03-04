drop schema if exists `QTalk`;
create schema `QTalk` default character set utf8mb4 collate utf8mb4_general_ci;
use QTalk;

DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`
(
    `id`          bigint(20)                                               NOT NULL COMMENT '主键',
    `user_id`     varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci   NULL DEFAULT NULL COMMENT '用户id',
    `uid`         varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci   NULL DEFAULT NULL COMMENT 'uid',
    `operation`   varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci   NULL DEFAULT NULL COMMENT '用户操作：DELETE ADD GET UPDATE',
    `time`        int(11)                                                  NULL DEFAULT NULL COMMENT '响应时间,单位毫秒',
    `method`      varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL COMMENT '请求方法（控制层方法全限定名）',
    `params`      varchar(5000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求参数',
    `ip`          varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci   NULL DEFAULT NULL COMMENT 'IP地址',
    `create_time` datetime(0)                                              NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '系统日志'
  ROW_FORMAT = COMPACT;

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
(
    `id`              bigint(20)                                              NOT NULL COMMENT '用户id',
    `uid`             varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT 'uid',
    `password`        varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户密码密文',
    `phone`           varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL COMMENT '手机号码',
    `real_name`       varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL COMMENT '真实名称',
    `nick_name`       varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL COMMENT '昵称',
    `email`           varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL COMMENT '邮箱(唯一)',
    `avatar`          varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户头像',
    `description`     varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户简介',
    `birthday`        datetime(0)                                             NULL DEFAULT NULL COMMENT '生日',
    `add_friends`     tinyint(4)                                              NULL DEFAULT 0 COMMENT '是否允许添加好友(0.同意后加好友 1.直接加好友 2.不允许加好友)',
    `last_login_time` datetime(0)                                             NULL DEFAULT NULL COMMENT '最后登录时间',
    `area_name`       varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '地区名称',
    `area_code`       varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '地区编码',
    `sex`             tinyint(4)                                              NULL DEFAULT 0 COMMENT '性别(0.未知 1.男 2.女)',
    `status`          tinyint(4)                                              NULL DEFAULT 0 COMMENT '账户状态(0.正常 1.锁定 )',
    `deleted`         tinyint(4)                                              NULL DEFAULT 0 COMMENT '是否删除(0未删除；1已删除)',
    `create_where`    tinyint(4)                                              NULL DEFAULT 1 COMMENT '创建来源(1.web 2.android 3.ios )',
    `create_time`     datetime(0)                                             NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`     datetime(0)                                             NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `unique_username` (`uid`) USING BTREE COMMENT '用户名唯一',
    UNIQUE INDEX `unique_email` (`email`) USING BTREE COMMENT '邮箱唯一'
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '用户表'
  ROW_FORMAT = COMPACT;

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`
(
    `id`          bigint(20)  NOT NULL COMMENT '主键',
    `user_id`     bigint(20)  NULL DEFAULT NULL COMMENT '用户id',
    `role_id`     bigint(20)  NULL DEFAULT NULL COMMENT '角色id',
    `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '用户角色表'
  ROW_FORMAT = COMPACT;

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`
(
    `id`          bigint(20)                                              NOT NULL COMMENT '主键',
    `name`        varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '角色名称',
    `description` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
    `status`      tinyint(4)                                              NULL DEFAULT 0 COMMENT '状态(0:正常 1:弃用)',
    `create_time` datetime(0)                                             NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime(0)                                             NULL DEFAULT NULL COMMENT '更新时间',
    `deleted`     tinyint(4)                                              NULL DEFAULT 0 COMMENT '是否删除(0未删除；1已删除)',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '角色表'
  ROW_FORMAT = COMPACT;

DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`
(
    `id`          bigint(20)                                              NOT NULL COMMENT '主键',
    `code`        varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL COMMENT '菜单权限编码(前端按钮权限标识)',
    `title`       varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '菜单权限名称',
    `icon`        varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT '' COMMENT '菜单图标(侧边导航栏图标)',
    `perms`       varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'SpringSecurity授权标识(如：sys:user:add)',
    `url`         varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '访问地址URL',
    `method`      varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL COMMENT '资源请求类型',
    `name`        varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT 'name与前端vue路由name约定一致',
    `pid`         bigint(20)                                              NULL DEFAULT 0 COMMENT '父级菜单权限id，pid等于0 为顶层权限',
    `order_num`   int(11)                                                 NULL DEFAULT 0 COMMENT '排序',
    `type`        tinyint(4) UNSIGNED ZEROFILL                            NULL DEFAULT 0001 COMMENT '菜单权限类型(1:目录;2:菜单;3:按钮)',
    `status`      tinyint(4)                                              NULL DEFAULT 0 COMMENT '状态(0:正常 1:禁用)',
    `create_time` datetime(0)                                             NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime(0)                                             NULL DEFAULT NULL COMMENT '更新时间',
    `deleted`     tinyint(4)                                              NULL DEFAULT 0 COMMENT '是否删除(0未删除；1已删除)',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '权限表（菜单）'
  ROW_FORMAT = COMPACT;

DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`
(
    `id`            bigint(20)  NOT NULL COMMENT '主键',
    `role_id`       bigint(20)  NULL DEFAULT NULL COMMENT '角色id',
    `permission_id` bigint(20)  NULL DEFAULT NULL COMMENT '菜单权限id',
    `create_time`   datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '角色权限表'
  ROW_FORMAT = COMPACT;

DROP TABLE IF EXISTS `sys_version`;
CREATE TABLE `sys_version`
(
    `id`              bigint(20)                                              NOT NULL COMMENT '主键 ID',
    `version`         varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '版本号 (如：1.0.0)',
    `platform`        tinyint(4)                                              NOT NULL DEFAULT 1 COMMENT '平台类型 (1:Web 端 2:Android 3: iOS)',
    `update_desc`     varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '更新描述 (支持 HTML/Markdown)',
    `file_url`        varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '安装包/更新文件 URL',
    `file_size`       bigint(20)                                              NULL DEFAULT NULL COMMENT '文件大小 (字节)',
    `file_md5`        varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL COMMENT '文件 MD5 校验值',
    `file_type`       tinyint(4)                                              NULL DEFAULT 1 COMMENT '文件类型 (1:完整包 2:增量包 3:外部分发链接)',
    `outer_link`      varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '外部下载链接 (如第三方存储)',
    `status`          tinyint(4)                                              NOT NULL DEFAULT 0 COMMENT '状态 (0:未发布, 1:全网发布, 2:灰度发布)',
    `grayscale_uids`  varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '灰度测试用户 UID 列表 (逗号分隔)',
    `publish_time`    datetime(0)                                             NULL DEFAULT NULL COMMENT '发布时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `unique_version_platform` (`version`, `platform`) USING BTREE COMMENT '同一平台版本号唯一',
    INDEX `idx_status` (`status`) USING BTREE COMMENT '状态索引',
    INDEX `idx_platform` (`platform`) USING BTREE COMMENT '平台索引',
    INDEX `idx_publish_time` (`publish_time`) USING BTREE COMMENT '发布时间索引'
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '系统版本管理表'
  ROW_FORMAT = COMPACT;
