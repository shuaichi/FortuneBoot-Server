-- ----------------------------
-- 1. 账单成员表
-- ----------------------------
CREATE TABLE `fortune_member`
(
    `member_id`   bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `book_id`     bigint(20) NOT NULL COMMENT '账本ID',
    `member_name` varchar(50) NOT NULL COMMENT '成员名称',
    `sort`        int(11) DEFAULT '0' COMMENT '排序',
    `enable`      tinyint(1) DEFAULT '1' COMMENT '是否启用',
    `remark`      varchar(512) DEFAULT NULL COMMENT '备注',
    `recycle_bin` tinyint(1) DEFAULT '0' COMMENT '回收站',
    `creator_id`  bigint(20) DEFAULT NULL COMMENT '创建者ID',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater_id`  bigint(20) DEFAULT NULL COMMENT '更新者ID',
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     tinyint(1) DEFAULT '0' COMMENT '删除标志',
    PRIMARY KEY (`member_id`),
    KEY           `idx_book_id` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单成员表';

-- ----------------------------
-- 2. 账单与成员关系表
-- ----------------------------
CREATE TABLE `fortune_member_relation`
(
    `member_relation_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `bill_id`            bigint(20) NOT NULL COMMENT '账单ID',
    `member_id`          bigint(20) NOT NULL COMMENT '成员ID',
    `creator_id`         bigint(20) DEFAULT NULL COMMENT '创建者ID',
    `create_time`        datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater_id`         bigint(20) DEFAULT NULL COMMENT '更新者ID',
    `update_time`        datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`            tinyint(1) DEFAULT '0' COMMENT '删除标志',
    PRIMARY KEY (`member_relation_id`),
    KEY                  `idx_bill_id` (`bill_id`),
    KEY                  `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单成员关系表';