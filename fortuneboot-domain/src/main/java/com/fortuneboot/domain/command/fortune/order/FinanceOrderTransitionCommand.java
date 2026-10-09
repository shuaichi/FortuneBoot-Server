package com.fortuneboot.domain.command.fortune.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 单据生命周期转换Command
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Data
public class FinanceOrderTransitionCommand {

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
     * 生命周期动作
     */
    @NotNull(message = "生命周期动作不能为空")
    private Action action;

    /**
     * 请求幂等ID
     */
    @NotBlank(message = "请求幂等ID不能为空")
    private String requestId;

    /**
     * 期望版本号
     */
    @NotNull(message = "期望版本号不能为空")
    @Positive(message = "期望版本号只能是正数")
    private Long expectedVersion;

    /**
     * 生命周期动作类型
     */
    public enum Action {
        CLOSE, REOPEN, REMOVE
    }
}
