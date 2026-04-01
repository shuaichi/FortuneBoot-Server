package com.fortuneboot.infrastructure.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * API 前缀重写过滤器
 * <p>
 * 将带有 api-prefix（如 /prod-api、/dev-api）的请求路径剥离前缀后继续传递，
 * 同时也支持不带前缀的原生请求直接通过。
 * 用于替代 nginx 的 rewrite ^/prod-api/(.*)$ /$1 break 配置。
 * </p>
 * <p>
 * 使用 HttpServletRequestWrapper 而非 forward()，
 * 确保后续的 Spring Security 过滤器链能正常处理请求（解析 JWT Token 等）。
 * </p>
 * <p>
 * 对于需要路由到独立 Servlet（如 Druid StatViewServlet）的请求，
 * 使用 forward() 使容器重新路由到正确的目标 Servlet。
 * </p>
 *
 * @author zhangchi118
 */
@AllArgsConstructor
@Slf4j
public class ApiPrefixRewriteFilter implements Filter {

    private static final String DRUID_PATH_PREFIX = "/druid";

    private final String apiPrefix;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (apiPrefix == null || apiPrefix.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = uri.substring(contextPath.length());

        // 如果请求路径以 apiPrefix/ 开头，则剥离前缀
        if (path.startsWith(apiPrefix + "/")) {
            String newPath = path.substring(apiPrefix.length());
            log.debug("API prefix rewrite: {} -> {}", path, newPath);

            // 对于独立 Servlet（如 Druid 监控），使用 forward 让容器重新路由到正确的 Servlet
            if (newPath.startsWith(DRUID_PATH_PREFIX + "/") || newPath.equals(DRUID_PATH_PREFIX)) {
                request.getRequestDispatcher(newPath).forward(request, response);
                return;
            }

            chain.doFilter(new RewrittenRequest(httpRequest, contextPath + newPath), response);
            return;
        }

        // 精确匹配 apiPrefix 本身（不带尾部斜杠）
        if (path.equals(apiPrefix)) {
            log.debug("API prefix rewrite: {} -> /", path);
            chain.doFilter(new RewrittenRequest(httpRequest, contextPath + "/"), response);
            return;
        }

        // 不带前缀的原生请求，直接放行
        chain.doFilter(request, response);
    }

    /**
     * 重写请求 URI 的包装器，不改变其他任何请求属性
     */
    private static class RewrittenRequest extends HttpServletRequestWrapper {

        private final String newRequestURI;

        public RewrittenRequest(HttpServletRequest request, String newRequestURI) {
            super(request);
            this.newRequestURI = newRequestURI;
        }

        @Override
        public String getRequestURI() {
            return newRequestURI;
        }

        @Override
        public StringBuffer getRequestURL() {
            StringBuffer url = new StringBuffer();
            url.append(getScheme()).append("://")
                    .append(getServerName()).append(':')
                    .append(getServerPort())
                    .append(newRequestURI);
            return url;
        }

        @Override
        public String getServletPath() {
            return newRequestURI;
        }
    }
}
