-- ----------------------------
-- 1. 账单成员表 (SQLite)
-- ----------------------------
CREATE TABLE `fortune_member`
(
    `member_id`   INTEGER PRIMARY KEY AUTOINCREMENT, -- 主键
    `book_id`     INTEGER NOT NULL,                  -- 账本ID
    `member_name` TEXT    NOT NULL,                  -- 成员名称
    `sort`        INTEGER DEFAULT 0,                 -- 排序
    `enable`      INTEGER DEFAULT 1,                 -- 是否启用 (1=启用,0=禁用)
    `remark`      TEXT,                              -- 备注
    `recycle_bin` INTEGER DEFAULT 0,                 -- 回收站
    `creator_id`  INTEGER,                           -- 创建者ID
    `create_time` TEXT    DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    `updater_id`  INTEGER,                           -- 更新者ID
    `update_time` TEXT    DEFAULT CURRENT_TIMESTAMP, -- 更新时间 (SQLite 无 ON UPDATE)
    `deleted`     INTEGER DEFAULT 0                  -- 删除标志
);
-- 索引
CREATE INDEX idx_fortune_member_book_id ON fortune_member (book_id);
CREATE INDEX idx_fortune_member_book_deleted_enable_sort ON fortune_member (book_id, deleted, enable, sort);

-- ----------------------------
-- 2. 账单与成员关系表 (SQLite)
-- ----------------------------
CREATE TABLE `fortune_member_relation`
(
    `member_relation_id` INTEGER PRIMARY KEY AUTOINCREMENT, -- 主键
    `bill_id`            INTEGER NOT NULL,                  -- 账单ID
    `member_id`          INTEGER NOT NULL,                  -- 成员ID
    `creator_id`         INTEGER,                           -- 创建者ID
    `create_time`        TEXT    DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    `updater_id`         INTEGER,                           -- 更新者ID
    `update_time`        TEXT    DEFAULT CURRENT_TIMESTAMP, -- 更新时间
    `deleted`            INTEGER DEFAULT 0,                 -- 删除标志
    UNIQUE (bill_id, member_id)                             -- 唯一约束
);
-- 索引
CREATE INDEX idx_fortune_member_relation_bill_deleted ON fortune_member_relation (bill_id, deleted);
CREATE INDEX idx_fortune_member_relation_member_deleted ON fortune_member_relation (member_id, deleted);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (80,'成员管理', 1, 'FortuneBookMember', 84, '/fortune/member/index', 0, '', '{"title":"成员管理","showLink":false,"showParent":true}', 1, '', 1, '2025-02-15 02:41:14', 1, '2025-02-22 17:09:19', 0);