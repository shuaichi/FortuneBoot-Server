package com.fortuneboot.domain.command.fortune;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分组/用户关系
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/11 23:09
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneUserGroupRelationModifyCommand extends FortuneUserGroupRelationAddCommand {

    /**
     * 主键
     */
    private Long userGroupRelationId;
}
