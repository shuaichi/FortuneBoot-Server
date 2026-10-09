package com.fortuneboot.domain.vo.fortune.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 单据操作审计VO
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Data
public class FinanceOrderAuditVo {

    /**
     * 审计id
     */
    private Long id;

    /**
     * 单据id
     */
    private Long orderId;

    /**
     * 流水id
     */
    private Long billId;

    /**
     * 操作动作
     */
    private String action;

    /**
     * 操作者id
     */
    private Long creatorId;

    /**
     * 操作时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 操作前快照
     */
    private String beforeJson;

    /**
     * 操作后快照
     */
    private String afterJson;
}
