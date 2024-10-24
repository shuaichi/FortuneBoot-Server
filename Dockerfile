FROM rockylinux:9.3
ENV JAVA_HOME /usr/java/jdk-21.0.2
ENV PATH $JAVA_HOME/bin:$PATH
# Default to UTF-8 file.encoding
ENV LANG C.UTF-8
ENV TZ=Asia/Shanghai
ENV JAVA_VERSION 21
COPY ./jdk-21.0.2_linux-x64_bin.tar.gz /root
RUN set -eux; \
		yum update -y; \
		yum install -y fontconfig; \
        mkdir -p "$JAVA_HOME"; \
        tar --extract \
    		--file /root/jdk-21.0.2_linux-x64_bin.tar.gz \
    		--directory "$JAVA_HOME" \
    		--strip-components 1 \
    		--no-same-owner \
    	; \
        ln -sfT "$JAVA_HOME" /usr/java/default; \
        ln -sfT "$JAVA_HOME" /usr/java/latest; \
    	for bin in "$JAVA_HOME/bin/"*; do \
    		base="$(basename "$bin")"; \
    		[ ! -e "/usr/bin/$base" ]; \
    		alternatives --install "/usr/bin/$base" "$base" "$bin" 20000; \
    	done; \
        rm -rf /root/jdk-21.0.2_linux-x64_bin.tar.gz; \
        java --version

COPY ./fortuneboot-main/target/fortuneboot-main-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]