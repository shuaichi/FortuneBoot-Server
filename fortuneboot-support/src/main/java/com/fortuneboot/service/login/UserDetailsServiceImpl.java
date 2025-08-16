package com.fortuneboot.service.login;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.customize.config.SecurityConfig;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.infrastructure.user.web.RoleInfo;
import com.fortuneboot.infrastructure.user.web.DataScopeEnum;
import com.fortuneboot.common.enums.common.UserStatusEnum;
import com.fortuneboot.common.enums.BasicEnumUtil;
import com.fortuneboot.domain.entity.system.SysMenuEntity;
import com.fortuneboot.domain.entity.system.SysRoleEntity;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import com.fortuneboot.repository.system.SysMenuRepo;
import com.fortuneboot.repository.system.SysRoleRepo;
import com.fortuneboot.repository.system.SysUserRepo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 自定义加载用户信息通过用户名
 * 用于SpringSecurity 登录流程
 * 没有办法把这个类 放进loginService中  会在SecurityConfig中造成循环依赖
 * @see SecurityConfig#filterChain(HttpSecurity)
 * @author valarchie
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserRepo userService;

    private final SysMenuRepo menuService;

    private final SysRoleRepo roleService;

    private final TokenService tokenService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUserEntity userEntity = userService.getUserByUserName(username);
        if (userEntity == null) {
            log.info("登录用户：{} 不存在.", username);
            throw new ApiException(ErrorCode.Business.USER_NON_EXIST, username);
        }
        if (!Objects.equals(UserStatusEnum.NORMAL.getValue(), userEntity.getStatus())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new ApiException(ErrorCode.Business.USER_IS_DISABLE, username);
        }

        RoleInfo roleInfo = getRoleInfo(userEntity.getRoleId(), userEntity.getIsAdmin());

        SystemLoginUser loginUser = new SystemLoginUser(userEntity.getUserId(), userEntity.getIsAdmin(), userEntity.getUsername(), userEntity.getPassword(), roleInfo);
        loginUser.fillLoginInfo();
        loginUser.setAutoRefreshCacheTime(loginUser.getLoginInfo().getLoginTime()
            + TimeUnit.MINUTES.toMillis(tokenService.getAutoRefreshTime()));
        return loginUser;
    }

    public RoleInfo getRoleInfo(Long roleId, boolean isAdmin) {
        if (roleId == null) {
            return RoleInfo.EMPTY_ROLE;
        }

        if (isAdmin) {
            LambdaQueryWrapper<SysMenuEntity> menuQuery = Wrappers.lambdaQuery();
            menuQuery.select(SysMenuEntity::getMenuId);
            List<SysMenuEntity> allMenus = menuService.list(menuQuery);

            Set<Long> allMenuIds = allMenus.stream().map(SysMenuEntity::getMenuId).collect(Collectors.toSet());

            return new RoleInfo(RoleInfo.ADMIN_ROLE_ID, RoleInfo.ADMIN_ROLE_KEY, DataScopeEnum.ALL, RoleInfo.ADMIN_PERMISSIONS, allMenuIds);

        }

        SysRoleEntity roleEntity = roleService.getById(roleId);

        if (roleEntity == null) {
            return RoleInfo.EMPTY_ROLE;
        }

        List<SysMenuEntity> menuList = roleService.getMenuListByRoleId(roleId);

        Set<Long> menuIds = menuList.stream().map(SysMenuEntity::getMenuId).collect(Collectors.toSet());
        Set<String> permissions = menuList.stream().map(SysMenuEntity::getPermission).collect(Collectors.toSet());

        DataScopeEnum dataScopeEnum = BasicEnumUtil.fromValue(DataScopeEnum.class, roleEntity.getDataScope());
        return new RoleInfo(roleId, roleEntity.getRoleKey(), dataScopeEnum, permissions, menuIds);
    }


}
