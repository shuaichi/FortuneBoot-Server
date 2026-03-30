-- FortuneBoot V1.5.0 增量迁移
-- 去除 Redis 依赖，登录令牌持久化到数据库

-- 创建登录令牌持久化表
create table if not exists sys_login_token
(
    id              bigint auto_increment comment '主键ID'
        primary key,
    token_key       varchar(255)    not null comment '令牌唯一标识（UUID）',
    login_user_json text            not null comment '登录用户信息（JSON序列化）',
    username        varchar(64)     null comment '用户名',
    user_id         bigint          null comment '用户ID',
    login_ip        varchar(128)    null comment '登录IP',
    expire_time     datetime        not null comment '过期时间',
    create_time     datetime        not null comment '创建时间',
    unique key uk_token_key (token_key),
    index idx_expire_time (expire_time),
    index idx_user_id (user_id)
) comment '登录令牌持久化表';