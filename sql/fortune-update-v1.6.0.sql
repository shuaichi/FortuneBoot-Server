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
    KEY           `idx_book_id` (`book_id`),
    -- 优化：覆盖常见查询条件（按账本查未删除、启用的成员，并按排序字段排序）
    KEY           `idx_book_deleted_enable_sort` (`book_id`, `deleted`, `enable`, `sort`)
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
    -- 优化：防止同一账单重复关联同一成员
    UNIQUE KEY `uk_bill_member` (`bill_id`, `member_id`),
    -- 优化：查询某账单下未删除的成员列表
    KEY                  `idx_bill_deleted` (`bill_id`, `deleted`),
    -- 优化：查询某成员参与的未删除账单
    KEY                  `idx_member_deleted` (`member_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单成员关系表';

INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (80,'成员管理', 1, 'FortuneBookMember', 84, '/fortune/member/index', 0, '', '{"title":"成员管理","showLink":false,"showParent":true}', 1, '', 1, '2025-02-15 02:41:14', 1, '2025-02-22 17:09:19', 0);

-- ----------------------------
-- 3. 账单附加费用表（手续费/优惠）
-- ----------------------------
CREATE TABLE `fortune_bill_extra`
(
    `extra_id`    bigint(20)     NOT NULL AUTO_INCREMENT COMMENT '主键',
    `bill_id`     bigint(20)     NOT NULL COMMENT '关联账单ID',
    `extra_type`  tinyint(4)     NOT NULL COMMENT '附加类型：1-手续费，2-优惠',
    `amount`      decimal(15, 2) NOT NULL COMMENT '金额',
    `account_side` tinyint(4)    NOT NULL DEFAULT '1' COMMENT '账户方向：1-转出账户(from)，2-转入账户(to)',
    `category_id` bigint(20)              DEFAULT NULL COMMENT '关联分类ID（可为空）',
    `remark`      varchar(255)            DEFAULT NULL COMMENT '备注',
    `creator_id`  bigint(20)              DEFAULT NULL COMMENT '创建者ID',
    `create_time` datetime                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater_id`  bigint(20)              DEFAULT NULL COMMENT '更新者ID',
    `update_time` datetime                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     tinyint(1)     NOT NULL DEFAULT '0' COMMENT '删除标志（0-存在 1-删除）',
    PRIMARY KEY (`extra_id`),
    KEY `idx_bill_deleted` (`bill_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单附加费用表（手续费/优惠）';