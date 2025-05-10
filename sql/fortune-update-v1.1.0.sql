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

INSERT INTO sys_menu ( menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES ( '归物', 1, 'FortuneGoodsKeeper', 66, '/fortune/goods-keeper/index', 0, '', '{"title":"归物","icon":"fa:recycle","showLink":true,"showParent":true,"rank":7}', 1, '', 1, '2025-05-06 16:50:46', 1, '2025-05-06 19:49:38', 0);

-- 账户设置索引
CREATE INDEX idx_fortune_account_main ON fortune_account (group_id, recycle_bin, account_type, sort);
CREATE INDEX idx_fortune_account_balance ON fortune_account (balance);
CREATE INDEX idx_fortune_account_currency ON fortune_account (currency_code);
CREATE INDEX idx_fortune_account_deleted ON fortune_account (deleted);

--账单设置索引
CREATE INDEX idx_fortune_bill_book_type_account_time ON fortune_bill (book_id, bill_type, account_id, trade_time);
CREATE INDEX idx_fortune_bill_book_time ON fortune_bill (book_id, trade_time);
CREATE INDEX idx_fortune_bill_book_amount ON fortune_bill (book_id, amount);
CREATE INDEX idx_fortune_bill_payee ON fortune_bill (payee_id);
CREATE INDEX idx_fortune_bill_title ON fortune_bill (title);
CREATE INDEX idx_fortune_bill_deleted ON fortune_bill (deleted);

-- 账本设置索引
CREATE INDEX idx_fortune_book_group_recycle_enable_sort ON fortune_book (group_id, recycle_bin, enable, sort);
CREATE INDEX idx_fortune_book_deleted ON fortune_book (deleted);

-- 分类设置索引
CREATE INDEX idx_fortune_category_book_recycle_type_enable_sort ON fortune_category (book_id, recycle_bin, category_type, enable, sort);
CREATE INDEX idx_fortune_category_book_parent ON fortune_category (book_id, parent_id);
CREATE INDEX idx_fortune_category_deleted ON fortune_category (deleted);

-- 设置账单分类关系索引
CREATE INDEX idx_fortune_category_relation_category ON fortune_category_relation (category_id);
CREATE INDEX idx_fortune_category_relation_bill ON fortune_category_relation (bill_id);
CREATE INDEX idx_fortune_category_relation_deleted ON fortune_category_relation (deleted);

-- 设置文件索引
CREATE INDEX idx_fortune_file_bill ON fortune_file (bill_id);
CREATE INDEX idx_fortune_file_deleted ON fortune_file (deleted);

-- 归物设置索引
CREATE INDEX idx_fortune_goods_keeper_book ON fortune_goods_keeper (book_id);
CREATE INDEX idx_fortune_goods_keeper_deleted ON fortune_goods_keeper (deleted);
-- 交易对象设置索引
CREATE INDEX idx_fortune_payee_book_recycle_enable_sort ON fortune_payee (book_id, recycle_bin, enable, sort);
CREATE INDEX idx_fortune_payee_book_expense_income ON fortune_payee (book_id, can_expense, can_income);
CREATE INDEX idx_fortune_payee_name ON fortune_payee (payee_name);
CREATE INDEX idx_fortune_payee_deleted ON fortune_payee (deleted);

-- 标签设置索引
CREATE INDEX idx_fortune_tag_book_recycle_sort ON fortune_tag (book_id, recycle_bin, sort);
CREATE INDEX idx_fortune_tag_book_expense_income_transfer_enable ON fortune_tag (book_id, can_expense, can_income, can_transfer, enable);
CREATE INDEX idx_fortune_tag_book_parent ON fortune_tag (book_id, parent_id);
CREATE INDEX idx_fortune_tag_name ON fortune_tag (tag_name);
CREATE INDEX idx_fortune_tag_deleted ON fortune_tag (deleted);

-- 设置账单标签关系索引
CREATE INDEX idx_fortune_tag_relation_tag ON fortune_tag_relation (tag_id);
CREATE INDEX idx_fortune_tag_relation_bill ON fortune_tag_relation (bill_id);
CREATE INDEX idx_fortune_tag_relation_deleted ON fortune_tag_relation (deleted);

-- 设置账单设置索引
CREATE INDEX idx_fortune_user_relation_user ON fortune_user_group_relation (user_id);
CREATE INDEX idx_fortune_user_relation_group ON fortune_user_group_relation (group_id);
CREATE INDEX idx_fortune_user_relation_deleted ON fortune_user_group_relation (deleted);