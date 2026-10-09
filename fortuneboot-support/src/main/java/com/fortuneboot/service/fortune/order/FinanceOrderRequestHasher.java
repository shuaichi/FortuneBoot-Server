package com.fortuneboot.service.fortune.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortuneboot.common.enums.fortune.FinanceOrderAuditActionEnum;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderCreateCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderMetadataCommand;
import com.fortuneboot.domain.dto.fortune.CategoryAmountDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 单据请求幂等哈希器。
 * <p>负责将命令内容与操作者折叠为稳定SHA-256指纹，供幂等回执比对。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
@RequiredArgsConstructor
public class FinanceOrderRequestHasher {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObjectMapper objectMapper;

    public String hashCreate(FinanceOrderCreateCommand command) {
        Map<String, Object> value = metadataValues(command);
        value.put("operation", FinanceOrderAuditActionEnum.CREATE.name());
        return digest(value);
    }

    public String hashModify(FinanceOrderMetadataCommand command) {
        Map<String, Object> value = metadataValues(command);
        value.put("operation", FinanceOrderAuditActionEnum.MODIFY.name());
        value.put("orderId", command.getOrderId());
        return digest(value);
    }

    public String hashTransition(String operation, Long bookId, Long orderId) {
        Map<String, Object> value = new TreeMap<>();
        value.put("operation", operation);
        value.put("bookId", bookId);
        value.put("orderId", orderId);
        value.put("operator", operator());
        return digest(value);
    }

    public String hashEntry(FinanceOrderEntryMutation command) {
        Map<String, Object> value = new TreeMap<>();
        value.put("operation", command.getOperation().name());
        value.put("bookId", command.getBookId());
        value.put("orderId", command.getOrderId());
        value.put("billId", command.getBillId());
        value.put("operator", operator());
        FinanceOrderEntryCommand entry = command.getEntryCommand();
        if (entry != null) {
            value.put("title", entry.getTitle());
            value.put("tradeTime", entry.getTradeTime() == null
                    ? null : DATE_TIME_FORMATTER.format(entry.getTradeTime()));
            value.put("accountId", entry.getAccountId());
            value.put("billType", entry.getBillType());
            value.put("orderComponent", entry.getOrderComponent());
            value.put("amount", entry.getAmount() == null ? null : entry.getAmount().toPlainString());
            value.put("payeeId", entry.getPayeeId());
            value.put("confirm", entry.getConfirm());
            value.put("include", entry.getInclude());
            value.put("remark", entry.getRemark());
            value.put("categories", categoryValues(entry.getCategoryAmountPair()));
            value.put("tags", entry.getTagIdList());
            value.put("members", entry.getMemberIdList());
            value.put("files", fileHashes(command.getFileList()));
        }
        return digest(value);
    }

    public String hashInclude(Long bookId, Long billId, boolean include) {
        Map<String, Object> value = new TreeMap<>();
        value.put("operation", include
                ? FinanceOrderAuditActionEnum.INCLUDE.name() : FinanceOrderAuditActionEnum.EXCLUDE.name());
        value.put("bookId", bookId);
        value.put("billId", billId);
        value.put("operator", operator());
        return digest(value);
    }

    private Map<String, Object> metadataValues(FinanceOrderCreateCommand command) {
        Map<String, Object> value = new TreeMap<>();
        value.put("bookId", command.getBookId());
        value.put("title", command.getTitle());
        value.put("type", command.getType());
        value.put("counterpartyId", command.getCounterpartyId());
        value.put("counterpartyName", command.getCounterpartyName());
        value.put("dueDate", command.getDueDate() == null ? null : command.getDueDate().toString());
        value.put("remark", command.getRemark());
        value.put("operator", operator());
        return value;
    }

    private String digest(Map<String, Object> value) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(value);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(json));
        } catch (Exception exception) {
            throw new IllegalStateException("单据请求哈希计算失败", exception);
        }
    }

    private List<Map<String, Object>> categoryValues(List<CategoryAmountDTO> categories) {
        if (categories == null) {
            return List.of();
        }
        List<Map<String, Object>> values = new ArrayList<>();
        for (CategoryAmountDTO category : categories) {
            Map<String, Object> value = new TreeMap<>();
            value.put("categoryId", category.getCategoryId());
            value.put("amount", category.getAmount() == null ? null : category.getAmount().toPlainString());
            values.add(value);
        }
        return values;
    }

    private List<String> fileHashes(List<MultipartFile> files) {
        if (files == null) {
            return List.of();
        }
        try {
            List<String> hashes = new ArrayList<>();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (MultipartFile file : files) {
                byte[] bytes = file == null || file.getBytes() == null ? new byte[0] : file.getBytes();
                hashes.add(HexFormat.of().formatHex(digest.digest(bytes)));
            }
            return hashes;
        } catch (Exception exception) {
            throw new IllegalStateException("附件哈希计算失败", exception);
        }
    }

    private String operator() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "anonymous" : authentication.getName();
    }
}
