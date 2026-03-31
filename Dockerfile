# syntax=docker/dockerfile:1.4

# ========== 第一阶段：构建前端应用 ==========
FROM --platform=$BUILDPLATFORM node:18-alpine AS node-builder

RUN apk add --no-cache git

# 克隆 Vue 仓库并构建
ARG VUE_REPO=https://github.com/shuaichi/FortuneBoot-Ui.git
ARG VUE_BRANCH=master
RUN git clone --branch ${VUE_BRANCH} --depth 1 ${VUE_REPO} /app/ui

WORKDIR /app/ui
RUN corepack enable && corepack prepare pnpm@8.15.0 --activate
RUN pnpm install --force
RUN pnpm build

# ========== 第二阶段：构建 Native Image ==========
FROM ghcr.io/graalvm/native-image-community:25-ol8 AS builder

# 安装 gzip 工具，以便后续能正常解压
RUN microdnf install -y gzip tar && microdnf clean all

# 手动安装 Maven (避开 microdnf)
WORKDIR /opt
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz | tar xzf -
ENV PATH="/opt/apache-maven-3.9.9/bin:${PATH}"

WORKDIR /app
COPY . .

# 将前端构建产物复制到 Spring Boot 静态资源目录
COPY --from=node-builder /app/ui/dist/ /app/fortuneboot-starter/src/main/resources/static/

# 先构建父模块，以便生成所有依赖
RUN mvn clean install -DskipTests

# 设置环境变量，让 GraalVM 采用最大兼容指令集编译
ENV NATIVE_IMAGE_OPTIONS="-march=compatibility"

# 构建特定的模块（fortuneboot-starter）
RUN mvn package -Pnative -DskipTests -pl fortuneboot-starter

# ========== 第三阶段：运行时镜像 ==========
FROM debian:13-slim

WORKDIR /app

# 从构建阶段复制生成的 native image 文件
COPY --from=builder /app/fortuneboot-starter/target/fortuneboot-starter /app/

RUN chmod +x /app/*

# 创建数据目录（用于 SQLite 数据文件）
RUN mkdir -p /data

# 默认 SQLite 模式，零配置开箱即用
ENV DB_TYPE=sqlite
ENV DB_PATH=/data/fortuneboot.db

# 声明数据卷，确保容器重启数据不丢失
VOLUME ["/data"]

EXPOSE 8080

CMD ["./fortuneboot-starter"]