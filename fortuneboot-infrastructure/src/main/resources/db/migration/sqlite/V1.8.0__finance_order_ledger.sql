-- FortuneBoot V1.8.0 单据账本扩展（SQLite）

-- 1. fortune_finance_order 增量扩展
alter table fortune_finance_order
    add column counterparty_id INTEGER;

alter table fortune_finance_order
    add column counterparty_name TEXT;

alter table fortune_finance_order
    add column currency_code TEXT;

alter table fortune_finance_order
    add column due_date TEXT;

alter table fortune_finance_order
    add column adjusted_amount DECIMAL(20, 4) DEFAULT 0.0000 NOT NULL;

alter table fortune_finance_order
    add column version INTEGER DEFAULT 0 NOT NULL;

alter table fortune_finance_order
    add column reconciliation_status TEXT DEFAULT 'REVIEW_REQUIRED' NOT NULL;

alter table fortune_finance_order
    add column reconciliation_note TEXT;

CREATE INDEX idx_finance_order_book_type_status
    ON fortune_finance_order (book_id, type, status, deleted);

CREATE INDEX idx_finance_order_book_due_date
    ON fortune_finance_order (book_id, due_date, deleted);

-- 2. fortune_bill 增量扩展
alter table fortune_bill
    add column order_component TEXT;

alter table fortune_bill
    add column order_ledger_version INTEGER DEFAULT 0 NOT NULL;

CREATE INDEX idx_fortune_bill_book_order_ledger
    ON fortune_bill (book_id, order_id, deleted, recycle_bin, confirm);

-- 3. 单据幂等请求表
create table fortune_finance_order_request
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id       INTEGER NOT NULL,
    request_id    TEXT    NOT NULL,
    operation     TEXT    NOT NULL,
    request_hash  TEXT    NOT NULL,
    creator_id    INTEGER,
    response_json TEXT,
    create_time   TEXT
);

CREATE UNIQUE INDEX uk_finance_order_request
    ON fortune_finance_order_request (book_id, request_id);

-- 4. 单据操作审计表
create table fortune_finance_order_audit
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id     INTEGER NOT NULL,
    order_id    INTEGER,
    bill_id     INTEGER,
    request_id  TEXT,
    action      TEXT    NOT NULL,
    before_json TEXT,
    after_json  TEXT,
    creator_id  INTEGER,
    create_time TEXT
);
