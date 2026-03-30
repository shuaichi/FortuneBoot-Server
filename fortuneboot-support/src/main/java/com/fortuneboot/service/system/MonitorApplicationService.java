package com.fortuneboot.service.system;

import cn.hutool.core.util.StrUtil;
import com.fortuneboot.common.utils.jackson.JacksonUtil;
import com.fortuneboot.domain.dto.monitor.OnlineUserDTO;
import com.fortuneboot.domain.dto.monitor.ServerInfo;
import com.fortuneboot.domain.entity.system.SysLoginTokenEntity;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.repository.system.SysLoginTokenRepo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author valarchie
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorApplicationService {

    private final SysLoginTokenRepo loginTokenRepo;

    public List<OnlineUserDTO> getOnlineUserList(String username, String ipAddress) {
        List<SysLoginTokenEntity> tokens = loginTokenRepo.listValidTokens();

        List<OnlineUserDTO> onlineUsers = tokens.stream()
                .map(token -> {
                    try {
                        SystemLoginUser loginUser = JacksonUtil.from(token.getLoginUserJson(), SystemLoginUser.class);
                        return loginUser != null ? new OnlineUserDTO(loginUser) : null;
                    } catch (Exception e) {
                        log.warn("解析登录用户信息失败, tokenKey: {}", token.getTokenKey(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(o -> StrUtil.isEmpty(username) || username.equals(o.getUsername()))
                .filter(o -> StrUtil.isEmpty(ipAddress) || ipAddress.equals(o.getIpAddress()))
                .collect(Collectors.toList());

        Collections.reverse(onlineUsers);
        return onlineUsers;
    }

    public ServerInfo getServerInfo() {
        return ServerInfo.fillInfo();
    }

}