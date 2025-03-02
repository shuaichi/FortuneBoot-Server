package com.fortuneboot.domain.command.fortune;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 余额调整
 *
 * @author zhangchi118
 * @date 2025/3/2 21:29
 **/
@Data
public class FortuneAccountAdjustCommand {

    /**
     * 账本id
     */
    @NotNull(message = "账本不能为空")
    private Long bookId;

    /**
     * 账户id
     */
    @NotNull(message = "账户不能为空")
    private Long accountId;

    /**
     * 标题
     */
    private String title;

    /**
     * 金额
     */
    @NotNull(message = "金额不能为空")
    private BigDecimal balance;

    /**
     * 发生事件
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "时间不能为空")
    private LocalDateTime tradeTime;

    /**
     * 备注
     */
    private String remark;
}
