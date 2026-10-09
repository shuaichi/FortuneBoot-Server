package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderPageUtil;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderReconciliationVo;
import com.fortuneboot.service.fortune.order.reconciliation.FinanceOrderReconciliationCsvWriter;
import com.fortuneboot.service.fortune.order.reconciliation.FinanceOrderReconciliationOrderScan;
import com.fortuneboot.service.fortune.order.reconciliation.FinanceOrderReconciliationReportAssembler;
import com.fortuneboot.service.fortune.order.reconciliation.FinanceOrderReconciliationScan;
import com.fortuneboot.service.fortune.order.reconciliation.FinanceOrderReconciliationScanner;
import com.fortuneboot.service.fortune.order.reconciliation.FinanceOrderSafeBackfillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.OutputStream;
import java.util.List;

/**
 * 历史单据核对服务。
 * <p>只负责扫描、报告、CSV导出与安全回填的编排；扫描、装配、回填和CSV写出由独立组件承担。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Service
@RequiredArgsConstructor
public class FinanceOrderReconciliationService {

    private final FinanceOrderReconciliationScanner scanner;
    private final FinanceOrderReconciliationReportAssembler reportAssembler;
    private final FinanceOrderReconciliationCsvWriter csvWriter;
    private final FinanceOrderSafeBackfillService safeBackfillService;
    private final TransactionTemplate transactionTemplate;

    public List<FinanceOrderReconciliationVo> preview(Long bookId) {
        return buildReports(scanner.scan(bookId));
    }

    public List<FinanceOrderReconciliationVo> backfillSafe(Long bookId) {
        FinanceOrderReconciliationScan scan = scanner.scan(bookId);
        for (FinanceOrderReconciliationOrderScan orderScan : scan.orders()) {
            if (!orderScan.isSafe()) {
                continue;
            }
            try {
                transactionTemplate.executeWithoutResult(status -> safeBackfillService.backfill(orderScan));
            } catch (RuntimeException exception) {
                orderScan.markBackfillFailed(exception.getMessage());
            }
        }
        return buildReports(scan);
    }

    public PageDTO<FinanceOrderReconciliationVo> page(Long bookId, Integer pageNum, Integer pageSize) {
        List<FinanceOrderReconciliationVo> reports = preview(bookId);
        return new PageDTO<>(FinanceOrderPageUtil.paginate(reports, pageNum, pageSize), (long) reports.size());
    }

    public void writeCsv(Long bookId, OutputStream outputStream) {
        csvWriter.write(preview(bookId), outputStream);
    }

    private List<FinanceOrderReconciliationVo> buildReports(FinanceOrderReconciliationScan scan) {
        return reportAssembler.build(scan);
    }
}
