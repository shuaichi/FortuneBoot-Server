# 第一阶段：构建Java环境
FROM debian:bookworm-slim as builder

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# 安装基础工具
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    curl \
    tar \
    && rm -rf /var/lib/apt/lists/*

# 配置Java环境变量
ENV LANG=C.UTF-8 \
    JAVA_URL=https://download.oracle.com/java/21/latest/jdk-21_linux-ARCH_bin.tar.gz \
    JAVA_HOME=/usr/lib/jvm/jdk-21

# 下载并解压JDK
RUN set -eux; \
    ARCH="$(uname -m | sed 's/x86_64/x64/;s/aarch64/aarch64/')"; \
    JAVA_URL=$(echo "${JAVA_URL}" | sed "s/ARCH/${ARCH}/g"); \
    curl -fL --retry 3 --output /tmp/jdk.tar.gz "${JAVA_URL}"; \
    echo "  $(curl -fL "${JAVA_URL}.sha256")  /tmp/jdk.tar.gz" | sha256sum -c -; \
    mkdir -p "${JAVA_HOME}"; \
    tar -xf /tmp/jdk.tar.gz -C "${JAVA_HOME}" --strip-components=1; \
    rm -f /tmp/jdk.tar.gz

# 第二阶段：合并Java和Nginx
FROM debian:bookworm-slim

# 设置Java环境变量
ENV JAVA_HOME=/usr/lib/jvm/jdk-21 \
    PATH="$PATH:${JAVA_HOME}/bin"

# 从构建阶段复制JDK
COPY --from=builder /usr/lib/jvm/jdk-21 /usr/lib/jvm/jdk-21

# 安装Java依赖并配置替代项
RUN apt-get update && apt-get install -y --no-install-recommends \
    fontconfig \
    libfreetype6 \
    && rm -rf /var/lib/apt/lists/* \
    && ln -sfT "${JAVA_HOME}" /usr/lib/jvm/default-java \
    && update-alternatives --install /usr/bin/java java "${JAVA_HOME}/bin/java" 1 \
    && update-alternatives --install /usr/bin/javac javac "${JAVA_HOME}/bin/javac" 1 \
    && update-alternatives --install /usr/bin/jshell jshell "${JAVA_HOME}/bin/jshell" 1

# Nginx环境变量
ENV NGINX_VERSION=1.27.4 \
    NJS_VERSION=0.8.9 \
    PKG_RELEASE=1~bookworm

# 安装Nginx
RUN set -eux; \
    mkdir -p /etc/apt/keyrings; \
    apt-get update && apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        gnupg2; \
    NGINX_GPGKEY=573BFD6B3D8FBC641079A6ABABF5BD827BD9BF62; \
    curl -fsSL https://nginx.org/keys/nginx_signing.key | gpg --dearmor -o /etc/apt/keyrings/nginx-archive-keyring.gpg; \
    echo "deb [signed-by=/etc/apt/keyrings/nginx-archive-keyring.gpg] https://nginx.org/packages/mainline/debian/ bookworm nginx" > /etc/apt/sources.list.d/nginx.list; \
    apt-get update; \
    apt-get install -y --no-install-recommends \
        nginx=${NGINX_VERSION}-${PKG_RELEASE} \
        nginx-module-xslt=${NGINX_VERSION}-${PKG_RELEASE} \
        nginx-module-geoip=${NGINX_VERSION}-${PKG_RELEASE} \
        nginx-module-image-filter=${NGINX_VERSION}-${PKG_RELEASE} \
        nginx-module-njs=${NGINX_VERSION}+${NJS_VERSION}-${PKG_RELEASE}; \
    apt-get purge -y --auto-remove curl gnupg2; \
    rm -rf /var/lib/apt/lists/* /etc/apt/sources.list.d/nginx.list; \
    ln -sf /dev/stdout /var/log/nginx/access.log; \
    ln -sf /dev/stderr /var/log/nginx/error.log;


# 暴露端口并定义启动命令
EXPOSE 80 443
CMD ["nginx", "-g", "daemon off;"]