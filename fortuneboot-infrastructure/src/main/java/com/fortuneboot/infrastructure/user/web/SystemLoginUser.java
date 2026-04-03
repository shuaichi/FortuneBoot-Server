package com.fortuneboot.infrastructure.user.web;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fortuneboot.infrastructure.user.base.BaseLoginUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 登录用户身份权限
 * @author valarchie
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SystemLoginUser extends BaseLoginUser {

    private static final long serialVersionUID = 1L;

    // 序列化时使用 isAdmin
    @JsonProperty("isAdmin")
    private Boolean isAdmin;

    private RoleInfo roleInfo;

    /**
     * 当超过这个时间 则触发刷新缓存时间
     */
    private Long autoRefreshCacheTime;

    public SystemLoginUser(Long userId, Boolean isAdmin, String username, String password, RoleInfo roleInfo) {
        this.userId = userId;
        this.isAdmin = isAdmin;
        this.username = username;
        this.password = password;
        this.roleInfo = roleInfo;
    }

    public Long getRoleId() {
        return getRoleInfo().getRoleId();
    }
}
