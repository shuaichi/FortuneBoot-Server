package com.fortuneboot.domain.vo.fortune.include;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author zhangchi118
 * @date 2025/2/24 21:12
 **/
@Data
public class BillTrendsQuery {

    /**
     * 账本id
     */
    @NotNull
    @Positive
    private Long bookId;

    /**
     * 时间维度
     *
     */
    private Integer timeGranularity;

    /**
     * 时间点
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timePoint;

    /**
     * 账单类型
     */
    private Integer billType;
}
