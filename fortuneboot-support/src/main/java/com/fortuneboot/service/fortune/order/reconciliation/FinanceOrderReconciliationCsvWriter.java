package com.fortuneboot.service.fortune.order.reconciliation;

import com.fortuneboot.common.utils.csv.CsvWriterUtil;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderReconciliationVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 历史单据核对CSV写出器。
 * <p>负责表头、行组装、BOM和Excel公式防护，服务层只做编排。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
@RequiredArgsConstructor
public class FinanceOrderReconciliationCsvWriter {

    private static final DateTimeFormatter GENERATED_AT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String HEADER = "orderId,billId,bookId,issueCodes,originalStatus,originalOutAmount,"
            + "originalInAmount,candidateIssuedAmount,candidateSettledAmount,"
            + "candidatePendingAmount,accountIds,currencyCodes,cashEffectKnown,note,generatedAt\n";

    private final Clock clock;

    public void write(List<FinanceOrderReconciliationVo> reports, OutputStream outputStream) {
        String generatedAt = GENERATED_AT_FORMAT.format(LocalDateTime.now(clock));
        StringBuilder builder = new StringBuilder(HEADER);
        for (FinanceOrderReconciliationVo report : reports) {
            appendRow(builder, report, generatedAt);
        }
        try {
            CsvWriterUtil.writeUtf8Bom(outputStream);
            outputStream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("单据核对报告CSV写入失败", exception);
        }
    }

    private void appendRow(StringBuilder builder, FinanceOrderReconciliationVo report, String generatedAt) {
        builder.append(cell(text(report.getOrderId()))).append(',')
                .append(cell(text(report.getBillId()))).append(',')
                .append(cell(text(report.getBookId()))).append(',')
                .append(cell(String.join("|", nullSafe(report.getIssueCodes())))).append(',')
                .append(cell(text(report.getOriginalStatus()))).append(',')
                .append(cell(report.getOriginalOutAmount())).append(',')
                .append(cell(report.getOriginalInAmount())).append(',')
                .append(cell(report.getCandidateIssuedAmount())).append(',')
                .append(cell(report.getCandidateSettledAmount())).append(',')
                .append(cell(report.getCandidatePendingAmount())).append(',')
                .append(cell(join(report.getAccountIds()))).append(',')
                .append(cell(String.join("|", nullSafe(report.getCurrencyCodes())))).append(',')
                .append(cell(String.valueOf(report.isCashEffectKnown()))).append(',')
                .append(CsvWriterUtil.cell(report.getNote(), true)).append(',')
                .append(cell(generatedAt))
                .append('\n');
    }

    private String cell(String value) {
        return CsvWriterUtil.cell(value, false);
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String join(Long[] values) {
        if (values == null || values.length == 0) {
            return "";
        }
        return Arrays.stream(values).map(String::valueOf).collect(Collectors.joining("|"));
    }

    private List<String> nullSafe(String[] values) {
        return values == null ? List.of() : List.of(values);
    }
}
