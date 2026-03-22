# syntax=docker/dockerfile:1.4
# 使用 native-image-community 镜像
FROM ghcr.io/graalvm/native-image-community:25-ol8 AS builder

# 安装 gzip 工具，以便后续能正常解压
RUN microdnf install -y gzip tar && microdnf clean all

# 手动安装 Maven (避开 microdnf)
WORKDIR /opt
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz | tar xzf -
ENV PATH="/opt/apache-maven-3.9.9/bin:${PATH}"

WORKDIR /app
COPY . .

# 先构建父模块，以便生成所有依赖
RUN mvn clean install -DskipTests

# 👇 设置环境变量，让 GraalVM 采用最大兼容指令集编译
ENV NATIVE_IMAGE_OPTIONS="-march=compatibility"

# 构建特定的模块（fortuneboot-starter）
RUN mvn package -Pnative -DskipTests -pl fortuneboot-starter

# -----------------------------------------
FROM debian:12-slim

WORKDIR /app

# 从构建阶段复制生成的 native image 文件
COPY --from=builder /app/fortuneboot-starter/target/fortuneboot-starter /app/

RUN chmod +x /app/*

CMD ["./fortuneboot-starter"]