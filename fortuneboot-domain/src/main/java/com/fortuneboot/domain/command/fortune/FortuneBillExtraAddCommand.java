package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单附加费用（手续费/优惠）新增命令
 *
 * @author zhangchi118
 **/
@Data
public class FortuneBillExtraAddCommand {

    /**
     * 关联账单id，新增账单时允许为空，保存主表后回填
     */
    private Long billId;

    /**
     * 附加类型：1-手续费，2-优惠
     */
    @NotNull(message = "附加类型不能为空")
    @Positive
    private Integer extraType;

    /**
     * 金额（>0）
     */
    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    /**
     * 账户方向：1-转出账户(from)，2-转入账户(to)
     * 支出账单默认为1；转账账单可指定手续费/优惠从哪方扣
     */
    @NotNull(message = "账户方向不能为空")
    @Positive
    private Integer accountSide;

    /**
     * 关联分类id（可为空）
     */
    private Long categoryId;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}