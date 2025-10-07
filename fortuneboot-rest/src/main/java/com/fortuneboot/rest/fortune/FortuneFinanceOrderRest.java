package com.fortuneboot.rest.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import com.fortuneboot.customize.accessLog.AccessLog;
import com.fortuneboot.domain.command.fortune.FortuneFinanceOrderAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneFinanceOrderModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.query.fortune.FortuneFinanceOrderQuery;
import com.fortuneboot.domain.vo.fortune.FortuneFinanceOrderVo;
import com.fortuneboot.service.fortune.FortuneFinanceOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @author work.chi.zhang@gmail.com
 * @date 2025/8/16 18:19
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/fortune/finance/order")
@Tag(name = "单据", description = "单据相关的增删查改")
public class FortuneFinanceOrderRest {

    private final FortuneFinanceOrderService fortuneFinanceOrderService;

    @Operation(summary = "分页查询单据")
    @GetMapping("/getPage")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    public ResponseDTO<PageDTO<FortuneFinanceOrderVo>> getPage(FortuneFinanceOrderQuery query) {
        IPage<FortuneFinanceOrderEntity> page = fortuneFinanceOrderService.getPage(query);
        List<FortuneFinanceOrderVo> records = page.getRecords().stream().map(FortuneFinanceOrderVo::new).toList();
        return ResponseDTO.ok(new PageDTO<>(records, page.getTotal()));
    }

    @Operation(summary = "新增单据")
    @PostMapping("/add")
    @AccessLog(title = "好记-单据管理", businessType = BusinessTypeEnum.ADD)
    @PreAuthorize("@fortune.bookActorPermission(#command.getBookId())")
    public ResponseDTO<Boolean> add(@Valid FortuneFinanceOrderAddCommand command) {
        fortuneFinanceOrderService.add(command);
        return ResponseDTO.ok(Boolean.TRUE);
    }

    @Operation(summary = "修改单据")
    @PutMapping("/modify")
    @AccessLog(title = "好记-单据管理", businessType = BusinessTypeEnum.MODIFY)
    @PreAuthorize("@fortune.bookActorPermission(#command.getBookId())")
    public ResponseDTO<Boolean> modify(@Valid FortuneFinanceOrderModifyCommand command) {
        fortuneFinanceOrderService.modify(command);
        return ResponseDTO.ok(Boolean.TRUE);
    }

    @Operation(summary = "删除单据")
    @DeleteMapping("/{bookId}/{orderId}/remove")
    @AccessLog(title = "好记-单据管理", businessType = BusinessTypeEnum.MODIFY)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Boolean> remove(@PathVariable @Positive Long bookId, @PathVariable @Positive Long orderId) {
        fortuneFinanceOrderService.remove(bookId, orderId);
        return ResponseDTO.ok(Boolean.TRUE);
    }

    @Operation(summary = "使用单据")
    @PatchMapping("/{bookId}/{orderId}/using")
    @AccessLog(title = "好记-单据管理", businessType = BusinessTypeEnum.USING_ORDER)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Boolean> using(@PathVariable @Positive Long bookId, @PathVariable @Positive Long orderId) {
        fortuneFinanceOrderService.using(bookId, orderId);
        return ResponseDTO.ok(Boolean.TRUE);
    }

    @Operation(summary = "关闭单据")
    @PatchMapping("/{bookId}/{orderId}/close")
    @AccessLog(title = "好记-单据管理", businessType = BusinessTypeEnum.CLOSE_ORDER)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Boolean> close(@PathVariable @Positive Long bookId, @PathVariable @Positive Long orderId) {
        fortuneFinanceOrderService.close(bookId, orderId);
        return ResponseDTO.ok(Boolean.TRUE);
    }

    @Operation(summary = "重开单据")
    @PatchMapping("/{bookId}/{orderId}/reopen")
    @AccessLog(title = "好记-单据管理", businessType = BusinessTypeEnum.REOPEN_ORDER)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Boolean> reopen(@PathVariable @Positive Long bookId, @PathVariable @Positive Long orderId) {
        fortuneFinanceOrderService.reopen(bookId, orderId);
        return ResponseDTO.ok(Boolean.TRUE);
    }

}
