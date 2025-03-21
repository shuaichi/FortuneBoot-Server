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
 * 角色信息表
 * </p>
 *
 * @author valarchie
 * @since 2022-10-02
 */
@Getter
@Setter
@TableName("sys_role")
@Schema(title = "SysRoleEntity对象", description = "角色信息表")
public class SysRoleEntity extends BaseEntity<SysRoleEntity> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "角色ID")
    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;

    @Schema(description = "角色名称")
    @TableField("role_name")
    private String roleName;

    @Schema(description = "角色权限字符串")
    @TableField("role_key")
    private String roleKey;

    @Schema(description = "显示顺序")
    @TableField("role_sort")
    private Integer roleSort;

    @Schema(description = "数据范围（1：全部数据权限 2：自定数据权限 3: 本部门数据权限 4: 本部门及以下数据权限 5: 本人权限）")
    @TableField("data_scope")
    private Integer dataScope;

    @Schema(description = "角色状态（1正常 0停用）")
    @TableField("`status`")
    private Integer status;


    @Schema(description = "允许注册")
    @TableField("allow_register")
    private Boolean allowRegister;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;


    @Override
    public Serializable pkVal() {
        return this.roleId;
    }

}
