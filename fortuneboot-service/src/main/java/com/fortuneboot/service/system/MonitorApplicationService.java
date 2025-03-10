package com.fortuneboot.service.system;

import cn.hutool.core.util.StrUtil;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode.Internal;
import com.fortuneboot.service.cache.CacheCenter;
import com.fortuneboot.domain.dto.monitor.OnlineUserDTO;
import com.fortuneboot.domain.dto.monitor.RedisCacheInfoDTO;
import com.fortuneboot.domain.dto.monitor.ServerInfo;
import com.fortuneboot.infrastructure.cache.redis.CacheKeyEnum;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author valarchie
 */
@Service
@RequiredArgsConstructor
public class MonitorApplicationService {

    private final RedisTemplate<String, ?> redisTemplate;

    public RedisCacheInfoDTO getRedisCacheInfo() {
        Properties info = (Properties) redisTemplate.execute((RedisCallback<Object>) RedisServerCommands::info);
        Properties commandStats = (Properties) redisTemplate.execute(
            (RedisCallback<Object>) connection -> connection.serverCommands().info("commandstats"));
        Long dbSize = redisTemplate.execute(RedisServerCommands::dbSize);

        if (commandStats == null || info == null) {
            throw new ApiException(Internal.INTERNAL_ERROR, "获取Redis监控信息失败。");
        }

        RedisCacheInfoDTO cacheInfo = new RedisCacheInfoDTO();

        cacheInfo.setInfo(info);
        cacheInfo.setDbSize(dbSize);
        cacheInfo.setCommandStats(new ArrayList<>());

        commandStats.stringPropertyNames().forEach(key -> {
            String property = commandStats.getProperty(key);

            RedisCacheInfoDTO.CommandStatusDTO commonStatus = new RedisCacheInfoDTO.CommandStatusDTO();
            commonStatus.setName(StrUtil.removePrefix(key, "cmdstat_"));
            commonStatus.setValue(StrUtil.subBetween(property, "calls=", ",usec"));

            cacheInfo.getCommandStats().add(commonStatus);
        });

        return cacheInfo;
    }

    public List<OnlineUserDTO> getOnlineUserList(String username, String ipAddress) {
        Collection<String> keys = redisTemplate.keys(CacheKeyEnum.LOGIN_USER.key() + "*");

        Stream<OnlineUserDTO> onlineUserStream = keys.stream().map(o ->
                    CacheCenter.loginUserCache.getObjectOnlyInCacheByKey(o))
            .filter(Objects::nonNull).map(OnlineUserDTO::new);

        List<OnlineUserDTO> filteredOnlineUsers = onlineUserStream
            .filter(o ->
                StrUtil.isEmpty(username) || username.equals(o.getUsername())
            ).filter( o ->
                StrUtil.isEmpty(ipAddress) || ipAddress.equals(o.getIpAddress())
            ).collect(Collectors.toList());

        Collections.reverse(filteredOnlineUsers);
        return filteredOnlineUsers;
    }

    public ServerInfo getServerInfo() {
        return ServerInfo.fillInfo();
    }


}
