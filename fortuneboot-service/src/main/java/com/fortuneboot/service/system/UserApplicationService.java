package com.fortuneboot.service.system;

import cn.hutool.core.convert.Convert;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.common.StatusEnum;
import com.fortuneboot.common.enums.common.UserSourceEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.factory.system.UserModelFactory;
import com.fortuneboot.factory.system.model.UserModel;
import com.fortuneboot.repository.system.SysConfigRepository;
import com.fortuneboot.repository.system.SysRoleRepository;
import com.fortuneboot.service.cache.CacheCenter;
import com.fortuneboot.domain.common.command.BulkOperationCommand;
import com.fortuneboot.domain.common.dto.CurrentLoginUserDTO;
import com.fortuneboot.domain.dto.RoleDTO;
import com.fortuneboot.domain.command.user.AddUserCommand;
import com.fortuneboot.domain.command.user.ChangeStatusCommand;
import com.fortuneboot.domain.command.user.ResetPasswordCommand;
import com.fortuneboot.domain.command.user.UpdateProfileCommand;
import com.fortuneboot.domain.command.user.UpdateUserAvatarCommand;
import com.fortuneboot.domain.command.user.UpdateUserCommand;
import com.fortuneboot.domain.command.user.UpdateUserPasswordCommand;
import com.fortuneboot.domain.entity.system.SearchUserDO;
import com.fortuneboot.domain.dto.user.UserDTO;
import com.fortuneboot.domain.dto.user.UserDetailDTO;
import com.fortuneboot.domain.dto.user.UserProfileDTO;
import com.fortuneboot.domain.query.system.SearchUserQuery;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.domain.entity.system.SysRoleEntity;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import com.fortuneboot.repository.system.SysUserRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.stream.Collectors;

import com.fortuneboot.service.fortune.FortuneGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author valarchie
 */
@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final SysUserRepository userRepository;

    private final SysRoleRepository roleRepository;


    private final UserModelFactory userModelFactory;

    private final SysConfigRepository sysConfigRepository;


    public PageDTO<UserDTO> getUserList(SearchUserQuery<SearchUserDO> query) {
        Page<SearchUserDO> userPage = userRepository.getUserList(query);
        List<UserDTO> userDTOList = userPage.getRecords().stream().map(UserDTO::new).collect(Collectors.toList());
        return new PageDTO<>(userDTOList, userPage.getTotal());
    }

    public UserProfileDTO getUserProfile(Long userId) {

        SysUserEntity userEntity = userRepository.getById(userId);
        SysRoleEntity roleEntity = userRepository.getRoleOfUser(userId);

        return new UserProfileDTO(userEntity, roleEntity);
    }


    /**
     * 获取当前登录用户信息
     *
     * @return 当前登录用户信息
     */
    public CurrentLoginUserDTO getLoginUserInfo(SystemLoginUser loginUser) {
        CurrentLoginUserDTO permissionDTO = new CurrentLoginUserDTO();

        permissionDTO.setUserInfo(new UserDTO(CacheCenter.userCache.getObjectById(loginUser.getUserId())));
        permissionDTO.setRoleKey(loginUser.getRoleInfo().getRoleKey());
        permissionDTO.setPermissions(loginUser.getRoleInfo().getMenuPermissions());

        return permissionDTO;
    }


    public void updateUserProfile(UpdateProfileCommand command) {
        UserModel userModel = userModelFactory.loadById(command.getUserId());
        userModel.loadUpdateProfileCommand(command);

        userModel.checkPhoneNumberIsUnique();
        userModel.checkEmailIsUnique();

        userModel.updateById();

        CacheCenter.userCache.delete(userModel.getUserId());
    }

    public UserDetailDTO getUserDetailInfo(Long userId) {
        SysUserEntity userEntity = userRepository.getById(userId);
        UserDetailDTO detailDTO = new UserDetailDTO();

        LambdaQueryWrapper<SysRoleEntity> roleQuery = new LambdaQueryWrapper<SysRoleEntity>()
                .orderByAsc(SysRoleEntity::getRoleSort);
        List<RoleDTO> roleDtoList = roleRepository.list(roleQuery).stream().map(RoleDTO::new).collect(Collectors.toList());
        detailDTO.setRoleOptions(roleDtoList);

        if (userEntity != null) {
            detailDTO.setUser(new UserDTO(userEntity));
            detailDTO.setRoleId(userEntity.getRoleId());
        }
        return detailDTO;
    }

    public void addUser(AddUserCommand command) {
        UserModel model = userModelFactory.create();
        model.loadAddUserCommand(command);

        model.checkUsernameIsUnique();
        model.checkPhoneNumberIsUnique();
        model.checkEmailIsUnique();
        model.checkFieldRelatedEntityExist();
        model.resetPassword(command.getPassword());

        model.insert();
    }

    public void updateUser(UpdateUserCommand command) {
        UserModel model = userModelFactory.loadById(command.getUserId());
        model.loadUpdateUserCommand(command);

        model.checkPhoneNumberIsUnique();
        model.checkEmailIsUnique();
        model.checkFieldRelatedEntityExist();
        model.updateById();

        CacheCenter.userCache.delete(model.getUserId());
    }

    public void deleteUsers(SystemLoginUser loginUser, BulkOperationCommand<Long> command) {
        for (Long id : command.getIds()) {
            UserModel userModel = userModelFactory.loadById(id);
            userModel.checkCanBeDelete(loginUser);
            userModel.deleteById();
        }
    }

    public void updatePasswordBySelf(SystemLoginUser loginUser, UpdateUserPasswordCommand command) {
        UserModel userModel = userModelFactory.loadById(command.getUserId());
        userModel.modifyPassword(command);
        userModel.updateById();

        CacheCenter.userCache.delete(userModel.getUserId());
    }

    public void resetUserPassword(ResetPasswordCommand command) {
        UserModel userModel = userModelFactory.loadById(command.getUserId());

        userModel.resetPassword(command.getPassword());
        userModel.updateById();

        CacheCenter.userCache.delete(userModel.getUserId());
    }

    public void changeUserStatus(ChangeStatusCommand command) {
        UserModel userModel = userModelFactory.loadById(command.getUserId());

        userModel.setStatus(Convert.toInt(command.getStatus()));
        userModel.updateById();

        CacheCenter.userCache.delete(userModel.getUserId());
    }

    public void updateUserAvatar(UpdateUserAvatarCommand command) {
        UserModel userModel = userModelFactory.loadById(command.getUserId());

        userModel.setAvatar(command.getAvatar());
        userModel.updateById();

        CacheCenter.userCache.delete(userModel.getUserId());
    }


    public void register(AddUserCommand command) {
        String configValue = sysConfigRepository.getConfigValueByKey("sys.account.registerUser");
        boolean registerUser = Boolean.parseBoolean(configValue);
        if (registerUser) {
            command.setSource(UserSourceEnum.REGISTER.getValue());
            command.setStatus(StatusEnum.ENABLE.getValue());
            this.addUser(command);
        } else {
            throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION);
        }
    }

    public List<RoleDTO> getAllowRegisterRoles() {
        String configValue = sysConfigRepository.getConfigValueByKey("sys.account.registerUser");
        boolean registerUser = Boolean.parseBoolean(configValue);
        if (registerUser) {
            List<SysRoleEntity> roleEntityList = roleRepository.getAllowRegisterRoles();
            return roleEntityList.stream().map(RoleDTO::new).collect(Collectors.toList());
        }
        throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION);
    }
}
