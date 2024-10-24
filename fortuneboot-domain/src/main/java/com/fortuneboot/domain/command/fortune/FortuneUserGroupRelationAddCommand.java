package com.fortuneboot.domain.command.fortune;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分组/用户关系
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/11 23:09
 **/
@Data
public class FortuneUserGroupRelationAddCommand {

    /**
     * 权限类型
     */
    @NotNull
    private Integer roleType;

    /**
     * 分组id
     */
    @NotNull
    private Long groupId;

    /**
     * 用户id
     */
    @NotNull
    private Long userId;

}
