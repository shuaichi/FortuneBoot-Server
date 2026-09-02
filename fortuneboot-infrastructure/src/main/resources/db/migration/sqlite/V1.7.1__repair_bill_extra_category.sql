-- 历史账单附加项分类修复：仅补齐非转账账单中 category_id 为空的手续费/优惠。
-- 审计表同时保存写前备份、分类决策和受保护回滚所需的候选分类。
CREATE TABLE fortune_bill_extra_category_repair_audit
(
    repair_id               INTEGER PRIMARY KEY AUTOINCREMENT,
    migration_version       TEXT NOT NULL,
    extra_id                INTEGER NOT NULL,
    bill_id                 INTEGER NOT NULL,
    bill_type               INTEGER NOT NULL,
    previous_category_id    INTEGER,
    active_relation_count   INTEGER NOT NULL,
    distinct_category_count INTEGER NOT NULL,
    candidate_category_id   INTEGER,
    decision                TEXT NOT NULL,
    executed_by             TEXT NOT NULL,
    snapshot_time           TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (migration_version, extra_id)
);

CREATE INDEX idx_bill_extra_category_repair_audit_extra
    ON fortune_bill_extra_category_repair_audit (extra_id);
CREATE INDEX idx_bill_extra_category_repair_audit_decision
    ON fortune_bill_extra_category_repair_audit (decision);

-- category_relation_id 按创建时 categoryAmountPair 的持久化顺序递增；多分类时取首个有效分类。
INSERT INTO fortune_bill_extra_category_repair_audit
(
    migration_version,
    extra_id,
    bill_id,
    bill_type,
    previous_category_id,
    active_relation_count,
    distinct_category_count,
    candidate_category_id,
    decision,
    executed_by
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
WHERE e.category_id IS NULL
  AND e.deleted = 0
  AND b.deleted = 0
  AND b.bill_type IS NOT NULL
  AND b.bill_type <> 3;

-- 使用冻结的审计候选值，不在更新阶段重新推导分类；再次限制空值，避免覆盖并发人工修改。
UPDATE fortune_bill_extra
SET category_id = (
    SELECT audit.candidate_category_id
    FROM fortune_bill_extra_category_repair_audit audit
    WHERE audit.migration_version = '1.8.0'
      AND audit.extra_id = fortune_bill_extra.extra_id
      AND audit.decision IN ('SINGLE_CATEGORY', 'AUTO_FIRST_CATEGORY')
)
WHERE category_id IS NULL
  AND deleted = 0
  AND EXISTS (
      SELECT 1
      FROM fortune_bill_extra_category_repair_audit audit
      INNER JOIN fortune_bill b ON b.bill_id = fortune_bill_extra.bill_id
      WHERE audit.migration_version = '1.8.0'
        AND audit.extra_id = fortune_bill_extra.extra_id
        AND audit.decision IN ('SINGLE_CATEGORY', 'AUTO_FIRST_CATEGORY')
        AND audit.candidate_category_id IS NOT NULL
        AND b.deleted = 0
        AND b.bill_type IS NOT NULL
        AND b.bill_type <> 3
        AND (
            SELECT COUNT(DISTINCT cr.category_id)
            FROM fortune_category_relation cr
            WHERE cr.bill_id = fortune_bill_extra.bill_id
              AND cr.deleted = 0
              AND cr.category_id IS NOT NULL
        ) = audit.distinct_category_count
        AND (
            SELECT cr.category_id
            FROM fortune_category_relation cr
            WHERE cr.bill_id = fortune_bill_extra.bill_id
              AND cr.deleted = 0
              AND cr.category_id IS NOT NULL
            ORDER BY cr.category_relation_id ASC
            LIMIT 1
        ) = audit.candidate_category_id
  );

-- 运维回滚预检：先确认将受影响的记录。
-- SELECT e.extra_id, e.bill_id, e.category_id, audit.previous_category_id
-- FROM fortune_bill_extra e
-- INNER JOIN fortune_bill_extra_category_repair_audit audit
--     ON audit.extra_id = e.extra_id AND audit.migration_version = '1.8.0'
-- WHERE audit.decision IN ('SINGLE_CATEGORY', 'AUTO_FIRST_CATEGORY')
--   AND e.category_id IS audit.candidate_category_id;
--
-- 运维回滚：仅恢复仍保留本迁移候选分类的记录，绝不覆盖后续不同分类修改。
-- UPDATE fortune_bill_extra
-- SET category_id = (
--     SELECT audit.previous_category_id
--     FROM fortune_bill_extra_category_repair_audit audit
--     WHERE audit.migration_version = '1.8.0'
--       AND audit.extra_id = fortune_bill_extra.extra_id
--       AND audit.decision IN ('SINGLE_CATEGORY', 'AUTO_FIRST_CATEGORY')
-- )
-- WHERE EXISTS (
--     SELECT 1
--     FROM fortune_bill_extra_category_repair_audit audit
--     WHERE audit.migration_version = '1.8.0'
--       AND audit.extra_id = fortune_bill_extra.extra_id
--       AND audit.decision IN ('SINGLE_CATEGORY', 'AUTO_FIRST_CATEGORY')
--       AND fortune_bill_extra.category_id IS audit.candidate_category_id
-- );
