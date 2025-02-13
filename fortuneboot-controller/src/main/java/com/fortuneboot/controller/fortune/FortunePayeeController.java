package com.fortuneboot.controller.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.domain.command.fortune.FortunePayeeAddCommand;
import com.fortuneboot.domain.command.fortune.FortunePayeeModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;
import com.fortuneboot.domain.query.fortune.FortunePayeeQuery;
import com.fortuneboot.domain.vo.fortune.FortunePayeeVo;
import com.fortuneboot.service.fortune.FortunePayeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账本分类
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/2/12 23:50
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/fortune/payee")
@Tag(name = "交易对象API", description = "账本交易对象的增删查改")
public class FortunePayeeController {

    private final FortunePayeeService fortunePayeeService;

    @Operation(summary = "查询交易对象")
    @GetMapping("/getPage")
    @PreAuthorize("@fortune.bookOwnerPermission(#query.getBookId())")
    public ResponseDTO<PageDTO<FortunePayeeVo>> getPage(@Valid FortunePayeeQuery query) {
        IPage<FortunePayeeEntity> page = fortunePayeeService.getPage(query);
        List<FortunePayeeVo> record = page.getRecords().stream().map(FortunePayeeVo::new).toList();
        return ResponseDTO.ok(new PageDTO<>(record, page.getTotal()));
    }

    @Operation(summary = "查询启用的交易对象")
    @GetMapping("/{bookId}/{billType}/getEnableList")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<List<FortunePayeeVo>> getEnableList(@PathVariable Long bookId, @PathVariable Integer billType) {
        List<FortunePayeeEntity> list = fortunePayeeService.getEnableList(bookId, billType);
        List<FortunePayeeVo> result = list.stream().map(FortunePayeeVo::new).toList();
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "新增交易对象")
    @PostMapping("/add")
    @PreAuthorize("@fortune.bookOwnerPermission(#addCommand.getBookId)")
    public ResponseDTO<Void> add(@Valid @RequestBody FortunePayeeAddCommand addCommand) {
        fortunePayeeService.add(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "新增交易对象")
    @PutMapping("/modify")
    @PreAuthorize("@fortune.bookOwnerPermission(#modifyCommand.getBookId)")
    public ResponseDTO<Void> modify(@RequestBody FortunePayeeModifyCommand modifyCommand) {
        fortunePayeeService.modify(modifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "交易对象移入回收站")
    @PatchMapping("/{bookId}/{payeeId}/moveToRecycleBin")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> moveToRecycleBin(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.moveToRecycleBin(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除交易对象")
    @DeleteMapping("/{bookId}/{payeeId}/remove")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> remove(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.remove(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "交易对象移出回收站")
    @PatchMapping("/{bookId}/{payeeId}/putBack")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> putBack(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.putBack(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "交易对象可支出")
    @PatchMapping("/{bookId}/{payeeId}/canExpense")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> canExpense(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.canExpense(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "交易对象不可支出")
    @PatchMapping("/{bookId}/{payeeId}/cannotExpense")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> cannotExpense(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.cannotExpense(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "交易对象可收入")
    @PatchMapping("/{bookId}/{payeeId}/canIncome")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> canIncome(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.canIncome(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "交易对象不可收入")
    @PatchMapping("/{bookId}/{payeeId}/cannotIncome")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> cannotIncome(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.cannotIncome(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "启用交易对象")
    @PatchMapping("/{bookId}/{payeeId}/enable")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> enable(@PathVariable Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.enable(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "禁用交易对象")
    @PatchMapping("/{bookId}/{payeeId}/disable")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> disable(@PathVariable Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.disable(bookId, payeeId);
        return ResponseDTO.ok();
    }
}
