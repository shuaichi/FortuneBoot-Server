package com.fortuneboot.service.fortune.order.reconciliation;

import com.fortuneboot.common.enums.fortune.FinanceOrderReconciliationIssueEnum;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * 单笔单据扫描结果。
 * <p>封装扫描发现与安全回填状态，避免向服务层暴露可变集合。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Getter
public class FinanceOrderReconciliationOrderScan {

    private final FortuneFinanceOrderEntity order;
    private final List<FortuneBillEntity> bills;
    private final LinkedHashSet<String> issueCodes;
    private final LinkedHashSet<Long> accountIds;
    private final LinkedHashSet<String> currencies;
    private final FinanceOrderLedgerSnapshot snapshot;
    private final boolean cashEffectKnown;
    private boolean safe;
    private String note;

    FinanceOrderReconciliationOrderScan(FortuneFinanceOrderEntity order,
                                        List<FortuneBillEntity> bills,
                                        LinkedHashSet<String> issueCodes,
                                        LinkedHashSet<Long> accountIds,
                                        LinkedHashSet<String> currencies,
                                        FinanceOrderLedgerSnapshot snapshot,
                                        boolean cashEffectKnown,
                                        boolean safe,
                                        String note) {
        this.order = order;
        this.bills = bills;
        this.issueCodes = issueCodes;
        this.accountIds = accountIds;
        this.currencies = currencies;
        this.snapshot = snapshot;
        this.cashEffectKnown = cashEffectKnown;
        this.safe = safe;
        this.note = note;
    }

    void addIssue(FinanceOrderReconciliationIssueEnum issue) {
        this.issueCodes.add(issue.name());
    }

    /**
     * 安全回填失败时记录原因并标记为不可安全回填。
     */
    public void markBackfillFailed(String message) {
        addIssue(FinanceOrderReconciliationIssueEnum.BACKFILL_FAILED);
        this.note = this.note == null ? message : this.note + "; " + message;
        this.safe = false;
    }
}
