package com.fortuneboot.domain.command.fortune.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

/**
 * 历史单据人工确认Command。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/15
 */
@Data
public class FinanceOrderReconciliationConfirmCommand {

    /**
     * 账本id
     */
    @NotNull(message = "账本id不能为空")
    @Positive(message = "账本id只能是正数")
    private Long bookId;

    /**
     * 单据id
     */
    @NotNull(message = "单据id不能为空")
    @Positive(message = "单据id只能是正数")
    private Long orderId;

    /**
     * 请求幂等ID
     */
    @NotBlank(message = "请求幂等ID不能为空")
    private String requestId;

    /**
     * 期望版本号
     */
    @NotNull(message = "期望版本号不能为空")
    @PositiveOrZero(message = "期望版本号不能小于0")
    private Long expectedVersion;
}
