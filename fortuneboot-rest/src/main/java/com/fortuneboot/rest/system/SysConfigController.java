package com.fortuneboot.rest.system;

import com.fortuneboot.common.core.base.BaseController;
import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.customize.accessLog.AccessLog;
import com.fortuneboot.domain.command.system.ConfigAddCommand;
import com.fortuneboot.domain.dto.configKeyDTO;
import com.fortuneboot.service.cache.CacheCenter;
import com.fortuneboot.service.system.ConfigApplicationService;
import com.fortuneboot.domain.command.system.ConfigUpdateCommand;
import com.fortuneboot.domain.dto.ConfigDTO;
import com.fortuneboot.domain.query.system.ConfigQuery;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 参数配置 信息操作处理
 * @author valarchie
 */
@RestController
@RequestMapping("/system")
@Validated
@RequiredArgsConstructor
@Tag(name = "配置API", description = "配置相关的增删查改")
public class SysConfigController extends BaseController {

    private final ConfigApplicationService configApplicationService;

    /**
     * 获取参数配置列表
     */
    @Operation(summary = "参数列表", description = "分页获取配置参数列表")
    @PreAuthorize("@permission.has('system:config:list')")
    @GetMapping("/configs")
    public ResponseDTO<PageDTO<ConfigDTO>> list(ConfigQuery query) {
        PageDTO<ConfigDTO> page = configApplicationService.getConfigList(query);
        return ResponseDTO.ok(page);
    }

    /**
     * 根据参数编号获取详细信息
     */
    @PreAuthorize("@permission.has('system:config:query')")
    @GetMapping(value = "/config/{configId}")
    @Operation(summary = "配置信息", description = "配置的详细信息")
    public ResponseDTO<ConfigDTO> getInfo(@NotNull @Positive @PathVariable Long configId) {
        ConfigDTO config = configApplicationService.getConfigInfo(configId);
        return ResponseDTO.ok(config);
    }


    /**
     * 修改参数配置
     */
    @PreAuthorize("@permission.has('system:config:edit')")
    @AccessLog(title = "参数管理", businessType = BusinessTypeEnum.MODIFY)
    @Operation(summary = "配置修改", description = "配置修改")
    @PutMapping(value = "/config/{configId}")
    public ResponseDTO<Void> edit(@NotNull @Positive @PathVariable Long configId, @RequestBody ConfigUpdateCommand config) {
        config.setConfigId(configId);
        configApplicationService.updateConfig(config);
        return ResponseDTO.ok();
    }

    /**
     * 刷新参数缓存
     */
    @Operation(summary = "刷新配置缓存")
    @PreAuthorize("@permission.has('system:config:remove')")
    @AccessLog(title = "参数管理", businessType = BusinessTypeEnum.CLEAN)
    @DeleteMapping("/configs/cache")
    public ResponseDTO<Void> refreshCache() {
        CacheCenter.configCache.invalidateAll();
        return ResponseDTO.ok();
    }

    /**
     * 获取参数设置枚举列表
     */
    @Operation(summary = "参数设置枚举列表", description = "分页获取配置参数枚举列表")
    @PreAuthorize("@permission.has('system:config:list')")
    @GetMapping("/config/getSystemConfigOptions")
    public ResponseDTO<List<configKeyDTO>> getSystemConfigOptions() {
        List<configKeyDTO> list = configApplicationService.getSystemConfigOptions();
        return ResponseDTO.ok(list);
    }

    @Operation(summary = "新增参数")
    @PreAuthorize("@permission.has('system:config:add')")
    @AccessLog(title = "参数管理", businessType = BusinessTypeEnum.ADD)
    @PostMapping("/config/addSystemConfig")
    public ResponseDTO<Void> addSystemConfig(@RequestBody ConfigAddCommand configAddCommand)  {
        configApplicationService.addSystemConfig(configAddCommand);
        return ResponseDTO.ok();
    }

    /**
     * 删除参数
     */
    @Operation(summary = "删除参数")
    @PreAuthorize("@permission.has('system:config:remove')")
    @AccessLog(title = "参数管理", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/config/{configId}")
    public ResponseDTO<Void> deleteSystemConfig(@NotNull @PathVariable("configId") Long configId) {
        configApplicationService.deleteSystemConfig(configId);
        return ResponseDTO.ok();
    }


}
