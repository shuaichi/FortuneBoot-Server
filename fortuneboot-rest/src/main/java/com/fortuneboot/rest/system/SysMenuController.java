package com.fortuneboot.rest.system;

import cn.hutool.core.lang.tree.Tree;
import com.fortuneboot.common.core.base.BaseController;
import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.customize.accessLog.AccessLog;
import com.fortuneboot.service.system.MenuApplicationService;
import com.fortuneboot.domain.command.system.AddMenuCommand;
import com.fortuneboot.domain.command.system.UpdateMenuCommand;
import com.fortuneboot.domain.dto.menu.MenuDTO;
import com.fortuneboot.domain.dto.menu.MenuDetailDTO;
import com.fortuneboot.domain.query.system.MenuQuery;
import com.fortuneboot.infrastructure.user.AuthenticationUtils;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 菜单信息
 *
 * @author valarchie
 */
@Tag(name = "菜单API", description = "菜单相关的增删查改")
@RestController
@RequestMapping("/system/menus")
@Validated
@RequiredArgsConstructor
public class SysMenuController extends BaseController {

    private final MenuApplicationService menuApplicationService;

    /**
     * 获取菜单列表
     */
    @Operation(summary = "菜单列表")
    @PreAuthorize("@permission.has('system:menu:list')")
    @GetMapping
    public ResponseDTO<List<MenuDTO>> menuList(MenuQuery menuQuery) {
        List<MenuDTO> menuList = menuApplicationService.getMenuList(menuQuery);
        return ResponseDTO.ok(menuList);
    }

    /**
     * 根据菜单编号获取详细信息
     */
    @Operation(summary = "菜单详情")
    @PreAuthorize("@permission.has('system:menu:query')")
    @GetMapping(value = "/{menuId}")
    public ResponseDTO<MenuDetailDTO> menuInfo(@PathVariable @NotNull @PositiveOrZero Long menuId) {
        MenuDetailDTO menu = menuApplicationService.getMenuInfo(menuId);
        return ResponseDTO.ok(menu);
    }

    /**
     * 获取菜单下拉树列表
     */
    @Operation(summary = "菜单列表（树级）", description = "菜单树级下拉框")
    @GetMapping("/dropdown")
    public ResponseDTO<List<Tree<Long>>> dropdownList() {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        List<Tree<Long>> dropdownList = menuApplicationService.getDropdownList(loginUser);
        return ResponseDTO.ok(dropdownList);
    }

    /**
     * 新增菜单
     * 需支持一级菜单以及 多级菜单 子菜单为一个 或者 多个的情况
     * 隐藏菜单不显示  以及rank排序
     * 内链 和 外链
     */
    @Operation(summary = "添加菜单")
    @PreAuthorize("@permission.has('system:menu:add')")
    @AccessLog(title = "菜单管理", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@RequestBody AddMenuCommand addCommand) {
        menuApplicationService.addMenu(addCommand);
        return ResponseDTO.ok();
    }

    /**
     * 修改菜单
     */
    @Operation(summary = "编辑菜单")
    @PreAuthorize("@permission.has('system:menu:edit')")
    @AccessLog(title = "菜单管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{menuId}")
    public ResponseDTO<Void> edit(@PathVariable("menuId") Long menuId, @RequestBody UpdateMenuCommand updateCommand) {
        updateCommand.setMenuId(menuId);
        menuApplicationService.updateMenu(updateCommand);
        return ResponseDTO.ok();
    }

    /**
     * 删除菜单
     */
    @Operation(summary = "删除菜单")
    @PreAuthorize("@permission.has('system:menu:remove')")
    @AccessLog(title = "菜单管理", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/{menuId}")
    public ResponseDTO<Void> remove(@PathVariable("menuId") Long menuId) {
        menuApplicationService.remove(menuId);
        return ResponseDTO.ok();
    }

}
