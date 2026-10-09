package com.fortuneboot.service.fortune.order.reconciliation;

import com.fortuneboot.domain.vo.fortune.order.FinanceOrderReconciliationVo;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 历史单据核对报告装配器。
 * <p>只把扫描结果转换为报告VO，不访问数据库。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
public class FinanceOrderReconciliationReportAssembler {

    public List<FinanceOrderReconciliationVo> build(FinanceOrderReconciliationScan scan) {
        List<FinanceOrderReconciliationVo> result = new ArrayList<>();
        for (FinanceOrderReconciliationOrderScan orderScan : scan.orders()) {
            if (!orderScan.isSafe() && !orderScan.getIssueCodes().isEmpty()) {
                result.add(orderReport(orderScan));
            }
        }
        scan.orphans().stream()
                .sorted(Comparator.comparing(orphan -> orphan.bill().getBillId()))
                .forEach(orphan -> result.add(orphanReport(orphan)));
        return result.stream()
                .sorted(Comparator.comparing(FinanceOrderReconciliationVo::getOrderId,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(FinanceOrderReconciliationVo::getBillId,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private FinanceOrderReconciliationVo orderReport(FinanceOrderReconciliationOrderScan orderScan) {
        FinanceOrderReconciliationVo vo = baseReport(orderScan.getOrder().getBookId(),
                orderScan.getIssueCodes(), orderScan.getCurrencies());
        vo.setOrderId(orderScan.getOrder().getOrderId());
        vo.setOriginalStatus(orderScan.getOrder().getStatus());
        vo.setOriginalOutAmount(amount(orderScan.getOrder().getOutAmount()));
        vo.setOriginalInAmount(amount(orderScan.getOrder().getInAmount()));
        FinanceOrderLedgerSnapshot snapshot = orderScan.getSnapshot();
        if (snapshot != null) {
            vo.setCandidateIssuedAmount(amount(snapshot.issuedAmount()));
            vo.setCandidateSettledAmount(amount(snapshot.settledAmount()));
            vo.setCandidatePendingAmount(amount(snapshot.pendingAmount()));
        }
        vo.setAccountIds(orderScan.getAccountIds().toArray(Long[]::new));
        vo.setCashEffectKnown(orderScan.isCashEffectKnown());
        vo.setNote(orderScan.getNote());
        return vo;
    }

    private FinanceOrderReconciliationVo orphanReport(FinanceOrderReconciliationOrphanScan orphan) {
        FinanceOrderReconciliationVo vo = baseReport(orphan.bookId(), orphan.issueCodes(), Set.of());
        vo.setBillId(orphan.bill().getBillId());
        vo.setAccountIds(orphan.bill().getAccountId() == null && orphan.bill().getToAccountId() == null
                ? new Long[0] : new Long[]{orphan.bill().getAccountId()});
        vo.setNote("关联单据不存在或已删除");
        return vo;
    }

    private FinanceOrderReconciliationVo baseReport(Long bookId, Set<String> issueCodes,
                                                    Set<String> currencies) {
        FinanceOrderReconciliationVo vo = new FinanceOrderReconciliationVo();
        vo.setBookId(bookId);
        vo.setIssueCodes(issueCodes.toArray(String[]::new));
        vo.setCurrencyCodes(currencies.toArray(String[]::new));
        return vo;
    }

    private String amount(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }
}
