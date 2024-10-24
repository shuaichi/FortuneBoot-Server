package com.fortuneboot.infrastructure.filter;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
/**
 * 过滤器模板代码
 * @author valarchie
 */
public class TestFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 实现你的逻辑
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
