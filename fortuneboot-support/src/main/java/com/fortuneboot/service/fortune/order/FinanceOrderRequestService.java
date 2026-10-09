package com.fortuneboot.service.fortune.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.dto.fortune.CategoryAmountDTO;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderRequestEntity;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRequestRepo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinanceOrderRequestService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FinanceOrderMutationService mutationService;
    private final FortuneFinanceOrderRequestRepo requestRepo;
    private final ObjectMapper objectMapper;

    public FinanceOrderMutationVo mutateEntry(FinanceOrderEntryMutation command) {
        normalizeRequestId(command);
        normalizeCommandForHash(command);
        String requestHash = hash(command);

        FortuneFinanceOrderRequestEntity receipt = findReceipt(command.getBookId(), command.getRequestId());
        if (receipt != null) {
            if (!Objects.equals(receipt.getRequestHash(), requestHash)) {
                throw new ApiException(ErrorCode.Business.ORDER_REQUEST_CONFLICT);
            }
            return readResponse(receipt.getResponseJson(), true);
        }

        try {
            return mutationService.executeEntry(command, requestHash);
        } catch (DataAccessException exception) {
            FortuneFinanceOrderRequestEntity concurrentReceipt =
                    findReceipt(command.getBookId(), command.getRequestId());
            if (concurrentReceipt != null) {
                if (!Objects.equals(concurrentReceipt.getRequestHash(), requestHash)) {
                    throw new ApiException(ErrorCode.Business.ORDER_REQUEST_CONFLICT);
                }
                return readResponse(concurrentReceipt.getResponseJson(), true);
            }
            throw new ApiException(ErrorCode.Business.ORDER_VERSION_CONFLICT);
        }
    }

    public FinanceOrderMutationVo legacyInclude(Long bookId, Long billId, boolean include) {
        String requestId = UUID.randomUUID().toString();
        String requestHash = hashInclude(bookId, billId, include);
        FortuneFinanceOrderRequestEntity receipt = findReceipt(bookId, requestId);
        if (receipt != null) {
            if (!Objects.equals(receipt.getRequestHash(), requestHash)) {
                throw new ApiException(ErrorCode.Business.ORDER_REQUEST_CONFLICT);
            }
            return readResponse(receipt.getResponseJson(), true);
        }
        try {
            return mutationService.executeInclude(bookId, billId, include, requestId, requestHash);
        } catch (DataAccessException exception) {
            FortuneFinanceOrderRequestEntity concurrentReceipt = findReceipt(bookId, requestId);
            if (concurrentReceipt != null) {
                if (!Objects.equals(concurrentReceipt.getRequestHash(), requestHash)) {
                    throw new ApiException(ErrorCode.Business.ORDER_REQUEST_CONFLICT);
                }
                return readResponse(concurrentReceipt.getResponseJson(), true);
            }
            throw new ApiException(ErrorCode.Business.ORDER_VERSION_CONFLICT);
        }
    }

    private void normalizeRequestId(FinanceOrderEntryMutation command) {
        if (command.getRequestId() == null || command.getRequestId().isBlank()) {
            command.setRequestId(UUID.randomUUID().toString());
        }
        if (command.getEntryCommand() != null) {
            command.getEntryCommand().setRequestId(command.getRequestId());
        }
    }

    private void normalizeCommandForHash(FinanceOrderEntryMutation command) {
        FinanceOrderEntryCommand entryCommand = command.getEntryCommand();
        if (entryCommand == null || command.getBookId() == null || command.getOrderId() == null) {
            return;
        }
        entryCommand.setBookId(command.getBookId());
        entryCommand.setOrderId(command.getOrderId());
        FinanceOrderComponentEnum component =
                FinanceOrderComponentEnum.getByValue(entryCommand.getOrderComponent());
        if (component == FinanceOrderComponentEnum.PRINCIPAL
                && List.of(9, 10, 11, 12).contains(entryCommand.getBillType())) {
            entryCommand.setAmount(scale(entryCommand.getAmount()));
        } else if (entryCommand.getCategoryAmountPair() != null) {
            BigDecimal amount = entryCommand.getCategoryAmountPair().stream()
                    .map(CategoryAmountDTO::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            entryCommand.setAmount(scale(amount));
        }
        entryCommand.setConvertedAmount(entryCommand.getAmount());
        entryCommand.setInclude(Objects.requireNonNullElse(entryCommand.getInclude(), Boolean.TRUE));
    }

    private String hash(FinanceOrderEntryMutation command) {
        Map<String, Object> value = new TreeMap<>();
        value.put("operation", command.getOperation().name());
        value.put("bookId", command.getBookId());
        value.put("orderId", command.getOrderId());
        value.put("billId", command.getBillId());
        value.put("operator", operator());
        FinanceOrderEntryCommand entry = command.getEntryCommand();
        if (entry != null) {
            value.put("title", entry.getTitle());
            value.put("tradeTime", entry.getTradeTime() == null ? null : DATE_TIME_FORMATTER.format(entry.getTradeTime()));
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

    private String hashInclude(Long bookId, Long billId, boolean include) {
        Map<String, Object> value = new TreeMap<>();
        value.put("operation", include ? "INCLUDE" : "EXCLUDE");
        value.put("bookId", bookId);
        value.put("billId", billId);
        value.put("operator", operator());
        return digest(value);
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

    private BigDecimal scale(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    private FortuneFinanceOrderRequestEntity findReceipt(Long bookId, String requestId) {
        LambdaQueryWrapper<FortuneFinanceOrderRequestEntity> wrapper =
                new LambdaQueryWrapper<>(FortuneFinanceOrderRequestEntity.class);
        wrapper.eq(FortuneFinanceOrderRequestEntity::getBookId, bookId)
                .eq(FortuneFinanceOrderRequestEntity::getRequestId, requestId);
        return requestRepo.getOne(wrapper, false);
    }

    private FinanceOrderMutationVo readResponse(String responseJson, boolean replayed) {
        try {
            FinanceOrderMutationVo result = objectMapper.readValue(responseJson, FinanceOrderMutationVo.class);
            result.setReplayed(replayed);
            return result;
        } catch (Exception exception) {
            throw new ApiException(ErrorCode.Business.ORDER_REQUEST_CONFLICT);
        }
    }
}
