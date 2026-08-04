package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收支对比查询
 *
 * @author zhangchi118
 * @date 2026/8/4 19:36
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class BillCompareQuery extends BillIncludeQuery {

    /**
     * 对比类型：1-按月，2-按年
     */
    private Integer compareType;
}
