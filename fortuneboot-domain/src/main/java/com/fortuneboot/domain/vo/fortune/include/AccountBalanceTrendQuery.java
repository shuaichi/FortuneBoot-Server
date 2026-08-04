package com.fortuneboot.domain.vo.fortune.include;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 账户余额趋势查询
 *
 * @author zhangchi118
 * @date 2026/8/4 19:42
 **/
@Data
public class AccountBalanceTrendQuery {

    @NotNull(message = "账户不能为空")
    @Positive(message = "账户ID必须是正数")
    private Long accountId;

    /**
     * 周期类型：3-近12月，4-近5年
     */
    private Integer periodType;
}
