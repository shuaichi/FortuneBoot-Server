-- FortuneBoot V1.8.1 账单关联归物 + 自动叠加成本
-- 1. fortune_bill 关联归物
alter table fortune_bill
    add column goods_keeper_id bigint null comment '关联归物id（仅收入/支出账单可关联）' after order_ledger_version;

create index idx_fortune_bill_goods_keeper
    on fortune_bill (goods_keeper_id, book_id, deleted);

-- 2. fortune_goods_keeper 累计关联成本
-- 由关联账单自动叠加：支出账单金额累加、收入账单金额冲减
alter table fortune_goods_keeper
    add column accumulated_cost decimal(19, 2) default 0.00 not null comment '关联账单自动叠加的净成本（支出加/收入减）' after sold_price;
