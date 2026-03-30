package com.fortuneboot.rest.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SPA 路由转发控制器
 * <p>
 * 替代 nginx 的 try_files $uri $uri/ /index.html 配置，
 * 将所有非静态资源、非 API 的前端路由请求转发到 index.html，
 * 由 Vue Router 在客户端处理路由。
 * </p>
 *
 * @author zhangchi118
 */
@Controller
public class SpaForwardController {

    /**
     * 匹配单级路由（如 /dashboard、/login）
     * 正则 [^\.]* 排除带扩展名的静态资源请求
     */
    @RequestMapping("/{path:[^.]*}")
    public String forward() {
        return "forward:/index.html";
    }

    /**
     * 匹配多级路由（如 /dashboard/analysis、/system/user/detail）
     */
    @RequestMapping("/**/{path:[^.]*}")
    public String forwardNested() {
        return "forward:/index.html";
    }
}