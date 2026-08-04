package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 借贷总览
 *
 * @author zhangchi118
 * @date 2026/8/4 19:40
 **/
@Data
public class LoanOverviewVo {

    private BigDecimal totalReceivable;

    private BigDecimal totalPayable;

    private Integer receivableCount;

    private Integer payableCount;

    private List<LoanDetailVo> topReceivables;
}
