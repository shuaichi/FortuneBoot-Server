-- ----------------------------
-- 为分类、标签、成员、交易对象表增加自定义图标字段
-- 存储 Iconify 图标名（如 ep:menu），由前端图标选择器写入
-- 存量数据保持 NULL，前端会回退为按各模块的默认图标
-- ----------------------------
ALTER TABLE `fortune_category`
    ADD COLUMN `icon` varchar(64) DEFAULT NULL COMMENT '分类图标（Iconify 图标名，如 ep:menu）' AFTER `category_name`;

ALTER TABLE `fortune_tag`
    ADD COLUMN `icon` varchar(64) DEFAULT NULL COMMENT '标签图标（Iconify 图标名，如 ep:collection-tag）' AFTER `tag_name`;

ALTER TABLE `fortune_member`
    ADD COLUMN `icon` varchar(64) DEFAULT NULL COMMENT '成员图标（Iconify 图标名，如 ep:user）' AFTER `member_name`;

ALTER TABLE `fortune_payee`
    ADD COLUMN `icon` varchar(64) DEFAULT NULL COMMENT '交易对象图标（Iconify 图标名，如 ep:shop）' AFTER `payee_name`;

-- 运维回滚：
-- ALTER TABLE `fortune_category` DROP COLUMN `icon`;
-- ALTER TABLE `fortune_tag` DROP COLUMN `icon`;
-- ALTER TABLE `fortune_member` DROP COLUMN `icon`;
-- ALTER TABLE `fortune_payee` DROP COLUMN `icon`;
