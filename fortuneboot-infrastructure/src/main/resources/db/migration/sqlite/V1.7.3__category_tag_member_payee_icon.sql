-- ----------------------------
-- 为分类、标签、成员、交易对象表增加自定义图标字段 (SQLite)
-- 存储 Iconify 图标名（如 ep:menu），由前端图标选择器写入
-- 存量数据保持 NULL，前端会回退为按各模块的默认图标
-- ----------------------------
ALTER TABLE `fortune_category`
    ADD COLUMN `icon` TEXT DEFAULT NULL;

ALTER TABLE `fortune_tag`
    ADD COLUMN `icon` TEXT DEFAULT NULL;

ALTER TABLE `fortune_member`
    ADD COLUMN `icon` TEXT DEFAULT NULL;

ALTER TABLE `fortune_payee`
    ADD COLUMN `icon` TEXT DEFAULT NULL;

-- 运维回滚：
-- SQLite 3.35+ 支持 DROP COLUMN
-- ALTER TABLE `fortune_category` DROP COLUMN `icon`;
-- ALTER TABLE `fortune_tag` DROP COLUMN `icon`;
-- ALTER TABLE `fortune_member` DROP COLUMN `icon`;
-- ALTER TABLE `fortune_payee` DROP COLUMN `icon`;
