-- FortuneBoot V1.0.0 SQLite 全量建表脚本

create table if not exists fortune_account
(
    account_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    card_no          TEXT,
    account_name     TEXT,
    balance          DECIMAL(20, 4) DEFAULT 0.0000 NOT NULL,
    bill_day         TEXT,
    repay_day        TEXT,
    can_expense      INTEGER DEFAULT 0,
    can_income       INTEGER DEFAULT 0,
    can_transfer_out INTEGER DEFAULT 0,
    can_transfer_in  INTEGER DEFAULT 0,
    credit_limit     DECIMAL,
    currency_code    TEXT DEFAULT 'CNY' NOT NULL,
    enable           INTEGER DEFAULT 1 NOT NULL,
    include          INTEGER DEFAULT 1 NOT NULL,
    apr              DECIMAL(8, 4),
    initial_balance  DECIMAL(20, 4) DEFAULT 0.0000,
    account_type     INTEGER NOT NULL,
    group_id         INTEGER NOT NULL,
    sort             INTEGER,
    recycle_bin      INTEGER DEFAULT 0 NOT NULL,
    remark           TEXT DEFAULT '' NOT NULL,
    creator_id       INTEGER,
    updater_id       INTEGER,
    update_time      TEXT,
    create_time      TEXT,
    deleted          INTEGER DEFAULT 0 NOT NULL
);

CREATE INDEX idx_fortune_account_balance ON fortune_account (balance);
CREATE INDEX idx_fortune_account_currency ON fortune_account (currency_code);
CREATE INDEX idx_fortune_account_deleted ON fortune_account (deleted);
CREATE INDEX idx_fortune_account_main ON fortune_account (group_id, recycle_bin, account_type, sort);

create table if not exists fortune_alert
(
    alert_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    alert_title     TEXT,
    start_date      TEXT,
    end_date        TEXT,
    interval_type   INTEGER,
    corn_expression TEXT,
    times           INTEGER,
    run_times       INTEGER,
    user_id         INTEGER,
    content         TEXT,
    creator_id      INTEGER,
    updater_id      INTEGER,
    update_time     TEXT,
    create_time     TEXT,
    deleted         INTEGER DEFAULT 0 NOT NULL
);

create table if not exists fortune_bill
(
    bill_id          INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id          INTEGER NOT NULL,
    title            TEXT,
    trade_time       TEXT,
    account_id       INTEGER,
    amount           DECIMAL(20, 4),
    converted_amount DECIMAL(20, 4),
    order_id         INTEGER,
    payee_id         INTEGER,
    bill_type        INTEGER,
    to_account_id    INTEGER,
    confirm          INTEGER,
    include          INTEGER,
    recycle_bin      INTEGER DEFAULT 0 NOT NULL,
    remark           TEXT DEFAULT '' NOT NULL,
    creator_id       INTEGER,
    updater_id       INTEGER,
    update_time      TEXT,
    create_time      TEXT,
    deleted          INTEGER DEFAULT 0 NOT NULL
);

CREATE INDEX idx_fortune_bill_book_amount ON fortune_bill (book_id, amount);
CREATE INDEX idx_fortune_bill_book_time ON fortune_bill (book_id, trade_time);
CREATE INDEX idx_fortune_bill_book_type_account_time ON fortune_bill (book_id, bill_type, account_id, trade_time);
CREATE INDEX idx_fortune_bill_deleted ON fortune_bill (deleted);
CREATE INDEX idx_fortune_bill_payee ON fortune_bill (payee_id);
CREATE INDEX idx_fortune_bill_title ON fortune_bill (title);

create table if not exists fortune_book
(
    book_id                         INTEGER PRIMARY KEY AUTOINCREMENT,
    group_id                        INTEGER NOT NULL,
    book_name                       TEXT NOT NULL,
    default_currency                TEXT DEFAULT 'CNY' NOT NULL,
    default_expense_account_id      INTEGER,
    default_income_account_id       INTEGER,
    default_transfer_out_account_id INTEGER,
    default_transfer_in_account_id  INTEGER,
    sort                            INTEGER,
    enable                          INTEGER DEFAULT 1 NOT NULL,
    recycle_bin                     INTEGER DEFAULT 0 NOT NULL,
    remark                          TEXT DEFAULT '' NOT NULL,
    creator_id                      INTEGER,
    updater_id                      INTEGER,
    update_time                     TEXT,
    create_time                     TEXT,
    deleted                         INTEGER DEFAULT 0 NOT NULL
);

CREATE INDEX idx_fortune_book_deleted ON fortune_book (deleted);
CREATE INDEX idx_fortune_book_group_recycle_enable_sort ON fortune_book (group_id, recycle_bin, enable, sort);

create table if not exists fortune_category
(
    category_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    category_type INTEGER NOT NULL,
    category_name TEXT NOT NULL,
    book_id       INTEGER NOT NULL,
    parent_id     INTEGER NOT NULL,
    sort          INTEGER,
    enable        INTEGER DEFAULT 1 NOT NULL,
    recycle_bin   INTEGER DEFAULT 0 NOT NULL,
    remark        TEXT DEFAULT '' NOT NULL,
    creator_id    INTEGER,
    updater_id    INTEGER,
    update_time   TEXT,
    create_time   TEXT,
    deleted       INTEGER DEFAULT 0 NOT NULL
);

CREATE INDEX idx_fortune_category_book_parent ON fortune_category (book_id, parent_id);
CREATE INDEX idx_fortune_category_book_recycle_type_enable_sort ON fortune_category (book_id, recycle_bin, category_type, enable, sort);
CREATE INDEX idx_fortune_category_deleted ON fortune_category (deleted);

create table if not exists fortune_category_relation
(
    category_relation_id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_id          INTEGER NOT NULL,
    bill_id              INTEGER NOT NULL,
    amount               DECIMAL(20, 4) NOT NULL,
    creator_id           INTEGER,
    updater_id           INTEGER,
    update_time          TEXT,
    create_time          TEXT,
    deleted              INTEGER DEFAULT 0 NOT NULL
);

CREATE INDEX idx_fortune_category_relation_bill ON fortune_category_relation (bill_id);
CREATE INDEX idx_fortune_category_relation_category ON fortune_category_relation (category_id);
CREATE INDEX idx_fortune_category_relation_deleted ON fortune_category_relation (deleted);

create table if not exists fortune_currency
(
    currency_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    currency_name TEXT NOT NULL,
    rate          DECIMAL(20, 8) NOT NULL,
    remark        TEXT,
    creator_id    INTEGER,
    updater_id    INTEGER,
    update_time   TEXT,
    create_time   TEXT,
    deleted       INTEGER DEFAULT 0 NOT NULL
);

create table if not exists fortune_file
(
    file_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    bill_id       INTEGER NOT NULL,
    content_type  TEXT,
    file_data     BLOB,
    size          INTEGER,
    original_name TEXT,
    creator_id    INTEGER,
    updater_id    INTEGER,
    update_time   TEXT,
    create_time   TEXT,
    deleted       INTEGER DEFAULT 0 NOT NULL
);

CREATE INDEX idx_fortune_file_bill ON fortune_file (bill_id);
CREATE INDEX idx_fortune_file_deleted ON fortune_file (deleted);

create table if not exists fortune_finance_order
(
    order_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id     INTEGER NOT NULL,
    title       TEXT NOT NULL,
    type        INTEGER NOT NULL,
    out_amount  DECIMAL(20, 4) DEFAULT 0.0000 NOT NULL,
    in_amount   DECIMAL(20, 4) DEFAULT 0.0000,
    status      INTEGER NOT NULL,
    submit_time TEXT,
    close_time  TEXT,
    remark      TEXT DEFAULT '' NOT NULL,
    creator_id  INTEGER,
    updater_id  INTEGER,
    update_time TEXT,
    create_time TEXT,
    deleted     INTEGER DEFAULT 0 NOT NULL
);

CREATE INDEX idx_fortune_finance_order_book ON fortune_finance_order (book_id);

create table if not exists fortune_goods_keeper
(
    goods_keeper_id INTEGER PRIMARY KEY AUTOINCREMENT,
    goods_name      TEXT NOT NULL,
    book_id         INTEGER NOT NULL,
    category_id     INTEGER NOT NULL,
    tag_id          INTEGER,
    price           DECIMAL(20, 4) NOT NULL,
    purchase_date   TEXT NOT NULL,
    warranty_date   TEXT,
    use_by_times    INTEGER DEFAULT 0,
    usage_num       INTEGER,
    status          INTEGER DEFAULT 1 NOT NULL,
    retired_date    TEXT,
    sold_price      DECIMAL(20, 4),
    remark          TEXT,
    creator_id      INTEGER,
    updater_id      INTEGER,
    update_time     TEXT,
    create_time     TEXT,
    deleted         INTEGER DEFAULT 0 NOT NULL
);

CREATE INDEX idx_fortune_goods_keeper_book ON fortune_goods_keeper (book_id);
CREATE INDEX idx_fortune_goods_keeper_deleted ON fortune_goods_keeper (deleted);

create table if not exists fortune_group
(
    group_id         INTEGER PRIMARY KEY AUTOINCREMENT,
    group_name       TEXT NOT NULL,
    default_currency TEXT DEFAULT 'CNY' NOT NULL,
    enable           INTEGER DEFAULT 1 NOT NULL,
    default_book_id  INTEGER DEFAULT 0 NOT NULL,
    remark           TEXT,
    creator_id       INTEGER,
    updater_id       INTEGER,
    update_time      TEXT,
    create_time      TEXT,
    deleted          INTEGER DEFAULT 0 NOT NULL
);

create table if not exists fortune_payee
(
    payee_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id     INTEGER NOT NULL,
    payee_name  TEXT NOT NULL,
    can_expense INTEGER DEFAULT 0 NOT NULL,
    can_income  INTEGER DEFAULT 0 NOT NULL,
    enable      INTEGER DEFAULT 1 NOT NULL,
    recycle_bin INTEGER DEFAULT 0 NOT NULL,
    sort        INTEGER,
    remark      TEXT,
    creator_id  INTEGER,
    updater_id  INTEGER,
    update_time TEXT,
    create_time TEXT,
    deleted     INTEGER DEFAULT 0 NOT NULL
);

CREATE INDEX idx_fortune_payee_book_expense_income ON fortune_payee (book_id, can_expense, can_income);
CREATE INDEX idx_fortune_payee_book_recycle_enable_sort ON fortune_payee (book_id, recycle_bin, enable, sort);
CREATE INDEX idx_fortune_payee_deleted ON fortune_payee (deleted);
CREATE INDEX idx_fortune_payee_name ON fortune_payee (payee_name);

create table if not exists fortune_recurring_bill_log
(
    log_id             INTEGER PRIMARY KEY AUTOINCREMENT,
    rule_id            INTEGER NOT NULL,
    execution_time     TEXT NOT NULL,
    status             INTEGER NOT NULL,
    bill_id            INTEGER NOT NULL,
    error_msg          TEXT DEFAULT '' NOT NULL,
    execution_duration INTEGER DEFAULT 0 NOT NULL,
    creator_id         INTEGER,
    updater_id         INTEGER,
    update_time        TEXT,
    create_time        TEXT,
    deleted            INTEGER DEFAULT 0 NOT NULL
);

CREATE INDEX idx_execution_time ON fortune_recurring_bill_log (execution_time);
CREATE INDEX idx_rule_id ON fortune_recurring_bill_log (rule_id);
CREATE INDEX idx_status ON fortune_recurring_bill_log (status);

create table if not exists fortune_recurring_bill_rule
(
    rule_id             INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id             INTEGER NOT NULL,
    rule_name           TEXT NOT NULL,
    cron_expression     TEXT NOT NULL,
    enable              INTEGER DEFAULT 1 NOT NULL,
    bill_request        TEXT NOT NULL,
    start_date          TEXT,
    end_date            TEXT,
    max_executions      INTEGER,
    executed_count      INTEGER DEFAULT 0,
    last_executed_time  TEXT,
    next_execution_time TEXT,
    last_recovery_check TEXT,
    recovery_strategy   INTEGER,
    max_recovery_count  INTEGER,
    remark              TEXT DEFAULT '' NOT NULL,
    creator_id          INTEGER,
    updater_id          INTEGER,
    update_time         TEXT,
    create_time         TEXT,
    deleted             INTEGER DEFAULT 0 NOT NULL
);

CREATE INDEX idx_book_id ON fortune_recurring_bill_rule (book_id);

create table if not exists fortune_tag
(
    tag_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    tag_name     TEXT NOT NULL,
    book_id      INTEGER NOT NULL,
    parent_id    INTEGER NOT NULL,
    can_expense  INTEGER DEFAULT 0 NOT NULL,
    can_income   INTEGER DEFAULT 0 NOT NULL,
    can_transfer INTEGER DEFAULT 0 NOT NULL,
    enable       INTEGER DEFAULT 1 NOT NULL,
    sort         INTEGER,
    recycle_bin  INTEGER DEFAULT 0 NOT NULL,
    remark       TEXT,
    creator_id   INTEGER,
    updater_id   INTEGER,
    update_time  TEXT,
    create_time  TEXT,
    deleted      INTEGER DEFAULT 0 NOT NULL
);

CREATE INDEX idx_fortune_tag_book_expense_income_transfer_enable ON fortune_tag (book_id, can_expense, can_income, can_transfer, enable);
CREATE INDEX idx_fortune_tag_book_parent ON fortune_tag (book_id, parent_id);
CREATE INDEX idx_fortune_tag_book_recycle_sort ON fortune_tag (book_id, recycle_bin, sort);
CREATE INDEX idx_fortune_tag_deleted ON fortune_tag (deleted);
CREATE INDEX idx_fortune_tag_name ON fortune_tag (tag_name);

create table if not exists fortune_tag_relation
(
    tag_relation_id INTEGER PRIMARY KEY AUTOINCREMENT,
    bill_id         INTEGER NOT NULL,
    tag_id          INTEGER NOT NULL,
    creator_id      INTEGER,
    updater_id      INTEGER,
    update_time     TEXT,
    create_time     TEXT,
    deleted         INTEGER DEFAULT 0 NOT NULL
);

CREATE INDEX idx_fortune_tag_relation_bill ON fortune_tag_relation (bill_id);
CREATE INDEX idx_fortune_tag_relation_deleted ON fortune_tag_relation (deleted);
CREATE INDEX idx_fortune_tag_relation_tag ON fortune_tag_relation (tag_id);

create table if not exists fortune_user_group_relation
(
    user_group_relation_id INTEGER PRIMARY KEY AUTOINCREMENT,
    role_type              INTEGER NOT NULL,
    group_id               INTEGER NOT NULL,
    user_id                INTEGER NOT NULL,
    creator_id             INTEGER,
    default_group          INTEGER DEFAULT 0 NOT NULL,
    updater_id             INTEGER,
    update_time            TEXT,
    create_time            TEXT,
    deleted                INTEGER DEFAULT 0 NOT NULL
);

CREATE INDEX idx_fortune_user_relation_deleted ON fortune_user_group_relation (deleted);
CREATE INDEX idx_fortune_user_relation_group ON fortune_user_group_relation (group_id);
CREATE INDEX idx_fortune_user_relation_user ON fortune_user_group_relation (user_id);

create table if not exists sys_config
(
    config_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    config_name     TEXT DEFAULT '' NOT NULL,
    config_key      TEXT DEFAULT '' NOT NULL,
    config_options  TEXT DEFAULT '' NOT NULL,
    config_value    TEXT DEFAULT '' NOT NULL,
    is_allow_change INTEGER NOT NULL,
    creator_id      INTEGER,
    updater_id      INTEGER,
    update_time     TEXT,
    create_time     TEXT,
    remark          TEXT,
    deleted         INTEGER DEFAULT 0 NOT NULL
);

CREATE INDEX config_key_idx ON sys_config (config_key);

create table if not exists sys_login_info
(
    info_id          INTEGER PRIMARY KEY AUTOINCREMENT,
    username         TEXT DEFAULT '' NOT NULL,
    ip_address       TEXT DEFAULT '' NOT NULL,
    login_location   TEXT DEFAULT '' NOT NULL,
    browser          TEXT DEFAULT '' NOT NULL,
    operation_system TEXT DEFAULT '' NOT NULL,
    status           INTEGER DEFAULT 0 NOT NULL,
    msg              TEXT DEFAULT '' NOT NULL,
    login_time       TEXT,
    deleted          INTEGER DEFAULT 0 NOT NULL
);

create table if not exists sys_menu
(
    menu_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    menu_name   TEXT NOT NULL,
    menu_type   INTEGER DEFAULT 0 NOT NULL,
    router_name TEXT DEFAULT '' NOT NULL,
    parent_id   INTEGER DEFAULT 0 NOT NULL,
    path        TEXT,
    is_button   INTEGER DEFAULT 0 NOT NULL,
    permission  TEXT,
    meta_info   TEXT DEFAULT '{}' NOT NULL,
    status      INTEGER DEFAULT 0 NOT NULL,
    remark      TEXT DEFAULT '',
    creator_id  INTEGER,
    create_time TEXT,
    updater_id  INTEGER,
    update_time TEXT,
    deleted     INTEGER DEFAULT 0 NOT NULL
);

create table if not exists sys_notice
(
    notice_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    notice_title   TEXT NOT NULL,
    notice_type    INTEGER NOT NULL,
    notice_content TEXT,
    status         INTEGER DEFAULT 0 NOT NULL,
    creator_id     INTEGER NOT NULL,
    create_time    TEXT,
    updater_id     INTEGER,
    update_time    TEXT,
    remark         TEXT DEFAULT '' NOT NULL,
    deleted        INTEGER DEFAULT 0 NOT NULL
);

create table if not exists sys_operation_log
(
    operation_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    business_type     INTEGER DEFAULT 0 NOT NULL,
    request_method    INTEGER DEFAULT 0 NOT NULL,
    request_module    TEXT DEFAULT '' NOT NULL,
    request_url       TEXT DEFAULT '' NOT NULL,
    called_method     TEXT DEFAULT '' NOT NULL,
    operator_type     INTEGER DEFAULT 0 NOT NULL,
    user_id           INTEGER DEFAULT 0,
    username          TEXT DEFAULT '',
    operator_ip       TEXT DEFAULT '',
    operator_location TEXT DEFAULT '',
    operation_param   TEXT DEFAULT '',
    operation_result  TEXT DEFAULT '',
    status            INTEGER DEFAULT 1 NOT NULL,
    error_stack       TEXT DEFAULT '',
    operation_time    TEXT NOT NULL,
    deleted           INTEGER DEFAULT 0 NOT NULL
);

create table if not exists sys_role
(
    role_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    role_name      TEXT NOT NULL,
    role_key       TEXT NOT NULL,
    role_sort      INTEGER NOT NULL,
    data_scope     INTEGER DEFAULT 1,
    status         INTEGER NOT NULL,
    allow_register INTEGER DEFAULT 0 NOT NULL,
    is_admin       INTEGER DEFAULT 0 NOT NULL,
    creator_id     INTEGER,
    create_time    TEXT,
    updater_id     INTEGER,
    update_time    TEXT,
    remark         TEXT,
    deleted        INTEGER DEFAULT 0 NOT NULL
);

create table if not exists sys_role_menu
(
    role_id INTEGER NOT NULL,
    menu_id INTEGER NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

create table if not exists sys_user
(
    user_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    role_id      INTEGER,
    username     TEXT NOT NULL,
    nickname     TEXT NOT NULL,
    user_type    INTEGER DEFAULT 0,
    email        TEXT DEFAULT '',
    phone_number TEXT DEFAULT '',
    sex          INTEGER DEFAULT 0,
    avatar       TEXT DEFAULT '',
    password     TEXT DEFAULT '' NOT NULL,
    status       INTEGER DEFAULT 0 NOT NULL,
    login_ip     TEXT DEFAULT '',
    login_date   TEXT,
    is_admin     INTEGER DEFAULT 0 NOT NULL,
    source       INTEGER NOT NULL,
    creator_id   INTEGER,
    create_time  TEXT,
    updater_id   INTEGER,
    update_time  TEXT,
    remark       TEXT,
    deleted      INTEGER DEFAULT 0 NOT NULL
);

create table if not exists sys_login_token
(
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    token_key       TEXT NOT NULL,
    login_user_json TEXT NOT NULL,
    username        TEXT,
    user_id         INTEGER,
    login_ip        TEXT,
    expire_time     TEXT NOT NULL,
    create_time     TEXT NOT NULL
);

CREATE UNIQUE INDEX uk_token_key ON sys_login_token (token_key);
CREATE INDEX idx_expire_time_token ON sys_login_token (expire_time);
CREATE INDEX idx_user_id_token ON sys_login_token (user_id);