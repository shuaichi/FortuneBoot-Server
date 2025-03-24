package com.fortuneboot.service.permission;

import com.fortuneboot.service.permission.model.AbstractDataPermissionChecker;
import com.fortuneboot.service.permission.model.checker.OnlySelfDataPermissionChecker;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.service.permission.model.checker.AllDataPermissionChecker;
import com.fortuneboot.service.permission.model.checker.DefaultDataPermissionChecker;
import com.fortuneboot.infrastructure.user.web.DataScopeEnum;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * 数据权限检测器工厂
 * @author valarchie
 */
@Component
public class DataPermissionCheckerFactory {
    private static AbstractDataPermissionChecker allChecker;
    private static AbstractDataPermissionChecker onlySelfChecker;
    private static AbstractDataPermissionChecker defaultSelfChecker;


    @PostConstruct
    public void initAllChecker() {
        allChecker = new AllDataPermissionChecker();
        onlySelfChecker = new OnlySelfDataPermissionChecker();
        defaultSelfChecker = new DefaultDataPermissionChecker();
    }


    public static AbstractDataPermissionChecker getChecker(SystemLoginUser loginUser) {
        DataScopeEnum dataScope = loginUser.getRoleInfo().getDataScope();
        switch (dataScope) {
            case ALL:
                return allChecker;
            case ONLY_SELF:
                return onlySelfChecker;
            default:
                return defaultSelfChecker;
        }
    }

}
