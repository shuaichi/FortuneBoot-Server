alter table sys_config
drop key config_key_uniq_idx;

create index config_key_idx
    on sys_config (config_key);
