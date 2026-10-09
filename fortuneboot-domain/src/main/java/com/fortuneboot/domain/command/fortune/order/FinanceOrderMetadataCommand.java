package com.fortuneboot.domain.command.fortune.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 单据元数据修改Command
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceOrderMetadataCommand extends FinanceOrderCreateCommand {

    /**
     * 单据id
     */
    @NotNull(message = "单据id不能为空")
    @Positive(message = "单据id只能是正数")
    private Long orderId;

    /**
     * 期望版本号
     */
    @NotNull(message = "期望版本号不能为空")
    @Positive(message = "期望版本号只能是正数")
    private Long expectedVersion;
}
