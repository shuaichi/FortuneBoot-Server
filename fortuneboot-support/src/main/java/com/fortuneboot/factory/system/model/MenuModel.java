package com.fortuneboot.factory.system.model;


import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.enums.common.MenuTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.common.utils.jackson.JacksonUtil;
import com.fortuneboot.domain.command.system.AddMenuCommand;
import com.fortuneboot.domain.command.system.UpdateMenuCommand;
import com.fortuneboot.domain.entity.system.SysMenuEntity;
import com.fortuneboot.repository.system.SysMenuRepo;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @author valarchie
 */
@NoArgsConstructor
public class MenuModel extends SysMenuEntity {

    private SysMenuRepo menuRepository;

    public MenuModel(SysMenuRepo menuRepository) {
        this.menuRepository = menuRepository;
    }

    public MenuModel(SysMenuEntity entity, SysMenuRepo menuRepository) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
        this.menuRepository = menuRepository;
    }

    public void loadAddCommand(AddMenuCommand command) {
        if (command != null) {
            BeanUtil.copyProperties(command, this, "menuId");

            String metaInfoStr = JacksonUtil.to(command.getMeta());
            this.setMetaInfo(metaInfoStr);
        }
    }

    public void loadUpdateCommand(UpdateMenuCommand command) {
        if (command != null) {
            if (!Objects.equals(this.getMenuType(), command.getMenuType()) && !this.getIsButton()) {
                throw new ApiException(ErrorCode.Business.MENU_CAN_NOT_CHANGE_MENU_TYPE);
            }
            loadAddCommand(command);
        }
    }


    public void checkMenuNameUnique() {
        if (menuRepository.isMenuNameDuplicated(getMenuName(), getMenuId(), getParentId())) {
            throw new ApiException(ErrorCode.Business.MENU_NAME_IS_NOT_UNIQUE);
        }
    }

    /**
     * Iframe和外链跳转类型  不允许添加按钮
     */
    public void checkAddButtonInIframeOrOutLink() {
        SysMenuEntity parentMenu = menuRepository.getById(getParentId());

        if (parentMenu != null && getIsButton() && (
            Objects.equals(parentMenu.getMenuType(), MenuTypeEnum.IFRAME.getValue())
                || Objects.equals(parentMenu.getMenuType(),MenuTypeEnum.OUTSIDE_LINK_REDIRECT.getValue())
        )) {
            throw new ApiException(ErrorCode.Business.MENU_NOT_ALLOWED_TO_CREATE_BUTTON_ON_IFRAME_OR_OUT_LINK);
        }
    }

    /**
     * 只允许在目录菜单类型底下 添加子菜单
     */
    public void checkAddMenuNotInCatalog() {
        SysMenuEntity parentMenu = menuRepository.getById(getParentId());

        if (parentMenu != null && !getIsButton() && (
            !Objects.equals(parentMenu.getMenuType(), MenuTypeEnum.CATALOG.getValue())
        )) {
            throw new ApiException(ErrorCode.Business.MENU_ONLY_ALLOWED_TO_CREATE_SUB_MENU_IN_CATALOG);
        }
    }


    public void checkExternalLink() {
//        if (getIsExternal() && !HttpUtil.isHttp(getPath()) && !HttpUtil.isHttps(getPath())) {
//            throw new ApiException(ErrorCode.Business.MENU_EXTERNAL_LINK_MUST_BE_HTTP);
//        }
    }


    public void checkParentIdConflict() {
        if (getMenuId().equals(getParentId())) {
            throw new ApiException(ErrorCode.Business.MENU_PARENT_ID_NOT_ALLOW_SELF);
        }
    }


    public void checkHasChildMenus() {
        if (menuRepository.hasChildrenMenu(getMenuId())) {
            throw new ApiException(ErrorCode.Business.MENU_EXIST_CHILD_MENU_NOT_ALLOW_DELETE);
        }
    }


    public void checkMenuAlreadyAssignToRole() {
        if (menuRepository.isMenuAssignToRoles(getMenuId())) {
            throw new ApiException(ErrorCode.Business.MENU_ALREADY_ASSIGN_TO_ROLE_NOT_ALLOW_DELETE);
        }
    }


}
