# 多阶段构建：安装 Java
FROM rockylinux/rockylinux:9.5 AS builder
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN yum install -y \
    ca-certificates \
    tar \
    && yum clean all
ENV LANG=C.UTF-8 \
    JAVA_URL=https://download.oracle.com/java/21/latest/jdk-21_linux-ARCH_bin.tar.gz \
    JAVA_HOME=/usr/lib/jvm/jdk-21
RUN set -eux; \
    ARCH="$(uname -m | sed 's/x86_64/x64/;s/aarch64/aarch64/')"; \
    JAVA_URL=$(echo "${JAVA_URL}" | sed "s/ARCH/${ARCH}/g"); \
    curl -fL --retry 3 --output /tmp/jdk.tar.gz "${JAVA_URL}"; \
    echo "  $(curl -fL "${JAVA_URL}.sha256")  /tmp/jdk.tar.gz" | sha256sum -c -; \
    mkdir -p "${JAVA_HOME}"; \
    tar -xf /tmp/jdk.tar.gz -C "${JAVA_HOME}" --strip-components=1; \
    rm -f /tmp/jdk.tar.gz
# 主镜像
FROM rockylinux/rockylinux:9.5
# 创建系统用户和组
RUN set -eux; \
    groupadd --system --gid 999 mysql; \
    useradd --system --uid 999 --gid 999 --home-dir /var/lib/mysql --no-create-home mysql; \
    groupadd -r -g 1000 redis && useradd -r -g redis -u 1000 redis
# 安装基础依赖
RUN yum install -y epel-release \
    && yum clean all \
    && yum install -y \
    fontconfig \
    freetype \
    ca-certificates \
    gnupg2 \
    tzdata \
    wget \
    gcc \
    glibc-devel \
    openssl-devel \
    make \
    supervisor \
    bzip2 \
    gzip \
    openssl \
    xz \
    zstd \
    findutils \
    && yum clean all
# 设置 Java 环境
ENV JAVA_HOME=/usr/lib/jvm/jdk-21 \
    PATH="$PATH:${JAVA_HOME}/bin"
COPY --from=builder /usr/lib/jvm/jdk-21 /usr/lib/jvm/jdk-21
RUN ln -sfT "${JAVA_HOME}" /usr/lib/jvm/default-java \
    && update-alternatives --install /usr/bin/java java "${JAVA_HOME}/bin/java" 1 \
    && update-alternatives --install /usr/bin/javac javac "${JAVA_HOME}/bin/javac" 1 \
    && update-alternatives --install /usr/bin/jshell jshell "${JAVA_HOME}/bin/jshell" 1
# 安装 GOSU（统一使用 MySQL 的安装方式）
ENV GOSU_VERSION 1.17
RUN set -eux; \
    arch="$(uname -m)"; \
    case "$arch" in \
        aarch64) gosuArch='arm64' ;; \
        x86_64) gosuArch='amd64' ;; \
        *) echo >&2 "error: unsupported architecture: '$arch'"; exit 1 ;; \
    esac; \
    curl -fL -o /usr/local/bin/gosu.asc "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$gosuArch.asc"; \
    curl -fL -o /usr/local/bin/gosu "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$gosuArch"; \
    export GNUPGHOME="$(mktemp -d)"; \
    gpg --batch --keyserver hkps://keys.openpgp.org --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4; \
    gpg --batch --verify /usr/local/bin/gosu.asc /usr/local/bin/gosu; \
    rm -rf "$GNUPGHOME" /usr/local/bin/gosu.asc; \
    chmod +x /usr/local/bin/gosu; \
    gosu --version; \
    gosu nobody true
# 安装 MySQL
RUN set -eux; \
    key='BCA4 3417 C3B4 85DD 128E C6D4 B7B3 B788 A8D3 785C'; \
    export GNUPGHOME="$(mktemp -d)"; \
    gpg --batch --keyserver keyserver.ubuntu.com --recv-keys "$key"; \
    gpg --batch --export --armor "$key" > /etc/pki/rpm-gpg/RPM-GPG-KEY-mysql; \
    rm -rf "$GNUPGHOME"
ENV MYSQL_MAJOR 8.0
ENV MYSQL_VERSION 8.0.41-1.el9
ENV MYSQL_SHELL_VERSION 8.0.41-1.el9
RUN set -eu; \
    { \
        echo '[mysql8.0-server-minimal]'; \
        echo 'name=MySQL 8.0 Server Minimal'; \
        echo 'enabled=1'; \
        echo 'baseurl=https://repo.mysql.com/yum/mysql-8.0-community/docker/el/9/$basearch/'; \
        echo 'gpgcheck=1'; \
        echo 'gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-mysql'; \
        echo 'module_hotfixes=true'; \
    } | tee /etc/yum.repos.d/mysql-community-minimal.repo; \
    { \
        echo '[mysql-tools-community]'; \
        echo 'name=MySQL Tools Community'; \
        echo 'baseurl=https://repo.mysql.com/yum/mysql-tools-community/el/9/$basearch/'; \
        echo 'enabled=1'; \
        echo 'gpgcheck=1'; \
        echo 'gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-mysql'; \
        echo 'module_hotfixes=true'; \
    } | tee /etc/yum.repos.d/mysql-community-tools.repo
RUN set -eux; \
    yum install -y "mysql-community-server-minimal-$MYSQL_VERSION" "mysql-shell-$MYSQL_SHELL_VERSION"; \
    yum clean all; \
    sed -i 's!^socket=.*!socket=/var/run/mysqld/mysqld.sock!' /etc/my.cnf; \
    { echo '[client]'; echo 'socket=/var/run/mysqld/mysqld.sock'; } >> /etc/my.cnf; \
    { echo; echo '!includedir /etc/mysql/conf.d/'; } >> /etc/my.cnf; \
    mkdir -p /etc/mysql/conf.d; \
    mkdir -p /var/lib/mysql /var/run/mysqld; \
    chown mysql:mysql /var/lib/mysql /var/run/mysqld; \
    chmod 1777 /var/lib/mysql /var/run/mysqld; \
    mkdir /docker-entrypoint-initdb.d
# 安装 Nginx
ENV NGINX_VERSION=1.26.3
RUN set -eux; \
    ARCH="$(uname -m)"; \
    case "${ARCH}" in \
        x86_64) NGINX_ARCH='x86_64' ;; \
        aarch64) NGINX_ARCH='aarch64' ;; \
        *) echo "Unsupported architecture: ${ARCH}"; exit 1 ;; \
    esac; \
    yum install -y "https://nginx.org/packages/rhel/9/${NGINX_ARCH}/RPMS/nginx-${NGINX_VERSION}-1.el9.ngx.${NGINX_ARCH}.rpm"; \
    yum clean all; \
    ln -sf /dev/stdout /var/log/nginx/access.log; \
    ln -sf /dev/stderr /var/log/nginx/error.log
# 安装 Redis
ENV REDIS_VERSION=7.2.7 \
    REDIS_DOWNLOAD_URL=http://download.redis.io/releases/redis-7.2.7.tar.gz \
    REDIS_DOWNLOAD_SHA=72c081e3b8cfae7144273d26d76736f08319000af46c01515cad5d29765cead5
RUN set -eux; \
    wget -O redis.tar.gz "$REDIS_DOWNLOAD_URL"; \
    echo "$REDIS_DOWNLOAD_SHA *redis.tar.gz" | sha256sum -c -; \
    mkdir -p /usr/src/redis; \
    tar -xzf redis.tar.gz -C /usr/src/redis --strip-components=1; \
    rm redis.tar.gz; \
    sed -ri 's!^( *createBoolConfig[(]"protected-mode",.*, *)1( *,.*[)],)$!\10\2!' /usr/src/redis/src/config.c; \
    export BUILD_TLS=yes; \
    make -C /usr/src/redis -j "$(nproc)" MALLOC=libc all; \
    make -C /usr/src/redis install; \
    rm -rf /usr/src/redis; \
    mkdir /redis && \
	mkdir /redis/conf && \
	mkdir /redis/data && \
	mkdir /redis/log && \
	chown redis:redis /redis
# 复制配置文件
COPY redis.conf /redis/conf/
COPY mysql-config/ /etc/mysql/
COPY supervisord.conf /etc/supervisord.conf
COPY mysql-entrypoint.sh redis-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/*.sh

# 配置卷和端口
VOLUME /var/lib/mysql /data
EXPOSE 80 443 3306 6379 33060
WORKDIR /root
# 使用 Supervisor 管理进程
CMD ["supervisord", "-n", "-c", "/etc/supervisord.conf"]