-- ----------------------------
-- 为账户表增加自定义图标字段
-- 存储 Iconify 图标名（如 ep:wallet），由前端图标选择器写入
-- 存量数据保持 NULL，前端会回退为按账户类型的默认图标
-- ----------------------------
ALTER TABLE `fortune_account`
    ADD COLUMN `icon` varchar(64) DEFAULT NULL COMMENT '账户图标（Iconify 图标名，如 ep:wallet）' AFTER `account_name`;

-- 运维回滚：
-- ALTER TABLE `fortune_account` DROP COLUMN `icon`;
