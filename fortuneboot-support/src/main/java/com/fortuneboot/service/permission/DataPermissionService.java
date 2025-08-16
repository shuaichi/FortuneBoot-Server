package com.fortuneboot.service.permission;

import cn.hutool.core.collection.CollUtil;
import com.fortuneboot.service.permission.model.AbstractDataPermissionChecker;
import com.fortuneboot.service.permission.model.DataCondition;
import com.fortuneboot.infrastructure.user.AuthenticationUtils;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import com.fortuneboot.repository.system.SysUserRepo;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 数据权限校验服务
 * @author valarchie
 */
@Service("dataScope")
@RequiredArgsConstructor
public class DataPermissionService {

    private final SysUserRepo userService;

    /**
     * 通过userId 校验当前用户 对 目标用户是否有操作权限
     *
     * @param userId 用户id
     * @return 检验结果
     */
    public boolean checkUserId(Long userId) {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        SysUserEntity targetUser = userService.getById(userId);
        if (targetUser == null) {
            return true;
        }
        return checkDataScope(loginUser, userId);
    }

    /**
     * 通过userId 校验当前用户 对 目标用户是否有操作权限
     * @param userIds 用户id列表
     * @return 校验结果
     */
    public boolean checkUserIds(List<Long> userIds) {
        if (CollUtil.isNotEmpty(userIds)) {
            for (Long userId : userIds) {
                boolean checkResult = checkUserId(userId);
                if (!checkResult) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkDataScope(SystemLoginUser loginUser, Long targetUserId) {
        DataCondition dataCondition = DataCondition.builder().targetUserId(targetUserId).build();
        AbstractDataPermissionChecker checker = DataPermissionCheckerFactory.getChecker(loginUser);
        return checker.check(loginUser, dataCondition);
    }



}
