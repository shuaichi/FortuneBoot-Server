-- FortuneBoot V1.4.0 增量迁移
-- sys_role 和 sys_user 新增 is_admin 字段

alter table sys_role
    add is_admin tinyint default 0 not null comment '是否超级管理员' after allow_register;

update sys_role set is_admin = 1 where role_id = 1;

alter table sys_user
    add is_admin tinyint default 0 not null comment '超级管理员标志（1是，0否）' after login_date;

update sys_user set is_admin = 1 where role_id = 1;