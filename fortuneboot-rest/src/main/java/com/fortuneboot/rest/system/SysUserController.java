package com.fortuneboot.rest.system;

import cn.hutool.core.collection.ListUtil;
import com.fortuneboot.common.core.validation.AddValidation;
import com.fortuneboot.common.core.base.BaseController;
import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.common.UserSourceEnum;
import com.fortuneboot.common.utils.poi.CustomExcelUtil;
import com.fortuneboot.customize.accessLog.AccessLog;
import com.fortuneboot.domain.common.command.BulkOperationCommand;
import com.fortuneboot.service.system.UserApplicationService;
import com.fortuneboot.domain.command.user.AddUserCommand;
import com.fortuneboot.domain.command.user.ChangeStatusCommand;
import com.fortuneboot.domain.command.user.ResetPasswordCommand;
import com.fortuneboot.domain.command.user.UpdateUserCommand;
import com.fortuneboot.domain.dto.user.UserDTO;
import com.fortuneboot.domain.dto.user.UserDetailDTO;
import com.fortuneboot.domain.query.system.SearchUserQuery;
import com.fortuneboot.infrastructure.user.AuthenticationUtils;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import com.fortuneboot.domain.entity.system.SearchUserDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户信息
 * @author valarchie
 */
@Tag(name = "用户API", description = "用户相关的增删查改")
@RestController
@RequestMapping("/system/users")
@RequiredArgsConstructor
public class SysUserController extends BaseController {

    private final UserApplicationService userApplicationService;

    /**
     * 获取用户列表
     */
    @Operation(summary = "用户列表")
    @PreAuthorize("@permission.has('system:user:list')")
    @GetMapping
    public ResponseDTO<PageDTO<UserDTO>> userList(SearchUserQuery<SearchUserDO> query) {
        PageDTO<UserDTO> page = userApplicationService.getUserList(query);
        return ResponseDTO.ok(page);
    }

    @Operation(summary = "用户列表导出")
    @AccessLog(title = "用户管理", businessType = BusinessTypeEnum.EXPORT)
    @PreAuthorize("@permission.has('system:user:export')")
    @GetMapping("/excel")
    public void exportUserByExcel(HttpServletResponse response, SearchUserQuery<SearchUserDO> query) {
        PageDTO<UserDTO> userList = userApplicationService.getUserList(query);
        CustomExcelUtil.writeToResponse(userList.getRows(), UserDTO.class, response);
    }

    @Operation(summary = "用户列表导入")
    @AccessLog(title = "用户管理", businessType = BusinessTypeEnum.IMPORT)
    @PreAuthorize("@permission.has('system:user:import')")
    @PostMapping("/excel")
    public ResponseDTO<Void> importUserByExcel(MultipartFile file) {
        List<AddUserCommand> commands = CustomExcelUtil.readFromRequest(AddUserCommand.class, file);

        for (AddUserCommand command : commands) {
            command.setSource(UserSourceEnum.ADMIN_IMPORT.getValue());
            userApplicationService.addUser(command);
        }
        return ResponseDTO.ok();
    }

    /**
     * 下载批量导入模板
     */
    @Operation(summary = "用户导入excel下载")
    @GetMapping("/excelTemplate")
    public void downloadExcelTemplate(HttpServletResponse response) {
        CustomExcelUtil.writeToResponse(ListUtil.toList(new AddUserCommand()), AddUserCommand.class, response);
    }

    /**
     * 根据用户编号获取详细信息
     */
    @Operation(summary = "用户详情")
    @PreAuthorize("@permission.has('system:user:query')")
    @GetMapping("/{userId}")
    public ResponseDTO<UserDetailDTO> getUserDetailInfo(@PathVariable(value = "userId", required = false) Long userId) {
        UserDetailDTO userDetailInfo = userApplicationService.getUserDetailInfo(userId);
        return ResponseDTO.ok(userDetailInfo);
    }

    /**
     * 新增用户
     */
    @Operation(summary = "新增用户")
    @PreAuthorize("@permission.has('system:user:add')")
    @AccessLog(title = "用户管理", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@Validated(AddValidation.class) @RequestBody AddUserCommand command) {
        command.setSource(UserSourceEnum.ADMIN_ADD.getValue());
        userApplicationService.addUser(command);
        return ResponseDTO.ok();
    }

    /**
     * 修改用户
     */
    @Operation(summary = "修改用户")
    @PreAuthorize("@permission.has('system:user:edit') AND @dataScope.checkUserId(#command?.userId)")
    @AccessLog(title = "用户管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{userId}")
    public ResponseDTO<Void> edit(@Validated @RequestBody UpdateUserCommand command) {
        userApplicationService.updateUser(command);
        return ResponseDTO.ok();
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户")
    @PreAuthorize("@permission.has('system:user:remove') AND @dataScope.checkUserIds(#userIds)")
    @AccessLog(title = "用户管理", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/{userIds}")
    public ResponseDTO<Void> remove(@PathVariable List<Long> userIds) {
        BulkOperationCommand<Long> bulkDeleteCommand = new BulkOperationCommand<>(userIds);
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        userApplicationService.deleteUsers(loginUser, bulkDeleteCommand);
        return ResponseDTO.ok();
    }

    /**
     * 重置密码
     */
    @Operation(summary = "重置用户密码")
    @PreAuthorize("@permission.has('system:user:resetPwd') AND @dataScope.checkUserId(#userId)")
    @AccessLog(title = "用户管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{userId}/password")
    public ResponseDTO<Void> resetPassword(@PathVariable Long userId, @RequestBody ResetPasswordCommand command) {
        command.setUserId(userId);
        userApplicationService.resetUserPassword(command);
        return ResponseDTO.ok();
    }

    /**
     * 状态修改
     */
    @Operation(summary = "修改用户状态")
    @PreAuthorize("@permission.has('system:user:edit') AND @dataScope.checkUserId(#command?.userId)")
    @AccessLog(title = "用户管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{userId}/status")
    public ResponseDTO<Void> changeStatus(@PathVariable Long userId, @RequestBody ChangeStatusCommand command) {
        command.setUserId(userId);
        userApplicationService.changeUserStatus(command);
        return ResponseDTO.ok();
    }


}
