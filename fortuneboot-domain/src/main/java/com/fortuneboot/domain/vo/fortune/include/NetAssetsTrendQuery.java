package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

/**
 * 净资产趋势查询
 *
 * @author zhangchi118
 * @date 2026/8/4 19:45
 **/
@Data
public class NetAssetsTrendQuery {

    /**
     * 周期类型：3-近12月，4-近5年
     */
    private Integer periodType;
}
