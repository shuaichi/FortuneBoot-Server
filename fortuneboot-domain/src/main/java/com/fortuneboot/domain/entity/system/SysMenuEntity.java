package com.fortuneboot.domain.entity.system;

import com.fortuneboot.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 菜单权限表
 * </p>
 *
 * @author valarchie
 * @since 2023-07-21
 */
@Getter
@Setter
@TableName("sys_menu")
@Schema(title = "SysMenuEntity对象", description = "菜单权限表")
public class SysMenuEntity extends BaseEntity<SysMenuEntity> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    @TableId(value = "menu_id", type = IdType.AUTO)
    private Long menuId;

    @Schema(description = "菜单名称")
    @TableField("menu_name")
    private String menuName;

    @Schema(description = "菜单的类型(1为普通菜单2为目录3为iFrame4为外部网站)")
    @TableField("menu_type")
    private Integer menuType;

    @Schema(description = "路由名称（需保持和前端对应的vue文件中的name保持一致defineOptions方法中设置的name）")
    @TableField("router_name")
    private String routerName;

    @Schema(description = "父菜单ID")
    @TableField("parent_id")
    private Long parentId;

    @Schema(description = "组件路径（对应前端项目view文件夹中的路径）")
    @TableField("path")
    private String path;

    @Schema(description = "是否按钮")
    @TableField("is_button")
    private Boolean isButton;

    @Schema(description = "权限标识")
    @TableField("permission")
    private String permission;

    @Schema(description = "路由元信息（前端根据这个信息进行逻辑处理）")
    @TableField("meta_info")
    private String metaInfo;

    @Schema(description = "菜单状态（1启用 0停用）")
    @TableField("`status`")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;


    @Override
    public Serializable pkVal() {
        return this.menuId;
    }

}
