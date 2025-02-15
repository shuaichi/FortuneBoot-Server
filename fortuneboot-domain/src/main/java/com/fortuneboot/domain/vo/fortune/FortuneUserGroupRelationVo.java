package com.fortuneboot.domain.vo.fortune;

import com.fortuneboot.common.enums.fortune.RoleTypeEnum;

import lombok.Data;

/**
 * 分组/用户关系Vo
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/12 23:35
 **/
@Data
public class FortuneUserGroupRelationVo {

    /**
     * 关系主键
     */
    private Long userGroupRelationId;

    /**
     * @see RoleTypeEnum
     * 角色
     */
    private Integer roleType;

    /**
     * 角色描述
     */
    private String roleTypeDesc;

    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 用户姓名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;
}
