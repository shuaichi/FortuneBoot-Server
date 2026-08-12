-- ----------------------------
-- 1. 账户余额快照表
-- ----------------------------
CREATE TABLE `fortune_balance_snapshot`
(
    `snapshot_id`       bigint(20)     NOT NULL AUTO_INCREMENT COMMENT '主键',
    `group_id`          bigint(20)     NOT NULL COMMENT '分组ID',
    `book_id`           bigint(20)              DEFAULT NULL COMMENT '账本ID',
    `account_id`        bigint(20)     NOT NULL COMMENT '账户ID',
    `snapshot_date`     date           NOT NULL COMMENT '快照日期',
    `currency_code`     varchar(16)             DEFAULT NULL COMMENT '账户币种',
    `balance`           decimal(15, 2) NOT NULL DEFAULT '0.00' COMMENT '账户余额',
    `converted_balance` decimal(15, 2) NOT NULL DEFAULT '0.00' COMMENT '转换后余额',
    `total_assets`      decimal(15, 2) NOT NULL DEFAULT '0.00' COMMENT '总资产',
    `total_liabilities` decimal(15, 2) NOT NULL DEFAULT '0.00' COMMENT '总负债',
    `net_assets`        decimal(15, 2) NOT NULL DEFAULT '0.00' COMMENT '净资产',
    `creator_id`        bigint(20)              DEFAULT NULL COMMENT '创建者ID',
    `create_time`       datetime                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater_id`        bigint(20)              DEFAULT NULL COMMENT '更新者ID',
    `update_time`       datetime                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           tinyint(1)     NOT NULL DEFAULT '0' COMMENT '删除标志',
    PRIMARY KEY (`snapshot_id`),
    UNIQUE KEY `uk_snapshot_account` (`snapshot_date`, `account_id`),
    KEY `idx_group_deleted_date` (`group_id`, `deleted`, `snapshot_date`),
    KEY `idx_account_deleted_date` (`account_id`, `deleted`, `snapshot_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户余额快照表';

-- ----------------------------
-- 2. 统计口径默认配置
-- ----------------------------
INSERT INTO sys_config (config_name, config_key, config_options, config_value, is_allow_change, remark, creator_id, create_time, updater_id, update_time, deleted)
SELECT '统计口径-转账不计入支出', 'fortune.include.excludeTransferFromExpense', '["true","false"]', 'true', 1, '统计模块口径说明：转账默认不计入支出', 1, NOW(), 1, NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'fortune.include.excludeTransferFromExpense')
UNION ALL
SELECT '统计口径-借贷不计入支出', 'fortune.include.excludeLoanFromExpense', '["true","false"]', 'true', 1, '统计模块口径说明：借贷默认不计入支出', 1, NOW(), 1, NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'fortune.include.excludeLoanFromExpense')
UNION ALL
SELECT '统计口径-包含未确认账单', 'fortune.include.includeUnconfirmed', '["true","false"]', 'true', 1, '统计模块口径说明：默认包含未确认账单', 1, NOW(), 1, NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'fortune.include.includeUnconfirmed');
-- ----------------------------
-- 3. 复用旧报表菜单为统计菜单
-- ----------------------------
UPDATE sys_menu
SET menu_name = '收支报表',
    menu_type = 1,
    router_name = 'FortuneStatisticsBill',
    parent_id = (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE path = '/report' AND deleted = 0 LIMIT 1) report_menu),
    path = '/fortune/statistics/bill/index',
    is_button = 0,
    permission = '',
    meta_info = '{"title":"收支报表","icon":"fa:bar-chart","showLink":true,"showParent":true,"rank":1}',
    status = 1,
    remark = '收支报表统计',
    updater_id = 1,
    update_time = NOW(),
    deleted = 0
WHERE menu_id = 91;

UPDATE sys_menu
SET menu_name = '资产负债',
    menu_type = 1,
    router_name = 'FortuneStatisticsAssetsLiabilities',
    parent_id = (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE path = '/report' AND deleted = 0 LIMIT 1) report_menu),
    path = '/fortune/statistics/assets-liabilities/index',
    is_button = 0,
    permission = '',
    meta_info = '{"title":"资产负债","icon":"fa:balance-scale","showLink":true,"showParent":true,"rank":2}',
    status = 1,
    remark = '资产负债统计',
    updater_id = 1,
    update_time = NOW(),
    deleted = 0
WHERE menu_id = 92;

UPDATE sys_menu
SET menu_name = '借贷理财',
    menu_type = 1,
    router_name = 'FortuneStatisticsLoanFinance',
    parent_id = (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE path = '/report' AND deleted = 0 LIMIT 1) report_menu),
    path = '/fortune/statistics/loan-finance/index',
    is_button = 0,
    permission = '',
    meta_info = '{"title":"借贷理财","icon":"fa:handshake-o","showLink":true,"showParent":true,"rank":3}',
    status = 1,
    remark = '借贷理财统计',
    updater_id = 1,
    update_time = NOW(),
    deleted = 0
WHERE menu_id = 93;

-- 删除旧报表中心下已废弃的多余菜单及其角色授权
DELETE FROM sys_role_menu
WHERE menu_id IN (94, 95, 96);

DELETE FROM sys_menu
WHERE menu_id IN (94, 95, 96);
