package com.fortuneboot.domain.command.fortune;

import lombok.Data;

/**
 * 标签关系表
 *
 * @author zhangchi118
 * @date 2025/1/29 20:00
 **/
@Data
public class FortuneTagRelationAddCommand {

    /**
     * 账单id
     */
    private Long billId;

    /**
     * 标签id
     */
    private Long tagId;
}
