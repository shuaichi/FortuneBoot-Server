package com.fortuneboot.rest.system;

import com.fortuneboot.common.core.base.BaseController;
import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.customize.accessLog.AccessLog;
import com.fortuneboot.service.cache.CacheCenter;
import com.fortuneboot.service.system.MonitorApplicationService;
import com.fortuneboot.domain.dto.monitor.OnlineUserDTO;
import com.fortuneboot.domain.dto.monitor.RedisCacheInfoDTO;
import com.fortuneboot.domain.dto.monitor.ServerInfo;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 缓存监控
 *
 * @author valarchie
 */
@Tag(name = "监控API", description = "监控相关信息")
@RestController
@RequestMapping("/monitor")
@RequiredArgsConstructor
public class MonitorController extends BaseController {

    private final MonitorApplicationService monitorApplicationService;

    @Operation(summary = "Redis信息")
    @PreAuthorize("@permission.has('monitor:cache:list')")
    @GetMapping("/cacheInfo")
    public ResponseDTO<RedisCacheInfoDTO> getRedisCacheInfo() {
        RedisCacheInfoDTO redisCacheInfo = monitorApplicationService.getRedisCacheInfo();
        return ResponseDTO.ok(redisCacheInfo);
    }


    @Operation(summary = "服务器信息")
    @PreAuthorize("@permission.has('monitor:server:list')")
    @GetMapping("/serverInfo")
    public ResponseDTO<ServerInfo> getServerInfo() {
        ServerInfo serverInfo = monitorApplicationService.getServerInfo();
        return ResponseDTO.ok(serverInfo);
    }

    /**
     * 获取在线用户列表
     *
     * @param ipAddress ip地址
     * @param username 用户名
     * @return 分页处理后的在线用户信息
     */
    @Operation(summary = "在线用户列表")
    @PreAuthorize("@permission.has('monitor:online:list')")
    @GetMapping("/onlineUsers")
    public ResponseDTO<PageDTO<OnlineUserDTO>> onlineUsers(String ipAddress, String username) {
        List<OnlineUserDTO> onlineUserList = monitorApplicationService.getOnlineUserList(username, ipAddress);
        return ResponseDTO.ok(new PageDTO<>(onlineUserList));
    }

    /**
     * 强退用户
     */
    @Operation(summary = "强退用户")
    @PreAuthorize("@permission.has('monitor:online:forceLogout')")
    @AccessLog(title = "在线用户", businessType = BusinessTypeEnum.FORCE_LOGOUT)
    @DeleteMapping("/onlineUser/{tokenId}")
    public ResponseDTO<Void> logoutOnlineUser(@PathVariable String tokenId) {
        CacheCenter.loginUserCache.delete(tokenId);
        return ResponseDTO.ok();
    }


}
