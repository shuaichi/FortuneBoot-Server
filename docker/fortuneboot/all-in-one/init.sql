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

create index idx_fortune_account_balance
    on fortune_account (balance);

create index idx_fortune_account_currency
    on fortune_account (currency_code);

create index idx_fortune_account_deleted
    on fortune_account (deleted);

create index idx_fortune_account_main
    on fortune_account (group_id, recycle_bin, account_type, sort);

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
    bill_type        tinyint                  null comment '流水类型 1、收入 2、支出 3、转账 4、余额调整 5、盈利 6、亏损',
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

create index idx_fortune_bill_book_amount
    on fortune_bill (book_id, amount);

create index idx_fortune_bill_book_time
    on fortune_bill (book_id, trade_time);

create index idx_fortune_bill_book_type_account_time
    on fortune_bill (book_id, bill_type, account_id, trade_time);

create index idx_fortune_bill_deleted
    on fortune_bill (deleted);

create index idx_fortune_bill_payee
    on fortune_bill (payee_id);

create index idx_fortune_bill_title
    on fortune_bill (title);

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

create index idx_fortune_book_deleted
    on fortune_book (deleted);

create index idx_fortune_book_group_recycle_enable_sort
    on fortune_book (group_id, recycle_bin, enable, sort);

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

create index idx_fortune_category_book_parent
    on fortune_category (book_id, parent_id);

create index idx_fortune_category_book_recycle_type_enable_sort
    on fortune_category (book_id, recycle_bin, category_type, enable, sort);

create index idx_fortune_category_deleted
    on fortune_category (deleted);

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

create index idx_fortune_category_relation_bill
    on fortune_category_relation (bill_id);

create index idx_fortune_category_relation_category
    on fortune_category_relation (category_id);

create index idx_fortune_category_relation_deleted
    on fortune_category_relation (deleted);

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

create index idx_fortune_file_bill
    on fortune_file (bill_id);

create index idx_fortune_file_deleted
    on fortune_file (deleted);

create table if not exists fortune_goods_keeper
(
    goods_keeper_id bigint auto_increment comment '主键'
    primary key,
    goods_name      varchar(128)         not null comment '名称',
    book_id         bigint               not null comment '账本id',
    category_id     bigint               not null comment '分类id',
    tag_id          bigint               null comment '标签id',
    price           decimal(20, 4)       not null comment '购买价格',
    purchase_date   date                 not null comment '购买日期',
    warranty_date   date                 null comment '保修日期',
    use_by_times    tinyint(1) default 0 null comment '按次使用',
    usage_num       bigint               null comment '使用次数',
    status          tinyint    default 1 not null comment '状态',
    retired_date    date                 null comment '退役日期',
    sold_price      decimal(20, 4)       null comment '出二手价格',
    remark          varchar(255)         null comment '备注',
    creator_id      bigint               null comment '创建者ID',
    updater_id      bigint               null comment '更新者ID',
    update_time     datetime             null comment '更新时间',
    create_time     datetime             null comment '创建时间',
    deleted         tinyint(1) default 0 not null comment '逻辑删除'
    )
    comment '归物表';

create index idx_fortune_goods_keeper_book
    on fortune_goods_keeper (book_id);

create index idx_fortune_goods_keeper_deleted
    on fortune_goods_keeper (deleted);

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

create index idx_fortune_payee_book_expense_income
    on fortune_payee (book_id, can_expense, can_income);

create index idx_fortune_payee_book_recycle_enable_sort
    on fortune_payee (book_id, recycle_bin, enable, sort);

create index idx_fortune_payee_deleted
    on fortune_payee (deleted);

create index idx_fortune_payee_name
    on fortune_payee (payee_name);

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

create index idx_fortune_tag_book_expense_income_transfer_enable
    on fortune_tag (book_id, can_expense, can_income, can_transfer, enable);

create index idx_fortune_tag_book_parent
    on fortune_tag (book_id, parent_id);

create index idx_fortune_tag_book_recycle_sort
    on fortune_tag (book_id, recycle_bin, sort);

create index idx_fortune_tag_deleted
    on fortune_tag (deleted);

create index idx_fortune_tag_name
    on fortune_tag (tag_name);

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

create index idx_fortune_tag_relation_bill
    on fortune_tag_relation (bill_id);

create index idx_fortune_tag_relation_deleted
    on fortune_tag_relation (deleted);

create index idx_fortune_tag_relation_tag
    on fortune_tag_relation (tag_id);

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

create index idx_fortune_user_relation_deleted
    on fortune_user_group_relation (deleted);

create index idx_fortune_user_relation_group
    on fortune_user_group_relation (group_id);

create index idx_fortune_user_relation_user
    on fortune_user_group_relation (user_id);



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
    constraint config_key_uniq_idx
    unique (config_key)
    )
    comment '参数配置表';

INSERT INTO sys_config (config_id, config_name, config_key, config_options, config_value, is_allow_change, creator_id, updater_id, update_time, create_time, remark, deleted) VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', '["skin-blue","skin-green","skin-purple","skin-red","skin-yellow"]', 'skin-blue', 1, null, null, '2022-08-28 22:12:19', '2022-05-21 08:30:55', '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow', 0);
INSERT INTO sys_config (config_id, config_name, config_key, config_options, config_value, is_allow_change, creator_id, updater_id, update_time, create_time, remark, deleted) VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '', '12345678', 1, null, 1, '2025-02-17 16:50:59', '2022-05-21 08:30:55', '初始化密码 123456', 0);
INSERT INTO sys_config (config_id, config_name, config_key, config_options, config_value, is_allow_change, creator_id, updater_id, update_time, create_time, remark, deleted) VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', '["theme-dark","theme-light"]', 'theme-dark', 1, null, null, '2022-08-28 22:12:15', '2022-08-20 08:30:55', '深色主题theme-dark，浅色主题theme-light', 0);
INSERT INTO sys_config (config_id, config_name, config_key, config_options, config_value, is_allow_change, creator_id, updater_id, update_time, create_time, remark, deleted) VALUES (4, '账号自助-验证码开关', 'sys.account.captchaOnOff', '["true","false"]', 'true', 1, null, 1, '2025-02-25 10:01:53', '2022-05-21 08:30:55', '是否开启验证码功能（true开启，false关闭）', 0);
INSERT INTO sys_config (config_id, config_name, config_key, config_options, config_value, is_allow_change, creator_id, updater_id, update_time, create_time, remark, deleted) VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', '["true","false"]', 'true', 0, null, 1, '2025-02-17 16:51:16', '2022-05-21 08:30:55', '是否开启注册用户功能（true开启，false关闭）', 0);
INSERT INTO sys_config (config_id, config_name, config_key, config_options, config_value, is_allow_change, creator_id, updater_id, update_time, create_time, remark, deleted) VALUES (6, '系统配置-ICP备案', 'sys.config.icp', '', '暂未配置ICP备案信息', 1, null, 1, '2025-05-13 22:57:42', '2025-05-13 22:57:42', 'ICP备案号', 0);
INSERT INTO sys_config (config_id, config_name, config_key, config_options, config_value, is_allow_change, creator_id, updater_id, update_time, create_time, remark, deleted) VALUES (7, '首页大屏-金额显示/隐藏设置', 'sys.config.display', '["true","false"]', 'true', 1, null, null, null, '2025-06-30 09:44:57', '首页金额默认显示/隐藏设置', 0);
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
    menu_type   smallint      default 0    not null comment '菜单的类型(1为普通菜单2为目录3为内嵌iFrame4为外链跳转)',
    router_name varchar(255)  default ''   not null comment '路由名称（需保持和前端对应的vue文件中的name保持一致defineOptions方法中设置的name）',
    parent_id   bigint        default 0    not null comment '父菜单ID',
    path        varchar(255)               null comment '组件路径（对应前端项目view文件夹中的路径）',
    is_button   tinyint(1)    default 0    not null comment '是否按钮',
    permission  varchar(128)               null comment '权限标识',
    meta_info   varchar(1024) default '{}' not null comment '路由元信息（前端根据这个信息进行逻辑处理）',
    status      smallint      default 0    not null comment '菜单状态（1启用 0停用）',
    remark      varchar(256)  default ''   null comment '备注',
    creator_id  bigint                     null comment '创建者ID',
    create_time datetime                   null comment '创建时间',
    updater_id  bigint                     null comment '更新者ID',
    update_time datetime                   null comment '更新时间',
    deleted     tinyint(1)    default 0    not null comment '逻辑删除'
    )
    comment '菜单权限表';

INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (1, '系统管理', 2, '', 0, '/system', 0, '', '{"title":"系统管理","icon":"ep:management","showLink":true,"showParent":true,"rank":3}', 1, '系统管理目录', 0, '2022-05-21 08:30:54', 1, '2025-02-16 22:42:29', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (2, '系统监控', 2, '', 0, '/monitor', 0, '', '{"title":"系统监控","icon":"ep:monitor","showLink":true,"showParent":true,"rank":5}', 1, '系统监控目录', 0, '2022-05-21 08:30:54', 1, '2025-02-16 22:42:22', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (3, '系统工具', 2, '', 0, '/tool', 0, '', '{"title":"系统工具","icon":"ep:tools","showLink":true,"showParent":true,"rank":4}', 1, '系统工具目录', 0, '2022-05-21 08:30:54', 1, '2025-02-16 22:42:26', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (5, '用户管理', 1, 'SystemUser', 1, '/system/user/index', 0, 'system:user:list', '{"title":"用户管理","icon":"ep:user-filled","showParent":true}', 1, '用户管理菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:16:13', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (6, '角色管理', 1, 'SystemRole', 1, '/system/role/index', 0, 'system:role:list', '{"title":"角色管理","icon":"ep:user","showParent":true}', 1, '角色管理菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:16:23', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (7, '菜单管理', 1, 'MenuManagement', 1, '/system/menu/index', 0, 'system:menu:list', '{"title":"菜单管理","icon":"ep:menu","showParent":true}', 1, '菜单管理菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:15:41', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (8, '官方网站', 4, 'https://www.fortuneboot.com', 0, '/external', 0, '', '{"title":"官方网站","icon":"fa:home","showLink":true,"showParent":true,"rank":1}', 1, '', 1, '2025-04-27 17:14:53', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (10, '参数设置', 1, 'Config', 1, '/system/config/index', 0, 'system:config:list', '{"title":"参数设置","icon":"ep:setting","showParent":true}', 1, '参数设置菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:15:03', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (11, '通知公告', 1, 'SystemNotice', 1, '/system/notice/index', 0, 'system:notice:list', '{"title":"通知公告","icon":"ep:notification","showParent":true}', 1, '通知公告菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:14:56', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (12, '日志管理', 1, 'LogManagement', 1, '/system/logd', 0, '', '{"title":"日志管理","icon":"ep:document","showParent":true}', 1, '日志管理菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:14:47', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (13, '在线用户', 1, 'OnlineUser', 2, '/system/monitor/onlineUser/index', 0, 'monitor:online:list', '{"title":"在线用户","icon":"fa-solid:users","showParent":true}', 1, '在线用户菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:13:13', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (14, '数据监控', 1, 'DataMonitor', 2, '/system/monitor/druid/index', 0, 'monitor:druid:list', '{"title":"数据监控","icon":"fa:database","showParent":true,"frameSrc":"/druid/login.html","isFrameSrcInternal":true}', 1, '数据监控菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:13:25', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (15, '服务监控', 1, 'ServerInfo', 2, '/system/monitor/server/index', 0, 'monitor:server:list', '{"title":"服务监控","icon":"fa:server","showParent":true}', 1, '服务监控菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:13:34', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (16, '缓存监控', 1, 'CacheInfo', 2, '/system/monitor/cache/index', 0, 'monitor:cache:list', '{"title":"缓存监控","icon":"ep:reading","showParent":true}', 1, '缓存监控菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:12:59', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (17, '系统接口', 1, 'SystemAPI', 3, '/tool/swagger/index', 0, 'tool:swagger:list', '{"title":"系统接口","icon":"ep:document-remove","showParent":true,"frameSrc":"/swagger-ui/index.html","isFrameSrcInternal":true}', 1, '系统接口菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:14:01', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (18, '操作日志', 1, 'OperationLog', 12, '/system/log/operationLog/index', 0, 'monitor:operlog:list', '{"title":"操作日志"}', 1, '操作日志菜单', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (19, '登录日志', 1, 'LoginLog', 12, '/system/log/loginLog/index', 0, 'monitor:logininfor:list', '{"title":"登录日志"}', 1, '登录日志菜单', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (20, '用户查询', 0, ' ', 5, '', 1, 'system:user:query', '{"title":"用户查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (21, '用户新增', 0, ' ', 5, '', 1, 'system:user:add', '{"title":"用户新增"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (22, '用户修改', 0, ' ', 5, '', 1, 'system:user:edit', '{"title":"用户修改"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (23, '用户删除', 0, ' ', 5, '', 1, 'system:user:remove', '{"title":"用户删除"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (24, '用户导出', 0, ' ', 5, '', 1, 'system:user:export', '{"title":"用户导出"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (25, '用户导入', 0, ' ', 5, '', 1, 'system:user:import', '{"title":"用户导入"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (26, '重置密码', 0, ' ', 5, '', 1, 'system:user:resetPwd', '{"title":"重置密码"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (27, '角色查询', 0, ' ', 6, '', 1, 'system:role:query', '{"title":"角色查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (28, '角色新增', 0, ' ', 6, '', 1, 'system:role:add', '{"title":"角色新增"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (29, '角色修改', 0, ' ', 6, '', 1, 'system:role:edit', '{"title":"角色修改"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (30, '角色删除', 0, ' ', 6, '', 1, 'system:role:remove', '{"title":"角色删除"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (31, '角色导出', 0, ' ', 6, '', 1, 'system:role:export', '{"title":"角色导出"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (32, '菜单查询', 0, ' ', 7, '', 1, 'system:menu:query', '{"title":"菜单查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (33, '菜单新增', 0, ' ', 7, '', 1, 'system:menu:add', '{"title":"菜单新增"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (34, '菜单修改', 0, ' ', 7, '', 1, 'system:menu:edit', '{"title":"菜单修改"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (35, '菜单删除', 0, ' ', 7, '', 1, 'system:menu:remove', '{"title":"菜单删除"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (45, '参数查询', 0, ' ', 10, '', 1, 'system:config:query', '{"title":"参数查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (46, '参数新增', 0, ' ', 10, '', 1, 'system:config:add', '{"title":"参数新增"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (47, '参数修改', 0, ' ', 10, '', 1, 'system:config:edit', '{"title":"参数修改"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (48, '参数删除', 0, ' ', 10, '', 1, 'system:config:remove', '{"title":"参数删除"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (49, '参数导出', 0, ' ', 10, '', 1, 'system:config:export', '{"title":"参数导出"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (50, '公告查询', 0, ' ', 11, '', 1, 'system:notice:query', '{"title":"公告查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (51, '公告新增', 0, ' ', 11, '', 1, 'system:notice:add', '{"title":"公告新增"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (52, '公告修改', 0, ' ', 11, '', 1, 'system:notice:edit', '{"title":"公告修改"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (53, '公告删除', 0, ' ', 11, '', 1, 'system:notice:remove', '{"title":"公告删除"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (54, '操作查询', 0, ' ', 18, '', 1, 'monitor:operlog:query', '{"title":"操作查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (55, '操作删除', 0, ' ', 18, '', 1, 'monitor:operlog:remove', '{"title":"操作删除"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (56, '日志导出', 0, ' ', 18, '', 1, 'monitor:operlog:export', '{"title":"日志导出"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (57, '登录查询', 0, ' ', 19, '', 1, 'monitor:logininfor:query', '{"title":"登录查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (58, '登录删除', 0, ' ', 19, '', 1, 'monitor:logininfor:remove', '{"title":"登录删除"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (59, '日志导出', 0, ' ', 19, '', 1, 'monitor:logininfor:export', '{"title":"日志导出","rank":22}', 1, '', 0, '2022-05-21 08:30:54', 1, '2023-07-22 17:02:28', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (60, '在线查询', 0, ' ', 13, '', 1, 'monitor:online:query', '{"title":"在线查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (61, '批量强退', 0, ' ', 13, '', 1, 'monitor:online:batchLogout', '{"title":"批量强退"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (62, '单条强退', 0, ' ', 13, '', 1, 'monitor:online:forceLogout', '{"title":"单条强退"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (66, '记账管理', 2, '', 0, '/fortune', 0, '', '{"title":"记账管理","icon":"fa:address-book","showLink":true,"showParent":true,"rank":6}', 1, '', 1, '2025-02-06 21:56:54', 1, '2025-02-22 17:28:55', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (67, '分组管理', 1, 'FortuneGroup', 66, '/fortune/group/index', 0, '', '{"title":"分组管理","icon":"fa:group","showLink":true,"showParent":true,"rank":1}', 1, '', 1, '2025-02-06 21:49:50', 1, '2025-04-12 19:47:40', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (68, '账本管理', 1, 'FortuneBook', 66, '/fortune/book/index', 0, '', '{"title":"账本管理","icon":"fa:book","showLink":true,"showParent":true,"rank":2}', 1, '', 1, '2025-02-06 21:00:26', 1, '2025-04-12 19:47:46', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (81, '账户管理', 1, 'FortuneAccount', 66, '/fortune/account/index', 0, '', '{"title":"账户管理","icon":"fa:credit-card","showLink":true,"showParent":true,"rank":3}', 1, '', 1, '2025-02-07 15:46:23', 1, '2025-04-12 19:47:52', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (82, '账单管理', 1, 'FortuneBill', 66, '/fortune/bill/index', 0, '', '{"title":"账单管理","icon":"fa-solid:money-bill-alt","showLink":true,"showParent":true,"rank":4}', 1, '', 1, '2025-02-08 23:14:36', 1, '2025-04-12 19:47:58', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (83, '交易对象', 1, 'FortuneBookPayee', 84, '/fortune/payee/index', 0, '', '{"title":"交易对象","showLink":false,"showParent":true}', 1, '', 1, '2025-02-13 16:04:57', 1, '2025-02-22 17:09:26', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (84, '账本配置', 2, '', 66, '/fortune/book/config', 0, '', '{"title":"账本配置","showLink":true,"showParent":true,"rank":5}', 1, '', 1, '2025-02-13 16:06:27', 1, '2025-04-12 19:48:06', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (85, '标签管理', 1, 'FortuneBookTag', 84, '/fortune/tag/index', 0, '', '{"title":"标签管理","showLink":false,"showParent":true}', 1, '', 1, '2025-02-13 20:22:41', 1, '2025-02-22 17:09:22', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (86, '分类管理', 1, 'FortuneBookCategory', 84, '/fortune/category/index', 0, '', '{"title":"分类管理","showLink":false,"showParent":true}', 1, '', 1, '2025-02-15 02:41:14', 1, '2025-02-22 17:09:19', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (87, '回收站', 1, 'FortuneRecycleBin', 66, '/fortune/recycle-bin/index', 0, '', '{"title":"回收站","icon":"fa:recycle","showLink":true,"showParent":true,"rank":7}', 1, '', 1, '2025-02-16 16:50:46', 1, '2025-04-12 19:49:38', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (88, '归物', 1, 'FortuneGoodsKeeper', 66, '/fortune/goods-keeper/index', 0, '', '{"title":"归物","icon":"fa:dropbox","showLink":true,"showParent":true,"rank":6}', 1, '', 1, '2025-05-06 16:50:46', 1, '2025-05-06 19:49:38', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (89, '报表中心', 2, '', 0, '/report', 0, '', '{"title":"报表中心","icon":"fa:pie-chart","showLink":true,"showParent":true,"rank":7}', 1, '', 1, '2025-02-22 23:30:10', 1, '2025-02-22 23:30:52', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (91, '支出分类', 1, 'FortuneCategoryExpense', 89, '/fortune/report/category/expense', 0, '', '{"title":"支出分类","icon":"fa:bookmark","showLink":true,"showParent":true,"rank":1}', 1, '', 1, '2025-03-05 21:03:59', 1, '2025-03-06 16:40:42', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (92, '收入分类', 1, 'FortuneCategoryIncome', 89, '/fortune/report/category/income', 0, '', '{"title":"收入分类","icon":"fa:bookmark-o","showLink":true,"showParent":true,"rank":2}', 1, '', 1, '2025-03-06 17:50:41', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (93, '支出标签', 1, 'FortuneTagExpense', 89, '/fortune/report/tag/expense', 0, '', '{"title":"支出标签","icon":"fa:calendar-minus-o","showLink":true,"showParent":true,"rank":3}', 1, '', 1, '2025-03-06 17:57:55', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (94, '收入标签', 1, 'FortuneTagIncome', 89, '/fortune/report/tag/income', 0, '', '{"title":"收入标签","icon":"fa:calendar-plus-o","showLink":true,"showParent":true,"rank":4}', 1, '', 1, '2025-03-06 17:58:45', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (95, '支出对象', 1, 'FortunePayeeExpense', 89, '/fortune/report/payee/expense', 0, '', '{"title":"支出对象","icon":"fa:paperclip","showLink":true,"showParent":true,"rank":5}', 1, '', 1, '2025-03-06 18:14:29', null, null, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (96, '收入对象', 1, 'FortunePayeeIncome', 89, '/fortune/report/payee/income', 0, '', '{"title":"收入对象","icon":"ep:paperclip","showParent":true,"rank":6}', 1, '', 1, '2025-03-06 18:33:49', 1, '2025-03-06 18:59:03', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (97, '汇率中心', 1, 'FortuneCurrency', 66, '/fortune/currency/index', 0, '', '{"title":"汇率中心","icon":"fa:dollar","showLink":true,"showParent":true,"rank":7}', 1, '', 1, '2025-04-12 19:47:04', 1, '2025-04-12 19:50:31', 0);

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
    business_type     smallint      default 0  not null comment '业务类型（0其它 1新增 2修改 3删除）',
    request_method    smallint      default 0  not null comment '请求方式',
    request_module    varchar(64)   default '' not null comment '请求模块',
    request_url       varchar(256)  default '' not null comment '请求URL',
    called_method     varchar(128)  default '' not null comment '调用方法',
    operator_type     smallint      default 0  not null comment '操作类别（0其它 1后台用户 2手机端用户）',
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

create table if not exists sys_role
(
    role_id        bigint auto_increment comment '角色ID'
    primary key,
    role_name      varchar(32)          not null comment '角色名称',
    role_key       varchar(128)         not null comment '角色权限字符串',
    role_sort      int                  not null comment '显示顺序',
    data_scope     smallint   default 1 null comment '数据范围（1：全部数据权限 2：自定数据权限 3: 本部门数据权限 4: 本部门及以下数据权限 5: 本人权限）',
    status         smallint             not null comment '角色状态（1正常 0停用）',
    allow_register tinyint(1) default 0 not null comment '允许注册',
    creator_id     bigint               null comment '创建者ID',
    create_time    datetime             null comment '创建时间',
    updater_id     bigint               null comment '更新者ID',
    update_time    datetime             null comment '更新时间',
    remark         varchar(512)         null comment '备注',
    deleted        tinyint(1) default 0 not null comment '删除标志（0代表存在 1代表删除）'
    )
    comment '角色信息表';

INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, allow_register, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (1, '超级管理员', 'admin', 1, 1, 1, 0, null, '2022-05-21 08:30:54', null, null, '超级管理员', 0);
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, allow_register, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (2, '普通角色', 'common', 3, 5, 1, 1, null, '2022-05-21 08:30:54', 7, '2025-03-06 18:37:48', '普通角色', 0);
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, allow_register, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (3, '闲置角色', 'unused', 4, 5, 0, 0, null, '2022-05-21 08:30:54', 1, '2025-02-22 17:11:36', '未使用的角色', 0);

create table if not exists sys_role_menu
(
    role_id bigint not null comment '角色ID',
    menu_id bigint not null comment '菜单ID',
    primary key (role_id, menu_id)
    )
    comment '角色和菜单关联表';

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 66);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 67);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 68);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 81);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 82);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 83);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 84);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 85);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 86);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 87);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 88);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 89);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 91);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 92);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 93);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 94);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 95);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 96);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 97);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (111, 1);

create table if not exists sys_user
(
    user_id      bigint auto_increment comment '用户ID'
    primary key,
    role_id      bigint                  null comment '角色id',
    username     varchar(64)             not null comment '用户账号',
    nickname     varchar(32)             not null comment '用户昵称',
    user_type    smallint     default 0  null comment '用户类型（00系统用户）',
    email        varchar(128) default '' null comment '用户邮箱',
    phone_number varchar(18)  default '' null comment '手机号码',
    sex          smallint     default 0  null comment '用户性别（0男 1女 2未知）',
    avatar       varchar(512) default '' null comment '头像地址',
    password     varchar(128) default '' not null comment '密码',
    status       smallint     default 0  not null comment '帐号状态（1正常 2停用 3冻结）',
    login_ip     varchar(128) default '' null comment '最后登录IP',
    login_date   datetime                null comment '最后登录时间',
    is_admin     tinyint(1)   default 0  not null comment '超级管理员标志（1是，0否）',
    source       int                     not null comment '来源',
    creator_id   bigint                  null comment '更新者ID',
    create_time  datetime                null comment '创建时间',
    updater_id   bigint                  null comment '更新者ID',
    update_time  datetime                null comment '更新时间',
    remark       varchar(512)            null comment '备注',
    deleted      tinyint(1)   default 0  not null comment '删除标志（0代表存在 1代表删除）'
    )
    comment '用户信息表';

INSERT INTO sys_user (user_id, role_id, username, nickname, user_type, email, phone_number, sex, avatar, password, status, login_ip, login_date, is_admin, source, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (1, 1, 'admin', 'fortune', 0, 'fortuneboot1@gmail.com', '15888888883', 0, '/profile/avatar/20230725164110_blob_6b7a989b1cdd4dd396665d2cfd2addc5.png', '$2a$10$o55UFZAtyWnDpRV6dvQe8.c/MjlFacC49ASj2usNXm9BY74SYI/uG', 1, '0:0:0:0:0:0:0:1', '2025-03-06 20:17:33', 1, 0, null, '2022-05-21 08:30:54', 1, '2025-03-06 20:17:33', '管理员', 0);
