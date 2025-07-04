create table if not exists fortune_recurring_bill_log
(
    log_id             bigint auto_increment comment '主键'
    primary key,
    rule_id            bigint                   not null comment '周期记账规则id',
    execution_time     datetime                 not null comment '执行时间',
    status             tinyint                  not null comment '执行状态：1-成功 2-失败 3-跳过',
    bill_id            bigint                   not null comment '生成的交易记录ID',
    error_msg          varchar(1024) default '' not null comment '错误信息',
    execution_duration int           default 0  not null comment '执行耗时（毫秒）',
    creator_id         bigint                   null comment '创建者ID',
    updater_id         bigint                   null comment '更新者ID',
    update_time        datetime                 null comment '更新时间',
    create_time        datetime                 null comment '创建时间',
    deleted            tinyint(1)    default 0  not null comment '逻辑删除'
    )
    comment '周期记账执行日志表';

create index idx_execution_time
    on fortune_recurring_bill_log (execution_time);

create index idx_rule_id
    on fortune_recurring_bill_log (rule_id);

create index idx_status
    on fortune_recurring_bill_log (status);

create table if not exists fortune_recurring_bill_rule
(
    rule_id             bigint auto_increment comment '主键'
    primary key,
    book_id             bigint                   not null,
    rule_name           varchar(100)             not null comment '规则名称',
    cron_expression     varchar(100)             not null comment 'Cron表达式',
    enable              tinyint(1)    default 1  not null comment '启用状态',
    bill_request        json                     not null comment '执行的账单参数',
    start_date          date                     null comment '开始日期',
    end_date            date                     null comment '结束日期',
    max_executions      bigint                   null comment '最大执行次数，NULL表示无限制',
    executed_count      bigint        default 0  null comment '已执行次数',
    last_executed_time  datetime                 null comment '上次执行时间',
    next_execution_time datetime                 null comment '下次执行时间',
    last_recovery_check datetime                 null comment '上次回复检查时间',
    recovery_strategy   int                      null comment '补偿策略1、全部补偿 2、仅补偿最近N次 3、不补偿',
    max_recovery_count  int                      null comment '最大补偿次数',
    remark              varchar(1024) default '' not null comment '备注',
    creator_id          bigint                   null comment '创建者ID',
    updater_id          bigint                   null comment '更新者ID',
    update_time         datetime                 null comment '更新时间',
    create_time         datetime                 null comment '创建时间',
    deleted             tinyint(1)    default 0  not null comment '逻辑删除'
    )
    comment '周期记账规则表';

create index idx_book_id
    on fortune_recurring_bill_rule (book_id);

