-- FortuneBoot V1.0.0 MySQL 基线建表脚本（初始版本）
-- 注意：此脚本代表项目最初发布时的数据库结构
-- 后续版本的变更通过增量脚本（V1.1.0~）应用

create table if not exists fortune_account
(
    account_id       bigint auto_increment comment '主键'
    primary key,
    card_no          varchar(128)                  null comment '卡号',
    account_name     varchar(64)                   null comment '账户名',
    balance          decimal(20, 4) default 0.0000 not null comment '余额',
    bill_day         date                          null comment '账单日',
    repay_day        date                          null comment '还款日',
    can_expense      tinyint(1)     default 0      null comment '可支出',
    can_income       tinyint(1)     default 0      null comment '可收入',
    can_transfer_out tinyint(1)     default 0      null comment '可转出',
    can_transfer_in  tinyint(1)     default 0      null comment '可转入',
    credit_limit     decimal                       null comment '信用额度',
    currency_code    varchar(16)    default 'CNY'  not null comment '币种',
    enable           tinyint(1)     default 1      not null comment '是否启用',
    include          tinyint(1)     default 1      not null comment '是否计入净资产',
    apr              decimal(8, 4)                 null comment '利率',
    initial_balance  decimal(20, 4) default 0.0000 null comment '期初余额',
    account_type     tinyint                       not null comment '账户类型 1、活期 2、信用 3、资产 4、贷款',
    group_id         bigint                        not null comment '分组id',
    sort             int                           null comment '排序',
    recycle_bin      tinyint(1)     default 0      not null comment '回收站',
    remark           varchar(1024)  default ''     not null comment '备注',
    creator_id       bigint                        null comment '创建者ID',
    updater_id       bigint                        null comment '更新者ID',
    update_time      datetime                      null comment '更新时间',
    create_time      datetime                      null comment '创建时间',
    deleted          tinyint(1)     default 0      not null comment '逻辑删除'
    )
    comment '账户表';

create table if not exists fortune_alert
(
    alert_id        bigint auto_increment comment '主键'
    primary key,
    alert_title     varchar(128)         null comment '标题',
    start_date      datetime             null comment '开始提醒日期',
    end_date        datetime             null comment '结束提醒日期',
    interval_type   int                  null comment '间隔类型',
    corn_expression varchar(16)          null comment 'corn表达式',
    times           int                  null comment '次数',
    run_times       int                  null comment '已执行次数',
    user_id         bigint               null comment '用户ID',
    content         varchar(1024)        null comment '提醒内容',
    creator_id      bigint               null comment '创建者ID',
    updater_id      bigint               null comment '更新者ID',
    update_time     datetime             null comment '更新时间',
    create_time     datetime             null comment '创建时间',
    deleted         tinyint(1) default 0 not null comment '逻辑删除'
    )
    comment '提醒事项';

-- 注意：bill_type 在 V1.0.0 中是 NOT NULL，V1.3.0 改为 nullable
-- 注意：order_id 在 V1.0.0 中不存在，V1.3.0 新增
create table if not exists fortune_bill
(
    bill_id          bigint auto_increment comment '主键'
    primary key,
    book_id          bigint                   not null comment '账本id',
    title            varchar(128)             null comment '标题',
    trade_time       datetime                 null comment '交易时间',
    account_id       bigint                   null comment '账户id',
    amount           decimal(20, 4)           null comment '金额',
    converted_amount decimal(20, 4)           null comment '汇率转换后的金额',
    payee_id         bigint                   null comment '交易对象',
    bill_type        tinyint                  not null comment '流水类型',
    to_account_id    tinyint                  null comment '转账到的账户',
    confirm          tinyint(1)               null comment '是否确认',
    include          tinyint(1)               null comment '是否统计',
    recycle_bin      tinyint(1)    default 0  not null comment '回收站',
    remark           varchar(1024) default '' not null comment '备注',
    creator_id       bigint                   null comment '创建者ID',
    updater_id       bigint                   null comment '更新者ID',
    update_time      datetime                 null comment '更新时间',
    create_time      datetime                 null comment '创建时间',
    deleted          tinyint(1)    default 0  not null comment '逻辑删除'
    )
    comment '账单流水表';

create table if not exists fortune_book
(
    book_id                         bigint auto_increment comment '主键'
    primary key,
    group_id                        bigint                      not null comment '所属组id',
    book_name                       varchar(128)                not null comment '账本名称',
    default_currency                varchar(16)   default 'CNY' not null comment '默认币种',
    default_expense_account_id      bigint                      null comment '默认支出账户ID',
    default_income_account_id       bigint                      null comment '默认收入账户ID',
    default_transfer_out_account_id bigint                      null comment '默认转出账户ID',
    default_transfer_in_account_id  bigint                      null comment '默认转入账户ID',
    sort                            int                         null comment '顺序',
    enable                          tinyint(1)    default 1     not null comment '是否启用',
    recycle_bin                     tinyint(1)    default 0     not null comment '是否启用',
    remark                          varchar(1024) default ''    not null comment '备注',
    creator_id                      bigint                      null comment '创建者ID',
    updater_id                      bigint                      null comment '更新者ID',
    update_time                     datetime                    null comment '更新时间',
    create_time                     datetime                    null comment '创建时间',
    deleted                         tinyint(1)    default 0     not null comment '逻辑删除'
    )
    comment '账本表';

create table if not exists fortune_category
(
    category_id   bigint auto_increment comment '主键'
    primary key,
    category_type tinyint                  not null comment '1、支出分类 2、收入分类',
    category_name varchar(128)             not null comment '分类名称',
    book_id       bigint                   not null comment '所属账本ID',
    parent_id     bigint                   not null comment '父级ID',
    sort          int                      null comment '顺序',
    enable        tinyint(1)    default 1  not null comment '是否启用',
    recycle_bin   tinyint(1)    default 0  not null comment '是否启用',
    remark        varchar(1024) default '' not null comment '备注',
    creator_id    bigint                   null comment '创建者ID',
    updater_id    bigint                   null comment '更新者ID',
    update_time   datetime                 null comment '更新时间',
    create_time   datetime                 null comment '创建时间',
    deleted       tinyint(1)    default 0  not null comment '逻辑删除'
    )
    comment '分类表';

create table if not exists fortune_category_relation
(
    category_relation_id bigint auto_increment comment '主键'
    primary key,
    category_id          bigint               not null comment '分类id',
    bill_id              bigint               not null comment '账单流水ID',
    amount               decimal(20, 4)       not null comment '金额',
    creator_id           bigint               null comment '创建者ID',
    updater_id           bigint               null comment '更新者ID',
    update_time          datetime             null comment '更新时间',
    create_time          datetime             null comment '创建时间',
    deleted              tinyint(1) default 0 not null comment '逻辑删除'
    )
    comment '交易标签和账单的关系表';

create table if not exists fortune_currency
(
    currency_id   bigint auto_increment comment '主键'
    primary key,
    currency_name varchar(128)         not null comment '货币名称',
    rate          decimal(20, 8)       not null comment '汇率',
    remark        varchar(1024)        null comment '备注',
    creator_id    bigint               null comment '创建者ID',
    updater_id    bigint               null comment '更新者ID',
    update_time   datetime             null comment '更新时间',
    create_time   datetime             null comment '创建时间',
    deleted       tinyint(1) default 0 not null comment '逻辑删除'
    )
    comment '货币表';

create table if not exists fortune_file
(
    file_id       bigint auto_increment comment '主键'
    primary key,
    bill_id       bigint               not null comment '账单流水ID',
    content_type  varchar(128)         null comment '内容类型',
    file_data     longblob             null comment '文件数据',
    size          bigint               null comment '文件大小',
    original_name varchar(255)         null comment '原始名称',
    creator_id    bigint               null comment '创建者ID',
    updater_id    bigint               null comment '更新者ID',
    update_time   datetime             null comment '更新时间',
    create_time   datetime             null comment '创建时间',
    deleted       tinyint(1) default 0 not null comment '逻辑删除'
    )
    comment '账单文件表';

create table if not exists fortune_group
(
    group_id         bigint auto_increment comment '主键'
    primary key,
    group_name       varchar(128)              not null comment '分组名称',
    default_currency varchar(16) default 'CNY' not null comment '默认币种',
    enable           tinyint(1)  default 1     not null comment '是否启用',
    default_book_id  tinyint(1)  default 0     not null comment '默认操作账本',
    remark           varchar(1024)             null comment '备注',
    creator_id       bigint                    null comment '创建者ID',
    updater_id       bigint                    null comment '更新者ID',
    update_time      datetime                  null comment '更新时间',
    create_time      datetime                  null comment '创建时间',
    deleted          tinyint(1)  default 0     not null comment '逻辑删除'
    )
    comment '分组表';

create table if not exists fortune_payee
(
    payee_id    bigint auto_increment comment '主键'
    primary key,
    book_id     bigint               not null comment '账本id',
    payee_name  varchar(128)         not null comment '交易对象名称',
    can_expense tinyint(1) default 0 not null comment '可支出',
    can_income  tinyint(1) default 0 not null comment '可收入',
    enable      tinyint(1) default 1 not null comment '是否启用',
    recycle_bin tinyint(1) default 0 not null comment '回收站',
    sort        int                  null comment '顺序',
    remark      varchar(1024)        null comment '备注',
    creator_id  bigint               null comment '创建者ID',
    updater_id  bigint               null comment '更新者ID',
    update_time datetime             null comment '更新时间',
    create_time datetime             null comment '创建时间',
    deleted     tinyint(1) default 0 not null comment '逻辑删除'
    )
    comment '交易对象表';

create table if not exists fortune_tag
(
    tag_id       bigint auto_increment comment '主键'
    primary key,
    tag_name     varchar(128)         not null comment '标签名称',
    book_id      bigint               not null comment '账本ID',
    parent_id    bigint               not null comment '父级ID',
    can_expense  tinyint(1) default 0 not null comment '可支出',
    can_income   tinyint(1) default 0 not null comment '可收入',
    can_transfer tinyint(1) default 0 not null comment '可转账',
    enable       tinyint(1) default 1 not null comment '是否启用',
    sort         int                  null comment '顺序',
    recycle_bin  tinyint(1) default 0 not null comment '回收站',
    remark       varchar(1024)        null comment '备注',
    creator_id   bigint               null comment '创建者ID',
    updater_id   bigint               null comment '更新者ID',
    update_time  datetime             null comment '更新时间',
    create_time  datetime             null comment '创建时间',
    deleted      tinyint(1) default 0 not null comment '逻辑删除'
    )
    comment '交易标签表';

create table if not exists fortune_tag_relation
(
    tag_relation_id bigint auto_increment comment '主键'
    primary key,
    bill_id         bigint               not null comment '账单流水ID',
    tag_id          bigint               not null comment '标签ID',
    creator_id      bigint               null comment '创建者ID',
    updater_id      bigint               null comment '更新者ID',
    update_time     datetime             null comment '更新时间',
    create_time     datetime             null comment '创建时间',
    deleted         tinyint(1) default 0 not null comment '逻辑删除'
    )
    comment '交易标签和账单的关系表';

create table if not exists fortune_user_group_relation
(
    user_group_relation_id bigint auto_increment comment '主键'
    primary key,
    role_type              tinyint              not null comment '1、管理员 2、协作者 3、访客',
    group_id               bigint               not null comment '分组ID',
    user_id                bigint               not null comment '用户ID',
    creator_id             bigint               null comment '创建者ID',
    default_group          tinyint(1) default 0 not null comment '默认分组',
    updater_id             bigint               null comment '更新者ID',
    update_time            datetime             null comment '更新时间',
    create_time            datetime             null comment '创建时间',
    deleted                tinyint(1) default 0 not null comment '逻辑删除'
    )
    comment '用户/分组关系表';

create table if not exists sys_config
(
    config_id       int auto_increment comment '参数主键'
        primary key,
    config_name     varchar(128)  default '' not null comment '配置名称',
    config_key      varchar(128)  default '' not null comment '配置键名',
    config_options  varchar(1024) default '' not null comment '可选的选项',
    config_value    varchar(256)  default '' not null comment '配置值',
    is_allow_change tinyint(1)               not null comment '是否允许修改',
    creator_id      bigint                   null comment '创建者ID',
    updater_id      bigint                   null comment '更新者ID',
    update_time     datetime                 null comment '更新时间',
    create_time     datetime                 null comment '创建时间',
    remark          varchar(128)             null comment '备注',
    deleted         tinyint(1)    default 0  not null comment '逻辑删除',
    unique key config_key_uniq_idx (config_key)
)
    comment '参数配置表';

create table if not exists sys_login_info
(
    info_id          bigint auto_increment comment '访问ID'
        primary key,
    username         varchar(50)   default '' not null comment '用户账号',
    ip_address       varchar(128)  default '' not null comment '登录IP地址',
    login_location   varchar(255)  default '' not null comment '登录地点',
    browser          varchar(50)   default '' not null comment '浏览器类型',
    operation_system varchar(50)   default '' not null comment '操作系统',
    status           smallint      default 0  not null comment '登录状态（1成功 0失败）',
    msg              varchar(1000) default '' not null comment '提示消息',
    login_time       datetime                 null comment '访问时间',
    deleted          tinyint(1)    default 0  not null comment '逻辑删除'
)
    comment '系统访问记录';

create table if not exists sys_menu
(
    menu_id     bigint auto_increment comment '菜单ID'
        primary key,
    menu_name   varchar(64)                not null comment '菜单名称',
    menu_type   smallint      default 0    not null comment '菜单的类型',
    router_name varchar(255)  default ''   not null comment '路由名称',
    parent_id   bigint        default 0    not null comment '父菜单ID',
    path        varchar(255)               null comment '组件路径',
    is_button   tinyint(1)    default 0    not null comment '是否按钮',
    permission  varchar(128)               null comment '权限标识',
    meta_info   varchar(1024) default '{}' not null comment '路由元信息',
    status      smallint      default 0    not null comment '菜单状态（1启用 0停用）',
    remark      varchar(256)  default ''   null comment '备注',
    creator_id  bigint                     null comment '创建者ID',
    create_time datetime                   null comment '创建时间',
    updater_id  bigint                     null comment '更新者ID',
    update_time datetime                   null comment '更新时间',
    deleted     tinyint(1)    default 0    not null comment '逻辑删除'
)
    comment '菜单权限表';

create table if not exists sys_notice
(
    notice_id      int auto_increment comment '公告ID'
        primary key,
    notice_title   varchar(64)             not null comment '公告标题',
    notice_type    smallint                not null comment '公告类型（1通知 2公告）',
    notice_content text                    null comment '公告内容',
    status         smallint     default 0  not null comment '公告状态（1正常 0关闭）',
    creator_id     bigint                  not null comment '创建者ID',
    create_time    datetime                null comment '创建时间',
    updater_id     bigint                  null comment '更新者ID',
    update_time    datetime                null comment '更新时间',
    remark         varchar(255) default '' not null comment '备注',
    deleted        tinyint(1)   default 0  not null comment '逻辑删除'
)
    comment '通知公告表';

create table if not exists sys_operation_log
(
    operation_id      bigint auto_increment comment '日志主键'
        primary key,
    business_type     smallint      default 0  not null comment '业务类型',
    request_method    smallint      default 0  not null comment '请求方式',
    request_module    varchar(64)   default '' not null comment '请求模块',
    request_url       varchar(256)  default '' not null comment '请求URL',
    called_method     varchar(128)  default '' not null comment '调用方法',
    operator_type     smallint      default 0  not null comment '操作类别',
    user_id           bigint        default 0  null comment '用户ID',
    username          varchar(32)   default '' null comment '操作人员',
    operator_ip       varchar(128)  default '' null comment '操作人员ip',
    operator_location varchar(256)  default '' null comment '操作地点',
    operation_param   varchar(2048) default '' null comment '请求参数',
    operation_result  varchar(2048) default '' null comment '返回参数',
    status            smallint      default 1  not null comment '操作状态（1正常 0异常）',
    error_stack       varchar(2048) default '' null comment '错误消息',
    operation_time    datetime                 not null comment '操作时间',
    deleted           tinyint(1)    default 0  not null comment '逻辑删除'
)
    comment '操作日志记录';

-- 注意：is_admin 列在 V1.0.0 中不存在，V1.4.0 新增
create table if not exists sys_role
(
    role_id        bigint auto_increment comment '角色ID'
        primary key,
    role_name      varchar(32)          not null comment '角色名称',
    role_key       varchar(128)         not null comment '角色权限字符串',
    role_sort      int                  not null comment '显示顺序',
    data_scope     smallint   default 1 null comment '数据范围',
    status         smallint             not null comment '角色状态（1正常 0停用）',
    allow_register tinyint(1) default 0 not null comment '允许注册',
    creator_id     bigint               null comment '创建者ID',
    create_time    datetime             null comment '创建时间',
    updater_id     bigint               null comment '更新者ID',
    update_time    datetime             null comment '更新时间',
    remark         varchar(512)         null comment '备注',
    deleted        tinyint(1) default 0 not null comment '删除标志'
)
    comment '角色信息表';

create table if not exists sys_role_menu
(
    role_id bigint not null comment '角色ID',
    menu_id bigint not null comment '菜单ID',
    primary key (role_id, menu_id)
)
    comment '角色和菜单关联表';

-- 注意：is_admin 列在 V1.0.0 中不存在，V1.4.0 新增
create table if not exists sys_user
(
    user_id      bigint auto_increment comment '用户ID'
        primary key,
    role_id      bigint                  null comment '角色id',
    username     varchar(64)             not null comment '用户账号',
    nickname     varchar(32)             not null comment '用户昵称',
    user_type    smallint     default 0  null comment '用户类型',
    email        varchar(128) default '' null comment '用户邮箱',
    phone_number varchar(18)  default '' null comment '手机号码',
    sex          smallint     default 0  null comment '用户性别',
    avatar       varchar(512) default '' null comment '头像地址',
    password     varchar(128) default '' not null comment '密码',
    status       smallint     default 0  not null comment '帐号状态',
    login_ip     varchar(128) default '' null comment '最后登录IP',
    login_date   datetime                null comment '最后登录时间',
    source       int                     not null comment '来源',
    creator_id   bigint                  null comment '创建者ID',
    create_time  datetime                null comment '创建时间',
    updater_id   bigint                  null comment '更新者ID',
    update_time  datetime                null comment '更新时间',
    remark       varchar(512)            null comment '备注',
    deleted      tinyint(1)   default 0  not null comment '删除标志'
)
    comment '用户信息表';