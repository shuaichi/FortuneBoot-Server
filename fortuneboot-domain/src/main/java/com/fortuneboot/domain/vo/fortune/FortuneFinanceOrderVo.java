package com.fortuneboot.domain.vo.fortune;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderTypeEnum;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * @author zhangchi118
 * @date 2025/8/25 20:56
 **/
@Data
public class FortuneFinanceOrderVo {

    public FortuneFinanceOrderVo(FortuneFinanceOrderEntity entity) {
        if (Objects.isNull(entity)) {
            return;
        }
        BeanUtils.copyProperties(entity, this);
        this.setTypeDesc(FinanceOrderTypeEnum.getDescByValue(entity.getType()));
        this.setStatusDesc(FinanceOrderStatusEnum.getDescByValue(entity.getStatus()));
    }

    /**
     * 单据id
     */
    private Long orderId;

    /**
     * 账本id
     */
    private Long bookId;

    /**
     * 标题
     */
    private String title;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 类型描述
     */
    private String typeDesc;

    /**
     * 支出金额
     */
    private BigDecimal outAmount;

    /**
     * 转入金额
     */
    private BigDecimal inAmount;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 单据提交时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime submitTime;

    /**
     * 单据关闭时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime closeTime;

    /**
     * 备注
     */
    private String remark;
}
