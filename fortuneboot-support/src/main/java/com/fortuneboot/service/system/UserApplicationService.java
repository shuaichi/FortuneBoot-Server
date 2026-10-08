package com.fortuneboot.service.system;

import cn.hutool.core.convert.Convert;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.common.ConfigKeyEnum;
import com.fortuneboot.common.enums.fortune.RoleTypeEnum;
import com.fortuneboot.common.enums.common.TrueFalseEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.user.DeleteAccountCommand;
import com.fortuneboot.domain.entity.fortune.FortuneUserGroupRelationEntity;
import com.fortuneboot.domain.event.UserRegisteredEvent;
import com.fortuneboot.factory.system.factory.UserModelFactory;
import com.fortuneboot.factory.system.model.UserModel;
import com.fortuneboot.repository.fortune.FortuneUserGroupRelationRepo;
import com.fortuneboot.service.fortune.FortuneGroupService;
import com.fortuneboot.service.login.LoginService;
import com.fortuneboot.repository.system.SysConfigRepo;
import com.fortuneboot.repository.system.SysRoleRepo;
import com.fortuneboot.service.cache.CacheCenter;
import com.fortuneboot.service.cache.CacheService;
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
import com.fortuneboot.repository.system.SysUserRepo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author valarchie
 */
@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final SysUserRepo userRepository;

    private final SysRoleRepo roleRepository;


    private final UserModelFactory userModelFactory;

    private final SysConfigRepo sysConfigRepo;

    private final ApplicationEventPublisher eventPublisher;

    private final CacheService cacheService;

    private final FortuneUserGroupRelationRepo fortuneUserGroupRelationRepo;

    private final FortuneGroupService fortuneGroupService;

    private final LoginService loginService;

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

    /**
     * 当前登录用户自助注销账号
     * 1. 校验密码 防止误操作或共用设备上的恶意注销
     * 2. 清理分组关系: 仅剩本人时删除整个分组 否则移除本人关系并将 OWNER 移交给其他成员
     * 3. 匿名化个人信息并逻辑删除用户
     * 4. 清理用户缓存和全部登录会话
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccountBySelf(SystemLoginUser loginUser, DeleteAccountCommand command) {
        UserModel userModel = userModelFactory.loadById(loginUser.getUserId());

        // 客户端与登录一致使用 RSA 加密传输密码 服务端解密后再校验
        String rawPassword = loginService.decryptPassword(command.getPassword());
        userModel.checkPassword(rawPassword);
        userModel.checkSelfDeletionAllowed();

        cleanupUserGroups(loginUser.getUserId());

        userModel.anonymizeForDeletion();
        userModel.updateById();
        userModel.deleteById();

        CacheCenter.userCache.delete(loginUser.getUserId());
        cacheService.removeLoginUserByUserId(loginUser.getUserId());
    }

    private void cleanupUserGroups(Long userId) {
        List<FortuneUserGroupRelationEntity> relations = fortuneUserGroupRelationRepo.getByUserId(userId);

        for (FortuneUserGroupRelationEntity relation : relations) {
            List<FortuneUserGroupRelationEntity> members = fortuneUserGroupRelationRepo.getByGroupId(relation.getGroupId());

            boolean soleMember = members.size() == 1
                    && Objects.equals(userId, members.get(0).getUserId());

            if (soleMember) {
                // 仅本人使用的分组 直接删除分组及其账本等关联数据
                fortuneGroupService.remove(relation.getGroupId());
                continue;
            }

            // 共享分组保留给其他成员 如本人是 OWNER 则将权限移交给其他成员 避免分组无管理者
            members.stream()
                    .filter(member -> !Objects.equals(userId, member.getUserId()))
                    .findFirst()
                    .ifPresent(member -> {
                        member.setRoleType(RoleTypeEnum.OWNER.getValue());
                        fortuneUserGroupRelationRepo.updateById(member);
                    });

            fortuneUserGroupRelationRepo.removeById(relation.getUserGroupRelationId());
        }
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

    @Transactional(rollbackFor = Exception.class)
    public void addUser(AddUserCommand command) {
        SysRoleEntity role = roleRepository.getById(command.getRoleId());
        UserModel model = userModelFactory.create();

        // 1. 加载数据
        model.loadAddUserCommand(command);
        model.setIsAdmin(role.getIsAdmin());

        // 2. 校验数据
        model.checkUsernameIsUnique();
        model.checkPhoneNumberIsUnique();
        model.checkEmailIsUnique();
        model.checkAddFieldRelatedEntityExist();
        model.resetPassword(command.getPassword());

        // 3. 新增用户入库
        model.insert();

        // 4. 发布用户注册成功事件
        // 注意：此处不会阻塞，真实的异步逻辑会在当前事务 commit 之后，由我们的自定义线程池接管执行
        UserRegisteredEvent registeredEvent = new UserRegisteredEvent(
                model.getUserId(),
                model.getUsername(),
                model.getNickname()
        );
        eventPublisher.publishEvent(registeredEvent);
    }

    public void updateUser(UpdateUserCommand command) {
        SysRoleEntity role = roleRepository.getById(command.getRoleId());
        UserModel model = userModelFactory.loadById(command.getUserId());
        model.setIsAdmin(role.getIsAdmin());
        command.setSource(model.getSource());

        model.loadUpdateUserCommand(command);
        model.checkPhoneNumberIsUnique();
        model.checkEmailIsUnique();
        model.checkModifyFieldRelatedEntityExist();
        model.updateById();

        CacheCenter.userCache.delete(model.getUserId());
        // 角色/权限变更，强制使该用户的登录会话失效，权限立即生效
        cacheService.removeLoginUserByUserId(model.getUserId());
    }

    public void deleteUsers(SystemLoginUser loginUser, BulkOperationCommand<Long> command) {
        for (Long id : command.getIds()) {
            UserModel userModel = userModelFactory.loadById(id);
            userModel.checkCanBeDelete(loginUser);
            userModel.deleteById();
            // 用户被删除，同步清除其登录会话
            cacheService.removeLoginUserByUserId(id);
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
        // 用户状态变更（禁用），强制使其登录会话失效
        cacheService.removeLoginUserByUserId(userModel.getUserId());
    }

    public void updateUserAvatar(UpdateUserAvatarCommand command) {
        UserModel userModel = userModelFactory.loadById(command.getUserId());

        userModel.setAvatar(command.getAvatar());
        userModel.updateById();

        CacheCenter.userCache.delete(userModel.getUserId());
    }

    public List<RoleDTO> getAllowRegisterRoles() {
        String configValue = sysConfigRepo.getConfigValueByKey(ConfigKeyEnum.REGISTER.getValue());
        boolean registerUser = Boolean.parseBoolean(configValue);
        if (registerUser) {
            List<SysRoleEntity> roleEntityList = roleRepository.getAllowRegisterRoles();
            return roleEntityList.stream().map(RoleDTO::new).collect(Collectors.toList());
        }
        throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION);
    }

    public Boolean checkRepeat(String userName) {
        SysUserEntity user = userRepository.getUserByUserName(userName);
        return Objects.nonNull(user);
    }

    public String getIcp() {
        return sysConfigRepo.getConfigValueByKey(ConfigKeyEnum.ICP.getValue());
    }

    public String getDisplayConfig() {
        String display = sysConfigRepo.getConfigValueByKey(ConfigKeyEnum.DISPLAY.getValue());
        return StringUtils.isBlank(display) ? TrueFalseEnum.TRUE.getDescription() : display;
    }
}
