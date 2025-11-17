alter table sys_role
    add is_admin tinyint default 0 not null comment '是否超级管理员' after allow_register;

update sys_role set is_admin = 1 where role_id = 1;

update sys_user set is_admin = 1 where role_id = 1;
