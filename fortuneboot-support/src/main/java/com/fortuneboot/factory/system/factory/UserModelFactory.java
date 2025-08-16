package com.fortuneboot.factory.system.factory;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import com.fortuneboot.repository.system.SysUserRepository;
import com.fortuneboot.factory.system.model.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户模型工厂
 * @author valarchie
 */
@Component
@RequiredArgsConstructor
public class UserModelFactory {

    private final SysUserRepository userRepository;

    private final RoleModelFactory roleModelFactory;

    public UserModel loadById(Long userId) {
        SysUserEntity byId = userRepository.getById(userId);
        if (byId == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, userId, "用户");
        }
        return new UserModel(byId, userRepository, roleModelFactory);
    }

    public UserModel create() {
        return new UserModel(userRepository, roleModelFactory);
    }

}
