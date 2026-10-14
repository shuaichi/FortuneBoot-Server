package com.fortuneboot.domain.vo.fortune.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单据写操作结果VO
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceOrderMutationVo {

    /**
     * 单据id
     */
    private Long orderId;

    /**
     * 流水id，单据级操作为空
     */
    private Long billId;

    /**
     * 操作后的单据版本号
     */
    private Long version;

    /**
     * 是否幂等重放
     */
    private Boolean replayed;
}
