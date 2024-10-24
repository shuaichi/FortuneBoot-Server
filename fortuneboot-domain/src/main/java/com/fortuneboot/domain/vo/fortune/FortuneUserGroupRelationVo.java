package com.fortuneboot.domain.vo.fortune;

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
     * com.fortuneboot.common.enums.fortune.RoleTypeEnum
     * 权限
     */
    private Integer roleType;

    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 用户
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String userName;
}
