package com.fortuneboot.factory.system.factory;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.system.SysMenuEntity;
import com.fortuneboot.factory.system.model.MenuModel;
import com.fortuneboot.repository.system.SysMenuRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 菜单模型工厂
 * @author valarchie
 */
@Component
@RequiredArgsConstructor
public class MenuModelFactory {

    private final SysMenuRepo menuRepository;

    public MenuModel loadById(Long menuId) {
        SysMenuEntity byId = menuRepository.getById(menuId);
        if (byId == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, menuId, "菜单");
        }
        return new MenuModel(byId, menuRepository);
    }

    public MenuModel create() {
        return new MenuModel(menuRepository);
    }


}
