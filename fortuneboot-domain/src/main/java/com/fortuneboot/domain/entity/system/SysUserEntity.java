package com.fortuneboot.domain.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fortuneboot.common.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 用户信息表
 * </p>
 *
 * @author valarchie
 * @since 2023-02-27
 */
@Getter
@Setter
@TableName("sys_user")
@Schema( title = "SysUserEntity对象", description = "用户信息表")
public class SysUserEntity extends BaseEntity<SysUserEntity> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    @Schema(description = "角色id")
    @TableField("role_id")
    private Long roleId;

    @Schema(description = "用户账号")
    @TableField("username")
    private String username;

    @Schema(description = "用户昵称")
    @TableField("nickname")
    private String nickname;

    @Schema(description = "用户类型（00系统用户）")
    @TableField("user_type")
    private Integer userType;

    @Schema(description = "用户邮箱")
    @TableField("email")
    private String email;

    @Schema(description = "手机号码")
    @TableField("phone_number")
    private String phoneNumber;

    @Schema(description = "用户性别（0男 1女 2未知）")
    @TableField("sex")
    private Integer sex;

    @Schema(description = "头像地址")
    @TableField("avatar")
    private String avatar;

    @Schema(description = "密码")
    @TableField("`password`")
    private String password;

    @Schema(description = "帐号状态（1正常 2停用 3冻结）")
    @TableField("`status`")
    private Integer status;

    @Schema(description = "最后登录IP")
    @TableField("login_ip")
    private String loginIp;

    @Schema(description = "最后登录时间")
    @TableField("login_date")
    private Date loginDate;

    @Schema(description = "超级管理员标志（1是，0否）")
    @TableField("is_admin")
    private Boolean isAdmin;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;


    @Override
    public Serializable pkVal() {
        return this.userId;
    }

}
