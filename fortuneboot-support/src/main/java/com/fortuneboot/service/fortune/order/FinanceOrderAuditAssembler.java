package com.fortuneboot.service.fortune.order;

import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderAuditEntity;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderAuditVo;
import org.springframework.stereotype.Component;

/**
 * 单据审计装配器。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
public class FinanceOrderAuditAssembler {

    public FinanceOrderAuditVo toVo(FortuneFinanceOrderAuditEntity audit) {
        FinanceOrderAuditVo vo = new FinanceOrderAuditVo();
        vo.setId(audit.getId());
        vo.setOrderId(audit.getOrderId());
        vo.setBillId(audit.getBillId());
        vo.setAction(audit.getAction());
        vo.setCreatorId(audit.getCreatorId());
        vo.setCreateTime(audit.getCreateTime());
        vo.setBeforeJson(audit.getBeforeJson());
        vo.setAfterJson(audit.getAfterJson());
        return vo;
    }
}
