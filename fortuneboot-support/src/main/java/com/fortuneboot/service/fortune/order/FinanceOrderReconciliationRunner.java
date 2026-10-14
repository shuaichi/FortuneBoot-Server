package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * 历史单据核对命令行入口。
 * <p>参数固定为finance.reconcile.mode/book-id/output；safe-backfill必须显式启用专用profile。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
@RequiredArgsConstructor
public class FinanceOrderReconciliationRunner implements ApplicationRunner {

    private static final String MODE_PREVIEW = "preview";
    private static final String MODE_SAFE_BACKFILL = "safe-backfill";
    private static final String RECONCILE_PROFILE = "finance-order-reconcile";

    private final FinanceOrderReconciliationService reconciliationService;
    private final Environment environment;

    @Override
    public void run(ApplicationArguments args) {
        Map<String, String> options = options(args);
        if (!options.containsKey("book-id") && !options.containsKey("mode")) {
            return;
        }
        String mode = options.getOrDefault("mode", MODE_PREVIEW);
        if (!MODE_PREVIEW.equals(mode) && !MODE_SAFE_BACKFILL.equals(mode)) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                    "finance.reconcile.mode只能是preview或safe-backfill");
        }
        String bookIdText = options.get("book-id");
        String output = options.get("output");
        if (bookIdText == null || bookIdText.isBlank() || output == null || output.isBlank()) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                    "finance.reconcile.book-id和output不能为空");
        }
        long bookId;
        try {
            bookId = Long.parseLong(bookIdText);
        } catch (NumberFormatException exception) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "账本ID必须是数字");
        }
        if (MODE_SAFE_BACKFILL.equals(mode)
                && !environment.acceptsProfiles(Profiles.of(RECONCILE_PROFILE))) {
            throw new ApiException(ErrorCode.Business.ORDER_RECONCILIATION_REQUIRED,
                    "safe-backfill必须启用finance-order-reconcile profile");
        }
        Path outputPath = Path.of(output);
        try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
            if (MODE_SAFE_BACKFILL.equals(mode)) {
                reconciliationService.backfillSafe(bookId);
            } else {
                reconciliationService.preview(bookId);
            }
            reconciliationService.writeCsv(bookId, outputStream);
        } catch (Exception exception) {
            if (exception instanceof ApiException apiException) {
                throw apiException;
            }
            throw new IllegalStateException("单据核对报告写入失败: " + outputPath, exception);
        }
    }

    private Map<String, String> options(ApplicationArguments args) {
        Map<String, String> result = new HashMap<>();
        for (String argument : args.getNonOptionArgs()) {
            if (!argument.startsWith("--finance.reconcile.")) {
                continue;
            }
            String value = argument.substring("--finance.reconcile.".length());
            int separator = value.indexOf('=');
            if (separator > 0) {
                result.put(value.substring(0, separator), value.substring(separator + 1));
            }
        }
        return result;
    }
}
