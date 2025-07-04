package com.fortuneboot.domain.dto.user;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.annotation.ExcelColumn;
import com.fortuneboot.common.annotation.ExcelSheet;
import com.fortuneboot.domain.entity.system.SysRoleEntity;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import com.fortuneboot.domain.entity.system.SearchUserDO;

import java.time.LocalDateTime;
import java.util.Date;

import com.fortuneboot.domain.registry.CacheRegistry;
import lombok.Data;

/**
 * @author valarchie
 */
@ExcelSheet(name = "用户列表")
@Data
public class UserDTO {

    public UserDTO(SysUserEntity entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);

            SysUserEntity creator = CacheRegistry.getUserById(entity.getCreatorId());
            if (creator != null) {
                //创建者
                this.creatorName = creator.getUsername();
            }

            if (entity.getRoleId() != null) {
                SysRoleEntity roleEntity = CacheRegistry.getRoleById(entity.getRoleId());
                //角色名称
                this.roleName = roleEntity != null ? roleEntity.getRoleName() : "";
            }
        }
    }

    public UserDTO(SearchUserDO entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
            if (entity.getRoleId() != null) {
                SysRoleEntity roleEntity = CacheRegistry.getRoleById(entity.getRoleId());
                //角色名称
                this.roleName = roleEntity != null ? roleEntity.getRoleName() : "";
            }
        }
    }


    @ExcelColumn(name = "用户ID")
    private Long userId;

    @ExcelColumn(name = "角色ID")
    private Long roleId;

    @ExcelColumn(name = "角色名称")
    private String roleName;

    @ExcelColumn(name = "用户名")
    private String username;

    @ExcelColumn(name = "用户昵称")
    private String nickname;

    @ExcelColumn(name = "用户类型")
    private Integer userType;

    @ExcelColumn(name = "邮件")
    private String email;

    @ExcelColumn(name = "号码")
    private String phoneNumber;

    @ExcelColumn(name = "性别")
    private Integer sex;

    @ExcelColumn(name = "用户头像")
    private String avatar;

    @ExcelColumn(name = "状态")
    private Integer status;

    @ExcelColumn(name = "IP")
    private String loginIp;

    @ExcelColumn(name = "登录时间")
    private Date loginDate;

    @ExcelColumn(name = "创建者ID")
    private Long creatorId;

    @ExcelColumn(name = "创建者")
    private String creatorName;

    @ExcelColumn(name = "创建时间")
    private LocalDateTime createTime;

    @ExcelColumn(name = "修改者ID")
    private Long updaterId;

    @ExcelColumn(name = "修改者")
    private String updaterName;

    @ExcelColumn(name = "修改时间")
    private LocalDateTime updateTime;

    @ExcelColumn(name = "备注")
    private String remark;

}
