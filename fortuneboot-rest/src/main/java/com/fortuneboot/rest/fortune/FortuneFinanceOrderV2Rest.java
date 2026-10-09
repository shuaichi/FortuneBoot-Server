package com.fortuneboot.rest.fortune;

import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortuneboot.customize.accessLog.AccessLog;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderCreateCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderMetadataCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderTransitionCommand;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderEntryQuery;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderOptionQuery;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderPageQuery;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderAuditVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderDetailVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderEntryVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderReconciliationVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderSummaryVo;
import com.fortuneboot.service.fortune.order.FinanceOrderQueryService;
import com.fortuneboot.service.fortune.order.FinanceOrderReconciliationService;
import com.fortuneboot.service.fortune.order.FinanceOrderRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 单据V2：生命周期、查询、幂等写入口与新旧HTTP适配。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/fortune/finance/order/v2")
@Tag(name = "单据V2", description = "单据生命周期、查询、幂等写入与审计")
public class FortuneFinanceOrderV2Rest {

    private final FinanceOrderQueryService financeOrderQueryService;
    private final FinanceOrderRequestService financeOrderRequestService;
    private final FinanceOrderReconciliationService financeOrderReconciliationService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "分页查询单据")
    @GetMapping("/page")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    public ResponseDTO<PageDTO<FinanceOrderDetailVo>> page(@Valid FinanceOrderPageQuery query) {
        return ResponseDTO.ok(financeOrderQueryService.page(query));
    }

    @Operation(summary = "查询单据看板汇总")
    @GetMapping("/{bookId}/summary")
    @PreAuthorize("@fortune.bookVisitorPermission(#bookId)")
    public ResponseDTO<FinanceOrderSummaryVo> summary(@PathVariable @Positive Long bookId) {
        return ResponseDTO.ok(financeOrderQueryService.summary(bookId));
    }

    @Operation(summary = "分页查询单据历史核对报告")
    @GetMapping("/{bookId}/reconciliation")
    @PreAuthorize("@fortune.bookVisitorPermission(#bookId)")
    public ResponseDTO<PageDTO<FinanceOrderReconciliationVo>> reconciliation(
            @PathVariable @Positive Long bookId,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) {
        return ResponseDTO.ok(financeOrderReconciliationService.page(bookId, pageNum, pageSize));
    }

    @Operation(summary = "导出单据历史核对CSV报告")
    @GetMapping("/{bookId}/reconciliation/export")
    @PreAuthorize("@fortune.bookVisitorPermission(#bookId)")
    public void exportReconciliation(HttpServletResponse response, @PathVariable @Positive Long bookId)
            throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=finance-order-reconciliation.csv");
        financeOrderReconciliationService.writeCsv(bookId, response.getOutputStream());
    }

    @Operation(summary = "查询单据选择器")
    @GetMapping("/{bookId}/options")
    @PreAuthorize("@fortune.bookVisitorPermission(#bookId)")
    public ResponseDTO<PageDTO<FinanceOrderDetailVo>> options(@PathVariable @Positive Long bookId,
                                                              @RequestParam Integer billType,
                                                              @RequestParam(required = false) String keyword,
                                                              Integer pageNum, Integer pageSize) {
        FinanceOrderOptionQuery query = new FinanceOrderOptionQuery();
        query.setBookId(bookId);
        query.setBillType(billType);
        query.setKeyword(keyword);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        return ResponseDTO.ok(financeOrderQueryService.options(query));
    }

    @Operation(summary = "查询单据详情")
    @GetMapping("/{bookId}/{orderId}")
    @PreAuthorize("@fortune.bookVisitorPermission(#bookId)")
    public ResponseDTO<FinanceOrderDetailVo> detail(@PathVariable @Positive Long bookId,
                                                    @PathVariable @Positive Long orderId) {
        return ResponseDTO.ok(financeOrderQueryService.detail(bookId, orderId));
    }

    @Operation(summary = "分页查询单据流水")
    @GetMapping("/{bookId}/{orderId}/entries")
    @PreAuthorize("@fortune.bookVisitorPermission(#bookId)")
    public ResponseDTO<PageDTO<FinanceOrderEntryVo>> entries(@PathVariable @Positive Long bookId,
                                                             @PathVariable @Positive Long orderId,
                                                             Integer pageNum, Integer pageSize,
                                                             @RequestParam(required = false)
                                                             FinanceOrderEntryQuery.EntryStatus entryStatus) {
        FinanceOrderEntryQuery query = new FinanceOrderEntryQuery();
        query.setBookId(bookId);
        query.setOrderId(orderId);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        query.setEntryStatus(entryStatus == null ? FinanceOrderEntryQuery.EntryStatus.ACTIVE : entryStatus);
        return ResponseDTO.ok(financeOrderQueryService.entries(query));
    }

    @Operation(summary = "分页查询单据操作审计")
    @GetMapping("/{bookId}/{orderId}/audit")
    @PreAuthorize("@fortune.bookVisitorPermission(#bookId)")
    public ResponseDTO<PageDTO<FinanceOrderAuditVo>> audit(@PathVariable @Positive Long bookId,
                                                           @PathVariable @Positive Long orderId,
                                                           Integer pageNum, Integer pageSize) {
        return ResponseDTO.ok(financeOrderQueryService.audit(bookId, orderId, pageNum, pageSize));
    }

    @Operation(summary = "新建单据")
    @PostMapping("/create")
    @AccessLog(title = "好记-单据管理", businessType = BusinessTypeEnum.ADD)
    @PreAuthorize("@fortune.bookActorPermission(#command.getBookId())")
    public ResponseDTO<FinanceOrderMutationVo> create(@Valid @RequestBody FinanceOrderCreateCommand command) {
        return ResponseDTO.ok(financeOrderRequestService.create(command));
    }

    @Operation(summary = "修改单据元数据")
    @PutMapping("/modify")
    @AccessLog(title = "好记-单据管理", businessType = BusinessTypeEnum.MODIFY)
    @PreAuthorize("@fortune.bookActorPermission(#command.getBookId())")
    public ResponseDTO<FinanceOrderMutationVo> modify(@Valid @RequestBody FinanceOrderMetadataCommand command) {
        return ResponseDTO.ok(financeOrderRequestService.modify(command));
    }

    @Operation(summary = "单据生命周期转换")
    @PostMapping("/{bookId}/{orderId}/{action}")
    @AccessLog(title = "好记-单据管理", businessType = BusinessTypeEnum.MODIFY)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<FinanceOrderMutationVo> transition(@PathVariable @Positive Long bookId,
                                                          @PathVariable @Positive Long orderId,
                                                          @PathVariable String action,
                                                          @RequestBody TransitionRequest request) {
        requirePathMatch(request == null ? null : request.bookId(), bookId);
        requirePathMatch(request == null ? null : request.orderId(), orderId);
        FinanceOrderTransitionCommand command = new FinanceOrderTransitionCommand();
        command.setBookId(bookId);
        command.setOrderId(orderId);
        command.setAction(parseTransitionAction(action));
        command.setRequestId(request == null || request.requestId() == null ? null : request.requestId());
        command.setExpectedVersion(request == null ? null : request.expectedVersion());
        requireTransitionCommand(command);
        return ResponseDTO.ok(financeOrderRequestService.transition(command));
    }

    @Operation(summary = "新增单据关联流水")
    @PostMapping("/{bookId}/{orderId}/entries")
    @AccessLog(title = "好记-单据管理", businessType = BusinessTypeEnum.ADD)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<FinanceOrderMutationVo> addEntry(@PathVariable @Positive Long bookId,
                                                        @PathVariable @Positive Long orderId,
                                                        @RequestPart("data") FinanceOrderEntryCommand command,
                                                        @RequestPart(name = "files", required = false)
                                                        List<MultipartFile> files) {
        requirePathMatch(command.getBookId(), bookId);
        requirePathMatch(command.getOrderId(), orderId);
        command.setBookId(bookId);
        command.setOrderId(orderId);
        requireEntryCommand(command);
        return ResponseDTO.ok(financeOrderRequestService.mutateEntry(entryMutation(
                FinanceOrderEntryMutation.Operation.ADD, bookId, orderId, null, command, files)));
    }

    @Operation(summary = "修改单据关联流水")
    @PutMapping("/{bookId}/{orderId}/entries/{billId}")
    @AccessLog(title = "好记-单据管理", businessType = BusinessTypeEnum.MODIFY)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<FinanceOrderMutationVo> modifyEntry(@PathVariable @Positive Long bookId,
                                                           @PathVariable @Positive Long orderId,
                                                           @PathVariable @Positive Long billId,
                                                           @RequestPart("data") JsonNode dataNode,
                                                           @RequestPart(name = "files", required = false)
                                                           List<MultipartFile> files) {
        FinanceOrderEntryCommand command = readEntryCommand(dataNode);
        Long bodyBillId = dataNode != null && dataNode.hasNonNull("billId") ? dataNode.get("billId").asLong() : null;
        requirePathMatch(bodyBillId, billId);
        requirePathMatch(command.getBookId(), bookId);
        requirePathMatch(command.getOrderId(), orderId);
        command.setBookId(bookId);
        command.setOrderId(orderId);
        requireEntryCommand(command);
        return ResponseDTO.ok(financeOrderRequestService.mutateEntry(entryMutation(
                FinanceOrderEntryMutation.Operation.MODIFY, bookId, orderId, billId, command, files)));
    }

    @Operation(summary = "单据流水状态动作")
    @PostMapping("/{bookId}/{orderId}/entries/{billId}/{action}")
    @AccessLog(title = "好记-单据管理", businessType = BusinessTypeEnum.MODIFY)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<FinanceOrderMutationVo> entryAction(@PathVariable @Positive Long bookId,
                                                           @PathVariable @Positive Long orderId,
                                                           @PathVariable @Positive Long billId,
                                                           @PathVariable String action,
                                                           @RequestBody EntryActionRequest request) {
        requirePathMatch(request == null ? null : request.bookId(), bookId);
        requirePathMatch(request == null ? null : request.orderId(), orderId);
        requirePathMatch(request == null ? null : request.billId(), billId);
        FinanceOrderEntryMutation mutation = FinanceOrderEntryMutation.builder()
                .bookId(bookId)
                .orderId(orderId)
                .billId(billId)
                .operation(parseEntryOperation(action))
                .requestId(request == null || request.requestId() == null ? null : request.requestId())
                .expectedVersion(request == null ? null : request.expectedVersion())
                .build();
        if (mutation.getRequestId() == null || mutation.getRequestId().isBlank()) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "请求幂等ID不能为空");
        }
        if (mutation.getExpectedVersion() == null) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "期望版本号不能为空");
        }
        return ResponseDTO.ok(financeOrderRequestService.mutateEntry(mutation));
    }

    private FinanceOrderEntryMutation entryMutation(FinanceOrderEntryMutation.Operation operation, Long bookId,
                                                    Long orderId, Long billId, FinanceOrderEntryCommand command,
                                                    List<MultipartFile> files) {
        return FinanceOrderEntryMutation.builder()
                .bookId(bookId)
                .orderId(orderId)
                .billId(billId)
                .operation(operation)
                .entryCommand(command)
                .requestId(command.getRequestId())
                .expectedVersion(command.getExpectedVersion())
                .fileList(files)
                .build();
    }

    private FinanceOrderEntryCommand readEntryCommand(JsonNode dataNode) {
        try {
            FinanceOrderEntryCommand command = objectMapper.treeToValue(dataNode, FinanceOrderEntryCommand.class);
            return command == null ? new FinanceOrderEntryCommand() : command;
        } catch (Exception exception) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "流水内容解析失败");
        }
    }

    private void requireEntryCommand(FinanceOrderEntryCommand command) {
        if (command.getRequestId() == null || command.getRequestId().isBlank()) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "请求幂等ID不能为空");
        }
        if (command.getExpectedVersion() == null) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "期望版本号不能为空");
        }
        if (command.getTitle() == null || command.getTitle().isBlank() || command.getTitle().length() > 50) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "标题必须为1到50个字符");
        }
        if (command.getTradeTime() == null) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "交易时间不能为空");
        }
        if (command.getConfirm() == null) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "确认状态不能为空");
        }
    }

    private void requireTransitionCommand(FinanceOrderTransitionCommand command) {
        if (command.getRequestId() == null || command.getRequestId().isBlank()) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "请求幂等ID不能为空");
        }
        if (command.getExpectedVersion() == null) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "期望版本号不能为空");
        }
    }

    private FinanceOrderTransitionCommand.Action parseTransitionAction(String action) {
        return switch (action == null ? "" : action.toLowerCase(Locale.ROOT)) {
            case "close" -> FinanceOrderTransitionCommand.Action.CLOSE;
            case "reopen" -> FinanceOrderTransitionCommand.Action.REOPEN;
            case "remove" -> FinanceOrderTransitionCommand.Action.REMOVE;
            default -> throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                    "不支持的单据生命周期动作: " + action);
        };
    }

    private FinanceOrderEntryMutation.Operation parseEntryOperation(String action) {
        return switch (action == null ? "" : action.toLowerCase(Locale.ROOT)) {
            case "confirm" -> FinanceOrderEntryMutation.Operation.CONFIRM;
            case "unconfirm" -> FinanceOrderEntryMutation.Operation.UNCONFIRM;
            case "void" -> FinanceOrderEntryMutation.Operation.VOID;
            case "restore" -> FinanceOrderEntryMutation.Operation.RESTORE;
            default -> throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                    "不支持的流水动作: " + action);
        };
    }

    private void requirePathMatch(Long bodyValue, Long pathValue) {
        if (bodyValue != null && !Objects.equals(bodyValue, pathValue)) {
            throw new ApiException(ErrorCode.Business.ORDER_BOOK_NOT_MATCH);
        }
    }

    /**
     * 生命周期动作请求体：仅幂等ID与期望版本；book/order可选，提供时必须与路径一致。
     */
    record TransitionRequest(String requestId, Long expectedVersion, Long bookId, Long orderId) {
    }

    /**
     * 流水动作请求体：仅幂等ID与期望版本；路径ID可选，提供时必须与路径一致。
     */
    record EntryActionRequest(String requestId, Long expectedVersion, Long bookId, Long orderId, Long billId) {
    }
}
