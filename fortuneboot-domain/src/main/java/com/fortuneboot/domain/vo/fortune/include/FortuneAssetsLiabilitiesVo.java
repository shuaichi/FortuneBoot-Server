package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 资产信息Vo
 * @author zhangchi118
 * @date 2025/2/24 09:26
 **/
@Data
public class FortuneAssetsLiabilitiesVo {

    /**
     * 资产
     */
    private BigDecimal totalAssets;

    /**
     * 负债
     */
    private BigDecimal totalLiabilities;

    /**
     * 净资产
     */
    private BigDecimal netAssets;
}
