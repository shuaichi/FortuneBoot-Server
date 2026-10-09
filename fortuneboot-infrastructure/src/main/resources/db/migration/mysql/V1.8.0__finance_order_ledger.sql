-- FortuneBoot V1.8.0 单据账本扩展
-- 1. fortune_finance_order 增量扩展
alter table fortune_finance_order
    add column counterparty_id bigint null comment '交易对象id' after remark,
    add column counterparty_name varchar(64) null comment '交易对象名称快照' after counterparty_id,
    add column currency_code varchar(16) null comment '币种' after counterparty_name,
    add column due_date date null comment '到期日' after currency_code,
    add column adjusted_amount decimal(20, 4) default 0.0000 not null comment '差额结清累计金额' after due_date,
    add column version bigint default 0 not null comment '乐观锁版本号' after adjusted_amount,
    add column reconciliation_status varchar(24) default 'REVIEW_REQUIRED' not null comment '核对状态' after version,
    add column reconciliation_note varchar(512) null comment '核对问题摘要' after reconciliation_status;

create index idx_finance_order_book_type_status
    on fortune_finance_order (book_id, type, status, deleted);

create index idx_finance_order_book_due_date
    on fortune_finance_order (book_id, due_date, deleted);

-- 2. fortune_bill 增量扩展
alter table fortune_bill
    add column order_component varchar(16) null comment '单据组件' after order_id,
    add column order_ledger_version smallint default 0 not null comment '单据账本版本' after order_component;

create index idx_fortune_bill_book_order_ledger
    on fortune_bill (book_id, order_id, deleted, recycle_bin, confirm);

-- 3. 单据幂等请求表
create table fortune_finance_order_request
(
    id            bigint auto_increment comment '主键'
        primary key,
    book_id       bigint       not null comment '账本id',
    request_id    varchar(64)  not null comment '请求幂等ID',
    operation     varchar(32)  not null comment '操作类型',
    request_hash  varchar(64)  not null comment '请求内容哈希',
    creator_id    bigint       null comment '创建者ID',
    response_json text         null comment '成功事务回执',
    create_time   datetime     null comment '创建时间',
    constraint uk_finance_order_request
        unique (book_id, request_id)
)
    comment '单据幂等请求表';

-- 4. 单据操作审计表
create table fortune_finance_order_audit
(
    id          bigint auto_increment comment '主键'
        primary key,
    book_id     bigint      not null comment '账本id',
    order_id    bigint      null comment '单据id',
    bill_id     bigint      null comment '流水id',
    request_id  varchar(64) null comment '请求幂等ID',
    action      varchar(32) not null comment '操作动作',
    before_json text        null comment '操作前快照',
    after_json  text        null comment '操作后快照',
    creator_id  bigint      null comment '操作者ID',
    create_time datetime    null comment '创建时间'
)
    comment '单据操作审计表';
