package com.fortuneboot.rest.system;

import com.fortuneboot.common.core.base.BaseController;
import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.utils.poi.CustomExcelUtil;
import com.fortuneboot.customize.accessLog.AccessLog;
import com.fortuneboot.service.system.RoleApplicationService;
import com.fortuneboot.domain.command.system.AddRoleCommand;
import com.fortuneboot.domain.command.system.UpdateDataScopeCommand;
import com.fortuneboot.domain.command.system.UpdateRoleCommand;
import com.fortuneboot.domain.command.system.UpdateStatusCommand;
import com.fortuneboot.domain.dto.RoleDTO;
import com.fortuneboot.domain.query.system.AllocatedRoleQuery;
import com.fortuneboot.domain.query.system.RoleQuery;
import com.fortuneboot.domain.query.system.UnallocatedRoleQuery;
import com.fortuneboot.domain.dto.user.UserDTO;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
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

/**
 * 角色信息
 *
 * @author valarchie
 */
@Tag(name = "角色API", description = "角色相关的增删查改")
@RestController
@RequestMapping("/system/role")
@Validated
@RequiredArgsConstructor
public class SysRoleController extends BaseController {

    private final RoleApplicationService roleApplicationService;

    @Operation(summary = "角色列表")
    @PreAuthorize("@permission.has('system:role:list')")
    @GetMapping("/list")
    public ResponseDTO<PageDTO<RoleDTO>> list(RoleQuery query) {
        PageDTO<RoleDTO> pageDTO = roleApplicationService.getRoleList(query);
        return ResponseDTO.ok(pageDTO);
    }

    @Operation(summary = "角色列表导出")
    @AccessLog(title = "角色管理", businessType = BusinessTypeEnum.EXPORT)
    @PreAuthorize("@permission.has('system:role:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, RoleQuery query) {
        PageDTO<RoleDTO> pageDTO = roleApplicationService.getRoleList(query);
        CustomExcelUtil.writeToResponse(pageDTO.getRows(), RoleDTO.class, response);
    }

    /**
     * 根据角色编号获取详细信息
     */
    @Operation(summary = "角色详情")
    @PreAuthorize("@permission.has('system:role:query')")
    @GetMapping(value = "/{roleId}")
    public ResponseDTO<RoleDTO> getInfo(@PathVariable @NotNull Long roleId) {
        RoleDTO roleInfo = roleApplicationService.getRoleInfo(roleId);
        return ResponseDTO.ok(roleInfo);
    }

    /**
     * 新增角色
     */
    @Operation(summary = "添加角色")
    @PreAuthorize("@permission.has('system:role:add')")
    @AccessLog(title = "角色管理", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@RequestBody AddRoleCommand addCommand) {
        roleApplicationService.addRole(addCommand);
        return ResponseDTO.ok();
    }

    /**
     * 移除角色
     */
    @Operation(summary = "删除角色")
    @PreAuthorize("@permission.has('system:role:remove')")
    @AccessLog(title = "角色管理", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping(value = "/{roleId}")
    public ResponseDTO<Void> remove(@PathVariable("roleId") List<Long> roleIds) {
        roleApplicationService.deleteRoleByBulk(roleIds);
        return ResponseDTO.ok();
    }

    /**
     * 修改保存角色
     */
    @Operation(summary = "修改角色")
    @PreAuthorize("@permission.has('system:role:edit')")
    @AccessLog(title = "角色管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping
    public ResponseDTO<Void> edit(@Validated @RequestBody UpdateRoleCommand updateCommand) {
        roleApplicationService.updateRole(updateCommand);
        return ResponseDTO.ok();
    }

    /**
     * 修改保存数据权限
     */
    @Operation(summary = "修改角色数据权限")
    @PreAuthorize("@permission.has('system:role:edit')")
    @AccessLog(title = "角色管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{roleId}/dataScope")
    public ResponseDTO<Void> dataScope(@PathVariable("roleId") Long roleId,
        @RequestBody UpdateDataScopeCommand command) {
        command.setRoleId(roleId);

        roleApplicationService.updateDataScope(command);
        return ResponseDTO.ok();
    }

    /**
     * 角色状态修改
     */
    @Operation(summary = "修改角色状态")
    @PreAuthorize("@permission.has('system:role:edit')")
    @AccessLog(title = "角色管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{roleId}/status")
    public ResponseDTO<Void> changeStatus(@PathVariable("roleId") Long roleId,
        @RequestBody UpdateStatusCommand command) {
        command.setRoleId(roleId);

        roleApplicationService.updateStatus(command);
        return ResponseDTO.ok();
    }


    /**
     * 查询已分配用户角色列表
     */
    @Operation(summary = "已关联该角色的用户列表")
    @PreAuthorize("@permission.has('system:role:list')")
    @GetMapping("/{roleId}/allocated/list")
    public ResponseDTO<PageDTO<UserDTO>> allocatedUserList(@PathVariable("roleId") Long roleId,
        AllocatedRoleQuery query) {
        query.setRoleId(roleId);
        PageDTO<UserDTO> page = roleApplicationService.getAllocatedUserList(query);
        return ResponseDTO.ok(page);
    }

    /**
     * 查询未分配用户角色列表
     */
    @Operation(summary = "未关联该角色的用户列表")
    @PreAuthorize("@permission.has('system:role:list')")
    @GetMapping("/{roleId}/unallocated/list")
    public ResponseDTO<PageDTO<UserDTO>> unallocatedUserList(@PathVariable("roleId") Long roleId,
        UnallocatedRoleQuery query) {
        query.setRoleId(roleId);
        PageDTO<UserDTO> page = roleApplicationService.getUnallocatedUserList(query);
        return ResponseDTO.ok(page);
    }


    /**
     * 批量取消授权用户
     */
    @Operation(summary = "批量解除角色和用户的关联")
    @PreAuthorize("@permission.has('system:role:edit')")
    @AccessLog(title = "角色管理", businessType = BusinessTypeEnum.GRANT)
    @DeleteMapping("/users/{userIds}/grant/bulk")
    public ResponseDTO<Void> deleteRoleOfUserByBulk(@PathVariable("userIds") List<Long> userIds) {
        roleApplicationService.deleteRoleOfUserByBulk(userIds);
        return ResponseDTO.ok();
    }

    /**
     * 批量选择用户授权
     */
    @Operation(summary = "批量添加用户和角色关联")
    @PreAuthorize("@permission.has('system:role:edit')")
    @AccessLog(title = "角色管理", businessType = BusinessTypeEnum.GRANT)
    @PostMapping("/{roleId}/users/{userIds}/grant/bulk")
    public ResponseDTO<Void> addRoleForUserByBulk(@PathVariable("roleId") Long roleId,
        @PathVariable("userIds") List<Long> userIds) {
        roleApplicationService.addRoleOfUserByBulk(roleId, userIds);
        return ResponseDTO.ok();
    }

}
