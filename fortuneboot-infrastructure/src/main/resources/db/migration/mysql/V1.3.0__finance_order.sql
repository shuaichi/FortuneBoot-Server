-- FortuneBoot V1.3.0 增量迁移
-- fortune_bill 改造 + 报销单表 + 单据管理菜单

-- 修改 fortune_bill.bill_type 允许为空
alter table fortune_bill
    modify bill_type tinyint null comment '流水类型';

-- 新增 order_id 字段
alter table fortune_bill
    add order_id bigint null comment '关联单据id' after converted_amount;

-- 创建报销单表
create table fortune_finance_order
(
    order_id    bigint auto_increment comment '主键'
        primary key,
    book_id     bigint                        not null comment '账本id',
    title       varchar(128)                  not null comment '单据名称',
    type        smallint                      not null comment '单据类型',
    out_amount  decimal(20, 4) default 0.0000 not null comment '出金额',
    in_amount   decimal(20, 4) default 0.0000 null comment '入金额',
    status      smallint                      not null comment '状态',
    submit_time datetime                      null comment '提交时间',
    close_time  datetime                      null comment '关闭时间',
    remark      varchar(1024)  default ''     not null comment '备注',
    creator_id  bigint                        null comment '创建者ID',
    updater_id  bigint                        null comment '更新者ID',
    update_time datetime                      null comment '更新时间',
    create_time datetime                      null comment '创建时间',
    deleted     tinyint(1)     default 0      not null comment '逻辑删除'
)
    comment '报销单表';

create index idx_fortune_book_group_recycle_enable_sort
    on fortune_finance_order (book_id);

-- 单据管理菜单
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (90, '单据管理', 1, 'FortuneFinanceOrder', 66, '/fortune/finance-order/index', 0, '', '{"title":"单据管理","icon":"fa:first-order","showLink":true,"showParent":true,"rank":11}', 1, '', 1, '2025-10-07 16:40:44', 1, '2025-10-07 16:41:23', 0);