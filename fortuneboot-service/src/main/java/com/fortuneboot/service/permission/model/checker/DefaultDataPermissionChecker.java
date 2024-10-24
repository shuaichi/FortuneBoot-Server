package com.fortuneboot.service.permission.model.checker;

import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.service.permission.model.AbstractDataPermissionChecker;
import com.fortuneboot.service.permission.model.DataCondition;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据权限测试接口
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DefaultDataPermissionChecker extends AbstractDataPermissionChecker {

    @Override
    public boolean check(SystemLoginUser loginUser, DataCondition condition) {
        return false;
    }

}
