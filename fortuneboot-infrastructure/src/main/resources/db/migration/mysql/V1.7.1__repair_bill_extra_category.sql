-- 历史账单附加项分类修复：仅补齐非转账账单中 category_id 为空的手续费/优惠。
-- 审计表同时保存写前备份、分类决策和受保护回滚所需的候选分类。
-- MySQL DDL 会隐式提交；若升级中断并在 Flyway repair 后重试，保留已建审计表与已冻结的快照。
CREATE TABLE IF NOT EXISTS `fortune_bill_extra_category_repair_audit`
(
    `repair_id`               bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键',
    `migration_version`       varchar(32) NOT NULL COMMENT 'Flyway 修复版本',
    `extra_id`                bigint(20)  NOT NULL COMMENT '附加项ID',
    `bill_id`                 bigint(20)  NOT NULL COMMENT '账单ID',
    `bill_type`               tinyint(4)  NOT NULL COMMENT '账单类型',
    `previous_category_id`    bigint(20)           DEFAULT NULL COMMENT '修复前分类ID',
    `active_relation_count`   int(11)     NOT NULL COMMENT '有效分类关系行数',
    `distinct_category_count` int(11)     NOT NULL COMMENT '有效不同分类数',
    `candidate_category_id`   bigint(20)           DEFAULT NULL COMMENT '修复候选分类ID',
    `decision`                varchar(64) NOT NULL COMMENT '修复决策',
    `executed_by`             varchar(64) NOT NULL COMMENT '执行者',
    `snapshot_time`           datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '快照时间',
    PRIMARY KEY (`repair_id`),
    UNIQUE KEY `uk_version_extra` (`migration_version`, `extra_id`),
    KEY `idx_extra` (`extra_id`),
    KEY `idx_decision` (`decision`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单附加项分类修复审计表';

-- category_relation_id 按创建时 categoryAmountPair 的持久化顺序递增；多分类时取首个有效分类。
INSERT INTO `fortune_bill_extra_category_repair_audit`
(
    `migration_version`,
    `extra_id`,
    `bill_id`,
    `bill_type`,
    `previous_category_id`,
    `active_relation_count`,
    `distinct_category_count`,
    `candidate_category_id`,
    `decision`,
    `executed_by`
)
SELECT
    '1.8.0',
    e.extra_id,
    e.bill_id,
    b.bill_type,
    e.category_id,
    (
        SELECT COUNT(*)
        FROM fortune_category_relation cr
        WHERE cr.bill_id = e.bill_id
          AND cr.deleted = 0
          AND cr.category_id IS NOT NULL
    ),
    (
        SELECT COUNT(DISTINCT cr.category_id)
        FROM fortune_category_relation cr
        WHERE cr.bill_id = e.bill_id
          AND cr.deleted = 0
          AND cr.category_id IS NOT NULL
    ),
    (
        SELECT cr.category_id
        FROM fortune_category_relation cr
        WHERE cr.bill_id = e.bill_id
          AND cr.deleted = 0
          AND cr.category_id IS NOT NULL
        ORDER BY cr.category_relation_id ASC
        LIMIT 1
    ),
    CASE
        WHEN (
            SELECT COUNT(DISTINCT cr.category_id)
            FROM fortune_category_relation cr
            WHERE cr.bill_id = e.bill_id
              AND cr.deleted = 0
              AND cr.category_id IS NOT NULL
        ) = 0 THEN 'SKIP_NO_CATEGORY'
        WHEN (
            SELECT COUNT(DISTINCT cr.category_id)
            FROM fortune_category_relation cr
            WHERE cr.bill_id = e.bill_id
              AND cr.deleted = 0
              AND cr.category_id IS NOT NULL
        ) = 1 THEN 'SINGLE_CATEGORY'
        ELSE 'AUTO_FIRST_CATEGORY'
    END,
    'FLYWAY_V1.8.0'
FROM fortune_bill_extra e
INNER JOIN fortune_bill b ON b.bill_id = e.bill_id
WHERE NOT EXISTS (
      SELECT 1
      FROM fortune_bill_extra_category_repair_audit existing_audit
      WHERE existing_audit.migration_version = '1.8.0'
        AND existing_audit.extra_id = e.extra_id
  )
  AND e.category_id IS NULL
  AND e.deleted = 0
  AND b.deleted = 0
  AND b.bill_type IS NOT NULL
  AND b.bill_type <> 3;

-- 使用冻结的审计候选值，不在更新阶段重新推导分类；再次限制空值，避免覆盖并发人工修改。
UPDATE fortune_bill_extra e
INNER JOIN fortune_bill_extra_category_repair_audit audit
    ON audit.extra_id = e.extra_id
   AND audit.migration_version = '1.8.0'
INNER JOIN fortune_bill b
    ON b.bill_id = e.bill_id
SET e.category_id = audit.candidate_category_id
WHERE audit.decision IN ('SINGLE_CATEGORY', 'AUTO_FIRST_CATEGORY')
  AND audit.candidate_category_id IS NOT NULL
  AND e.category_id IS NULL
  AND e.deleted = 0
  AND b.deleted = 0
  AND b.bill_type IS NOT NULL
  AND b.bill_type <> 3
  AND (
      SELECT COUNT(DISTINCT cr.category_id)
      FROM fortune_category_relation cr
      WHERE cr.bill_id = e.bill_id
        AND cr.deleted = 0
        AND cr.category_id IS NOT NULL
  ) = audit.distinct_category_count
  AND (
      SELECT cr.category_id
      FROM fortune_category_relation cr
      WHERE cr.bill_id = e.bill_id
        AND cr.deleted = 0
        AND cr.category_id IS NOT NULL
      ORDER BY cr.category_relation_id ASC
      LIMIT 1
  ) = audit.candidate_category_id;

-- 运维回滚预检：先确认将受影响的记录。
-- SELECT e.extra_id, e.bill_id, e.category_id, audit.previous_category_id
-- FROM fortune_bill_extra e
-- INNER JOIN fortune_bill_extra_category_repair_audit audit
--     ON audit.extra_id = e.extra_id AND audit.migration_version = '1.8.0'
-- WHERE audit.decision IN ('SINGLE_CATEGORY', 'AUTO_FIRST_CATEGORY')
--   AND e.category_id <=> audit.candidate_category_id;
--
-- 运维回滚：仅恢复仍保留本迁移候选分类的记录，绝不覆盖后续不同分类修改。
-- UPDATE fortune_bill_extra e
-- INNER JOIN fortune_bill_extra_category_repair_audit audit
--     ON audit.extra_id = e.extra_id
--    AND audit.migration_version = '1.8.0'
-- SET e.category_id = audit.previous_category_id
-- WHERE audit.decision IN ('SINGLE_CATEGORY', 'AUTO_FIRST_CATEGORY')
--   AND e.category_id <=> audit.candidate_category_id;
