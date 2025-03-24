package com.fortuneboot.service.permission.model.checker;

import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.service.permission.model.AbstractDataPermissionChecker;
import com.fortuneboot.service.permission.model.DataCondition;
import java.util.Objects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 数据权限测试接口
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class OnlySelfDataPermissionChecker extends AbstractDataPermissionChecker {

    @Override
    public boolean check(SystemLoginUser loginUser, DataCondition condition) {
        if (condition == null || loginUser == null) {
            return false;
        }

        if (loginUser.getUserId() == null || condition.getTargetUserId() == null) {
            return false;
        }

        Long currentUserId = loginUser.getUserId();
        Long targetUserId = condition.getTargetUserId();
        return Objects.equals(currentUserId, targetUserId);
    }

}
