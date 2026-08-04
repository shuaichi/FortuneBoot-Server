package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收支排行查询
 *
 * @author zhangchi118
 * @date 2026/8/4 19:37
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class BillRankQuery extends BillIncludeQuery {

    /**
     * TopN，默认10
     */
    private Integer topN;
}
