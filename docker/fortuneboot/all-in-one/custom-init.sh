#!/bin/bash

# 读取环境变量
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-3306}
DB_NAME=${DB_NAME:-fortune_boot}
DB_USERNAME=${DB_USERNAME:-root}
DB_PASSWORD=${DB_PASSWORD:-Jk9Lm2XqPvZt8N7e}
# 初始化标志文件路径（建议使用持久化存储位置）
INIT_FLAG="/data/initialized.flag"
set -e
# 检查是否已初始化
if [ ! -f "$INIT_FLAG" ]; then
    echo "首次启动，执行初始化脚本..."
    # 等待数据库就绪
    echo "Waiting for MySQL to start..."
    while ! mysqladmin ping -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" --silent; do
        sleep 1
    done
    # 创建标志文件
    touch "$INIT_FLAG"
    # 创建数据库（若不存在）
    echo "Creating database $DB_NAME if not exists..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\`;"

    # 执行完整初始化脚本
    echo "Initializing database schema and data..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" "$DB_NAME" < /app/init.sql

    echo "Initialization completed."
    shutdown -r now


    echo "初始化完成，已创建标志文件。"
else
    echo "已初始化，跳过脚本执行。"
fi