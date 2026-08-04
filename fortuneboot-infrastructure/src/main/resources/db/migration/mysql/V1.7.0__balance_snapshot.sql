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
    KEY `idx_group_date_deleted` (`group_id`, `snapshot_date`, `deleted`),
    KEY `idx_account_date_deleted` (`account_id`, `snapshot_date`, `deleted`)
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
-- 1. 定位「报表中心」目录 id（若你想挂到别处，改这里的 path 即可）
SET @report_id = (
  SELECT menu_id FROM sys_menu
  WHERE path = '/report' AND deleted = 0
  LIMIT 1
);

-- 2. 取当前最大 menu_id 作为递增基准
SET @base_id = (SELECT COALESCE(MAX(menu_id), 0) FROM sys_menu);

-- 3. 插入三个统计菜单（menu_type=1 菜单，is_button=0，无按钮权限）
INSERT INTO sys_menu
(menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
VALUES
    (@base_id + 1, '收支报表', 1, 'FortuneStatisticsBill', @report_id, '/fortune/statistics/bill/index', 0, '',
     '{"title":"收支报表","icon":"fa:bar-chart","showLink":true,"showParent":true,"rank":7}', 1, '收支报表统计', 1, NOW(), NULL, NULL, 0),

    (@base_id + 2, '资产负债', 1, 'FortuneStatisticsAssetsLiabilities', @report_id, '/fortune/statistics/assets-liabilities/index', 0, '',
     '{"title":"资产负债","icon":"fa:balance-scale","showLink":true,"showParent":true,"rank":8}', 1, '资产负债统计', 1, NOW(), NULL, NULL, 0),

    (@base_id + 3, '借贷理财', 1, 'FortuneStatisticsLoanFinance', @report_id, '/fortune/statistics/loan-finance/index', 0, '',
     '{"title":"借贷理财","icon":"fa:handshake-o","showLink":true,"showParent":true,"rank":9}', 1, '借贷理财统计', 1, NOW(), NULL, NULL, 0);