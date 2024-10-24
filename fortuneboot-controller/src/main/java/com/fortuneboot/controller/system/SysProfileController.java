package com.fortuneboot.controller.system;

import com.fortuneboot.common.constant.Constants.UploadSubDir;
import com.fortuneboot.common.core.base.BaseController;
import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.common.utils.file.FileUploadUtils;
import com.fortuneboot.customize.accessLog.AccessLog;
import com.fortuneboot.domain.common.dto.UploadFileDTO;
import com.fortuneboot.service.system.UserApplicationService;
import com.fortuneboot.domain.command.user.UpdateProfileCommand;
import com.fortuneboot.domain.command.user.UpdateUserAvatarCommand;
import com.fortuneboot.domain.command.user.UpdateUserPasswordCommand;
import com.fortuneboot.domain.dto.user.UserProfileDTO;
import com.fortuneboot.infrastructure.user.AuthenticationUtils;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人信息 业务处理
 *
 * @author ruoyi
 */
@Tag(name = "个人信息API", description = "个人信息相关接口")
@RestController
@RequestMapping("/system/user/profile")
@RequiredArgsConstructor
public class SysProfileController extends BaseController {

    private final UserApplicationService userApplicationService;

    /**
     * 个人信息
     */
    @Operation(summary = "获取个人信息")
    @GetMapping
    public ResponseDTO<UserProfileDTO> profile() {
        SystemLoginUser user = AuthenticationUtils.getSystemLoginUser();
        UserProfileDTO userProfile = userApplicationService.getUserProfile(user.getUserId());
        return ResponseDTO.ok(userProfile);
    }

    /**
     * 修改用户
     */
    @Operation(summary = "修改个人信息")
    @AccessLog(title = "个人信息", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping
    public ResponseDTO<Void> updateProfile(@RequestBody UpdateProfileCommand command) {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        command.setUserId(loginUser.getUserId());
        userApplicationService.updateUserProfile(command);
        return ResponseDTO.ok();
    }

    /**
     * 重置密码
     */
    @Operation(summary = "重置个人密码")
    @AccessLog(title = "个人信息", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/password")
    public ResponseDTO<Void> updatePassword(@RequestBody UpdateUserPasswordCommand command) {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        command.setUserId(loginUser.getUserId());
        userApplicationService.updatePasswordBySelf(loginUser, command);
        return ResponseDTO.ok();
    }

    /**
     * 头像上传
     */
    @Operation(summary = "修改个人头像")
    @AccessLog(title = "用户头像", businessType = BusinessTypeEnum.MODIFY)
    @PostMapping("/avatar")
    public ResponseDTO<UploadFileDTO> avatar(@RequestParam("avatarfile") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ApiException(ErrorCode.Business.USER_UPLOAD_FILE_FAILED);
        }
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        String avatarUrl = FileUploadUtils.upload(UploadSubDir.AVATAR_PATH, file);

        userApplicationService.updateUserAvatar(new UpdateUserAvatarCommand(loginUser.getUserId(), avatarUrl));
        return ResponseDTO.ok(new UploadFileDTO(avatarUrl));
    }
}
