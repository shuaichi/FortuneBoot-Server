package com.fortuneboot.service.system;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.fortuneboot.domain.command.system.AddMenuCommand;
import com.fortuneboot.domain.command.system.UpdateMenuCommand;
import com.fortuneboot.domain.dto.menu.MenuDTO;
import com.fortuneboot.domain.dto.menu.MenuDetailDTO;
import com.fortuneboot.domain.dto.menu.RouterDTO;
import com.fortuneboot.factory.system.model.MenuModel;
import com.fortuneboot.factory.system.MenuModelFactory;
import com.fortuneboot.domain.query.system.MenuQuery;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.common.enums.common.StatusEnum;
import com.fortuneboot.domain.entity.system.SysMenuEntity;
import com.fortuneboot.repository.system.SysMenuRepository;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 菜单应用服务
 * @author valarchie
 */
@Service
@RequiredArgsConstructor
public class MenuApplicationService {

    private final SysMenuRepository menuRepository;

    private final MenuModelFactory menuModelFactory;


    public List<MenuDTO> getMenuList(MenuQuery query) {
        List<SysMenuEntity> list = menuRepository.list(query.toQueryWrapper());
        return list.stream().map(MenuDTO::new)
            .sorted(Comparator.comparing(MenuDTO::getRank, Comparator.nullsLast(Integer::compareTo)))
            .collect(Collectors.toList());
    }

    public MenuDetailDTO getMenuInfo(Long menuId) {
        SysMenuEntity byId = menuRepository.getById(menuId);
        return new MenuDetailDTO(byId);
    }

    public List<Tree<Long>> getDropdownList(SystemLoginUser loginUser) {
        List<SysMenuEntity> menuEntityList =
            loginUser.isAdmin() ? menuRepository.list() : menuRepository.getMenuListByUserId(loginUser.getUserId());

        return buildMenuTreeSelect(menuEntityList);
    }


    public void addMenu(AddMenuCommand addCommand) {
        MenuModel model = menuModelFactory.create();
        model.loadAddCommand(addCommand);

        // TODO 只允许在页面类型上添加按钮
        // 目前前端不支持嵌套的外链跳转
        model.checkMenuNameUnique();
        model.checkAddButtonInIframeOrOutLink();
        model.checkAddMenuNotInCatalog();
        model.checkExternalLink();

        model.insert();
    }

    public void updateMenu(UpdateMenuCommand updateCommand) {
        MenuModel model = menuModelFactory.loadById(updateCommand.getMenuId());
        model.loadUpdateCommand(updateCommand);

        model.checkMenuNameUnique();
        model.checkAddButtonInIframeOrOutLink();
        model.checkAddMenuNotInCatalog();
        model.checkExternalLink();
        model.checkParentIdConflict();

        model.updateById();
    }


    public void remove(Long menuId) {
        MenuModel menuModel = menuModelFactory.loadById(menuId);

        menuModel.checkHasChildMenus();
        menuModel.checkMenuAlreadyAssignToRole();

        menuModel.deleteById();
    }


    /**
     * 构建前端所需要树结构
     *
     * @param menus 菜单列表
     * @return 树结构列表
     */
    public List<Tree<Long>> buildMenuTreeSelect(List<SysMenuEntity> menus) {
        TreeNodeConfig config = new TreeNodeConfig();
        //默认为id可以不设置
        config.setIdKey("menuId");
        return TreeUtil.build(menus, 0L, config, (menu, tree) -> {
            tree.setId(menu.getMenuId());
            tree.setParentId(menu.getParentId());
            tree.putExtra("label", menu.getMenuName());
        });
    }


    public List<Tree<Long>> buildMenuEntityTree(SystemLoginUser loginUser) {
        List<SysMenuEntity> allMenus;
        if (loginUser.isAdmin()) {
            allMenus = menuRepository.list();
        } else {
            allMenus = menuRepository.getMenuListByUserId(loginUser.getUserId());
        }

        // 传给前端的路由排除掉按钮和停用的菜单
        List<SysMenuEntity> noButtonMenus = allMenus.stream()
            .filter(menu -> !menu.getIsButton())
            .filter(menu-> StatusEnum.ENABLE.getValue().equals(menu.getStatus()))
            .collect(Collectors.toList());

        TreeNodeConfig config = new TreeNodeConfig();
        // 默认为id 可以不设置
        config.setIdKey("menuId");

        return TreeUtil.build(noButtonMenus, 0L, config, (menu, tree) -> {
            tree.setId(menu.getMenuId());
            tree.setParentId(menu.getParentId());
            // TODO 可以取meta中的rank来排序
//            tree.setWeight(menu.getRank());
            tree.putExtra("entity", menu);
        });

    }


    public List<RouterDTO> buildRouterTree(List<Tree<Long>> trees) {
        List<RouterDTO> routers = new LinkedList<>();
        if (CollUtil.isNotEmpty(trees)) {
            for (Tree<Long> tree : trees) {
                Object entity = tree.get("entity");
                if (entity != null) {
                    RouterDTO routerDTO = new RouterDTO((SysMenuEntity) entity);
                    List<Tree<Long>> children = tree.getChildren();
                    if (CollUtil.isNotEmpty(children)) {
                        routerDTO.setChildren(buildRouterTree(children));
                    }
                    routers.add(routerDTO);
                }

            }
        }
        return routers;
    }


    public List<RouterDTO> getRouterTree(SystemLoginUser loginUser) {
        List<Tree<Long>> trees = buildMenuEntityTree(loginUser);
        return buildRouterTree(trees);
    }

}
