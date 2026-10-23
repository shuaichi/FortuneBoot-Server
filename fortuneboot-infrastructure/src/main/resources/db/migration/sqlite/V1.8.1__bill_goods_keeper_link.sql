-- FortuneBoot V1.8.1 账单关联归物 + 自动叠加成本（SQLite）

-- 1. fortune_bill 关联归物
alter table fortune_bill
    add column goods_keeper_id INTEGER;

CREATE INDEX idx_fortune_bill_goods_keeper
    ON fortune_bill (goods_keeper_id, book_id, deleted);

-- 2. fortune_goods_keeper 累计关联成本
-- 由关联账单自动叠加：支出账单金额累加、收入账单金额冲减
alter table fortune_goods_keeper
    add column accumulated_cost DECIMAL(19, 2) DEFAULT 0.00 NOT NULL;
