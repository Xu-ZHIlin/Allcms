CREATE TABLE IF NOT EXISTS `sys_user` (
                                          `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                                          `username`        VARCHAR(50)  NOT NULL COMMENT '登录账号，唯一',
    `password`        VARCHAR(200) NOT NULL COMMENT '加密后的密码（BCrypt）',
    `real_name`       VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    `phone`           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `email`           VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `avatar`          VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `last_login_time` DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`) COMMENT '账号唯一索引',
    KEY `idx_status` (`status`) COMMENT '状态索引，加速列表筛选',
    KEY `idx_phone` (`phone`) COMMENT '手机号索引，按需使用'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统用户表';