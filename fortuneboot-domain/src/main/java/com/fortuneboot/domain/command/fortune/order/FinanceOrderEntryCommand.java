package com.fortuneboot.domain.command.fortune.order;

import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 单据关联流水Command（multipart data部分的JSON）
 * <p>
 * 沿用FortuneBillAddCommand字段，增加幂等、乐观锁和单据组件；
 * tradeTime/confirm/金额精度及PRINCIPAL的amount要求由单据写入口强校验。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceOrderEntryCommand extends FortuneBillAddCommand {

    /**
     * 请求幂等ID（UUID）
     */
    @NotBlank(message = "请求幂等ID不能为空")
    @Size(max = 64, message = "请求幂等ID长度不能超过64个字符")
    private String requestId;

    /**
     * 期望版本号
     */
    @NotNull(message = "期望版本号不能为空")
    @Positive(message = "期望版本号只能是正数")
    private Long expectedVersion;

    /**
     * 单据组件
     *
     * @see com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum
     */
    private String orderComponent;
}
