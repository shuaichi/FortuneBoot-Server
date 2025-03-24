package com.fortuneboot.factory.system.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.fortuneboot.common.config.FortuneBootConfig;
import com.fortuneboot.common.enums.common.UserSourceEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode.Business;
import com.fortuneboot.domain.command.user.AddUserCommand;
import com.fortuneboot.domain.command.user.UpdateProfileCommand;
import com.fortuneboot.domain.command.user.UpdateUserCommand;
import com.fortuneboot.domain.command.user.UpdateUserPasswordCommand;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import com.fortuneboot.factory.system.RoleModelFactory;
import com.fortuneboot.infrastructure.user.AuthenticationUtils;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.repository.system.SysUserRepository;
import java.util.Objects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class UserModel extends SysUserEntity {

    private SysUserRepository userRepository;

    private RoleModelFactory roleModelFactory;

    public UserModel(SysUserEntity entity, SysUserRepository userRepository, RoleModelFactory roleModelFactory) {
        this(userRepository, roleModelFactory);

        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    public UserModel(SysUserRepository userRepository, RoleModelFactory roleModelFactory) {
        this.userRepository = userRepository;
        this.roleModelFactory = roleModelFactory;
    }

    public void loadAddUserCommand(AddUserCommand command) {
        if (command != null) {
            BeanUtil.copyProperties(command, this, "userId");
        }
    }


    public void loadUpdateUserCommand(UpdateUserCommand command) {
        if (command != null) {
            loadAddUserCommand(command);
        }
    }

    public void loadUpdateProfileCommand(UpdateProfileCommand command) {
        if (command != null) {
            this.setSex(command.getSex());
            this.setNickname(command.getNickName());
            this.setPhoneNumber(command.getPhoneNumber());
            this.setEmail(command.getEmail());
        }
    }


    public void checkUsernameIsUnique() {
        if (userRepository.isUserNameDuplicated(getUsername())) {
            throw new ApiException(Business.USER_NAME_IS_NOT_UNIQUE);
        }
    }

    public void checkPhoneNumberIsUnique() {
        if (StrUtil.isNotEmpty(getPhoneNumber()) && userRepository.isPhoneDuplicated(getPhoneNumber(),
            getUserId())) {
            throw new ApiException(Business.USER_PHONE_NUMBER_IS_NOT_UNIQUE);
        }
    }

    public void checkFieldRelatedEntityExist() {
        if (getRoleId() != null) {
            RoleModel roleModel = roleModelFactory.loadById(getRoleId());
            UserSourceEnum source = UserSourceEnum.getByValue(this.getSource());
            // 不同角色来源，做不同的操作
            switch (source){
                case REGISTER:
                    if (!roleModel.getAllowRegister()) {
                        throw new ApiException(Business.USER_ROLE_NOT_ALLOW_REGISTER);
                    }
                    break;

                case ADMIN_ADD:
                case ADMIN_IMPORT:
                    break;

                case null:
                default:
                    throw new ApiException(Business.USER_ADD_SOURCE_ILLEGALITY);
            }
        }
    }


    public void checkEmailIsUnique() {
        if (StrUtil.isNotEmpty(getEmail()) && userRepository.isEmailDuplicated(getEmail(), getUserId())) {
            throw new ApiException(Business.USER_EMAIL_IS_NOT_UNIQUE);
        }
    }

    public void checkCanBeDelete(SystemLoginUser loginUser) {
        if (Objects.equals(getUserId(), loginUser.getUserId())
            || this.getIsAdmin()) {
            throw new ApiException(Business.USER_CURRENT_USER_CAN_NOT_BE_DELETE);
        }
    }


    public void modifyPassword(UpdateUserPasswordCommand command) {
        if (!AuthenticationUtils.matchesPassword(command.getOldPassword(), getPassword())) {
            throw new ApiException(Business.USER_PASSWORD_IS_NOT_CORRECT);
        }

        if (AuthenticationUtils.matchesPassword(command.getNewPassword(), getPassword())) {
            throw new ApiException(Business.USER_NEW_PASSWORD_IS_THE_SAME_AS_OLD);
        }
        setPassword(AuthenticationUtils.encryptPassword(command.getNewPassword()));
    }

    public void resetPassword(String newPassword) {
        setPassword(AuthenticationUtils.encryptPassword(newPassword));
    }

    @Override
    public boolean updateById() {
        if (this.getIsAdmin() && FortuneBootConfig.isDemoEnabled()) {
            throw new ApiException(Business.USER_ADMIN_CAN_NOT_BE_MODIFY);
        }
       return super.updateById();
    }
}
