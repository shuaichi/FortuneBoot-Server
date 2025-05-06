create table if not exists fortune_goods_keeper
(
    goods_keeper_id bigint auto_increment comment '主键'
    primary key,
    goods_name      varchar(128)         not null comment '名称',
    book_id         bigint               not null comment '账本id',
    category_id     bigint               not null comment '分类id',
    price           decimal(20, 4)       not null comment '购买价格',
    purchase_date   date                 not null comment '购买日期',
    warranty_date   date                 null comment '保修日期',
    use_by_times    tinyint(1) default 0 null comment '按次使用',
    usage_num       bigint               null comment '使用次数',
    status          tinyint              null comment '状态',
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

INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES ( '归物', 1, 'FortuneGoodsKeeper', 66, '/fortune/goods-keeper/index', 0, '', '{"title":"归物","icon":"fa:recycle","showLink":true,"showParent":true,"rank":7}', 1, '', 1, '2025-05-06 16:50:46', 1, '2025-05-06 19:49:38', 0);