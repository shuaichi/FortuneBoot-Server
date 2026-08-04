-- ----------------------------
-- 1. 账户余额快照表 (SQLite)
-- ----------------------------
CREATE TABLE `fortune_balance_snapshot`
(
    `snapshot_id`       INTEGER PRIMARY KEY AUTOINCREMENT, -- 主键
    `group_id`          INTEGER        NOT NULL,           -- 分组ID
    `book_id`           INTEGER,                           -- 账本ID
    `account_id`        INTEGER        NOT NULL,           -- 账户ID
    `snapshot_date`     TEXT           NOT NULL,           -- 快照日期
    `currency_code`     TEXT,                              -- 账户币种
    `balance`           NUMERIC(15, 2) NOT NULL DEFAULT 0, -- 账户余额
    `converted_balance` NUMERIC(15, 2) NOT NULL DEFAULT 0, -- 转换后余额
    `total_assets`      NUMERIC(15, 2) NOT NULL DEFAULT 0, -- 总资产
    `total_liabilities` NUMERIC(15, 2) NOT NULL DEFAULT 0, -- 总负债
    `net_assets`        NUMERIC(15, 2) NOT NULL DEFAULT 0, -- 净资产
    `creator_id`        INTEGER,                           -- 创建者ID
    `create_time`       TEXT           DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    `updater_id`        INTEGER,                           -- 更新者ID
    `update_time`       TEXT           DEFAULT CURRENT_TIMESTAMP, -- 更新时间
    `deleted`           INTEGER        NOT NULL DEFAULT 0  -- 删除标志
);
CREATE UNIQUE INDEX uk_fortune_balance_snapshot_snapshot_account ON fortune_balance_snapshot (snapshot_date, account_id);
CREATE INDEX idx_fortune_balance_snapshot_group_date_deleted ON fortune_balance_snapshot (group_id, snapshot_date, deleted);
CREATE INDEX idx_fortune_balance_snapshot_account_date_deleted ON fortune_balance_snapshot (account_id, snapshot_date, deleted);

-- ----------------------------
-- 2. 统计口径默认配置 (SQLite)
-- ----------------------------
INSERT INTO sys_config (config_name, config_key, config_options, config_value, is_allow_change, remark, creator_id, create_time, updater_id, update_time, deleted)
SELECT '统计口径-转账不计入支出', 'fortune.include.excludeTransferFromExpense', '["true","false"]', 'true', 1, '统计模块口径说明：转账默认不计入支出', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'fortune.include.excludeTransferFromExpense')
UNION ALL
SELECT '统计口径-借贷不计入支出', 'fortune.include.excludeLoanFromExpense', '["true","false"]', 'true', 1, '统计模块口径说明：借贷默认不计入支出', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'fortune.include.excludeLoanFromExpense')
UNION ALL
SELECT '统计口径-包含未确认账单', 'fortune.include.includeUnconfirmed', '["true","false"]', 'true', 1, '统计模块口径说明：默认包含未确认账单', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'fortune.include.includeUnconfirmed');
-- 1. 定位「报表中心」目录 id（若你想挂到别处，改这里的 path 即可）
-- 2. 取当前最大 menu_id 作为递增基准
-- 3. 插入三个统计菜单（menu_type=1 菜单，is_button=0，无按钮权限）
WITH
    report AS (
        SELECT menu_id
        FROM sys_menu
        WHERE path = '/report' AND deleted = 0
    LIMIT 1
    ),
    base AS (
SELECT COALESCE(MAX(menu_id), 0) AS max_id
FROM sys_menu
    )
INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path,
    is_button, permission, meta_info, status, remark,
    creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    max_id + 1, '收支报表', 1, 'FortuneStatisticsBill',
    (SELECT menu_id FROM report), '/fortune/statistics/bill/index',
    0, '', '{"title":"收支报表","icon":"fa:bar-chart","showLink":true,"showParent":true,"rank":7}',
    1, '收支报表统计', 1, CURRENT_TIMESTAMP, NULL, NULL, 0
FROM base

UNION ALL

SELECT
    max_id + 2, '资产负债', 1, 'FortuneStatisticsAssetsLiabilities',
    (SELECT menu_id FROM report), '/fortune/statistics/assets-liabilities/index',
    0, '', '{"title":"资产负债","icon":"fa:balance-scale","showLink":true,"showParent":true,"rank":8}',
    1, '资产负债统计', 1, CURRENT_TIMESTAMP, NULL, NULL, 0
FROM base

UNION ALL

SELECT
    max_id + 3, '借贷理财', 1, 'FortuneStatisticsLoanFinance',
    (SELECT menu_id FROM report), '/fortune/statistics/loan-finance/index',
    0, '', '{"title":"借贷理财","icon":"fa:handshake-o","showLink":true,"showParent":true,"rank":9}',
    1, '借贷理财统计', 1, CURRENT_TIMESTAMP, NULL, NULL, 0
FROM base;