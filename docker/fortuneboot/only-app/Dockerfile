# 第一阶段：构建Java应用
FROM maven:3.9.9-eclipse-temurin-21-alpine AS java-builder

# 安装依赖
RUN apk add --no-cache git openssh-client

# SSH配置
ARG SSH_PRIVATE_KEY
RUN mkdir -p /root/.ssh && \
    echo "${SSH_PRIVATE_KEY}" > /root/.ssh/id_rsa && \
    chmod 600 /root/.ssh/id_rsa && \
    ssh-keyscan github.com >> /root/.ssh/known_hosts

# 克隆Java仓库
ARG JAVA_REPO=https://github.com/shuaichi/FortuneBoot-Server.git
ARG JAVA_BRANCH=master
RUN git clone --branch ${JAVA_BRANCH} --depth 1 ${JAVA_REPO} /app/server

# 构建Java
WORKDIR /app/server
RUN mvn dependency:go-offline
RUN mvn clean package -DskipTests

# 第二阶段：构建前端应用
FROM node:18-alpine AS node-builder

# 安装依赖
RUN apk add --no-cache git openssh-client

# SSH配置
ARG SSH_PRIVATE_KEY
RUN mkdir -p /root/.ssh && \
    echo "${SSH_PRIVATE_KEY}" > /root/.ssh/id_rsa && \
    chmod 600 /root/.ssh/id_rsa && \
    ssh-keyscan github.com >> /root/.ssh/known_hosts

# 克隆Vue仓库
ARG VUE_REPO=https://github.com/shuaichi/FortuneBoot-Ui.git
ARG VUE_BRANCH=master
RUN git clone --branch ${VUE_BRANCH} --depth 1 ${VUE_REPO} /app/ui
ENV TZ=Asia/Shanghai
# 安装pnpm并构建
WORKDIR /app/ui
RUN corepack enable && corepack prepare pnpm@8.15.0 --activate
RUN pnpm install --frozen-lockfile
RUN pnpm build
# 第三阶段：生产镜像
FROM registry.cn-hangzhou.aliyuncs.com/chishenjianglin/nginx-jdk:debian12

# 复制构建产物
COPY --from=java-builder /app/server/fortuneboot-main/target/*.jar /app.jar
COPY --from=node-builder /app/ui/dist /usr/share/nginx/html
COPY nginx.conf.template /etc/nginx/conf.d/default.conf
COPY gzip.conf /etc/nginx/conf.d/gzip.conf
COPY supervisord.conf /etc/supervisord.conf

# 时区配置
RUN { \
        echo "deb http://mirrors.tuna.tsinghua.edu.cn/debian/ bookworm main contrib non-free non-free-firmware"; \
        echo "deb http://mirrors.tuna.tsinghua.edu.cn/debian/ bookworm-updates main contrib non-free non-free-firmware"; \
        echo "deb http://mirrors.tuna.tsinghua.edu.cn/debian-security bookworm-security main contrib non-free non-free-firmware"; \
    } > /etc/apt/sources.list &&\
    apt-get update && \
    apt-get install -y  tzdata ca-certificates supervisor && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /
EXPOSE 80 8080
CMD ["supervisord", "-n", "-c", "/etc/supervisord.conf"]