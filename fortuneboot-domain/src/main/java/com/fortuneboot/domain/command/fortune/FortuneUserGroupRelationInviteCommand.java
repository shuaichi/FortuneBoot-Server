package com.fortuneboot.domain.command.fortune;


import com.fortuneboot.common.enums.fortune.RoleTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分组/用户关系
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/11 23:09
 **/
@Data
public class FortuneUserGroupRelationInviteCommand {

    /**
     * 权限类型
     * @see RoleTypeEnum
     */
    @NotNull
    private Integer roleType;

    /**
     * 分组id
     */
    @NotNull
    private Long groupId;

    /**
     * 用户名
     */
    @NotBlank
    private String username;

}
