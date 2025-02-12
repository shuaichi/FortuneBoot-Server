package com.fortuneboot.controller.fortune;

import com.fortuneboot.common.core.dto.ResponseDTO;
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
    @GetMapping("/getList")
    @PreAuthorize("@fortune.bookOwnerPermission(#query.getBookId())")
    public ResponseDTO<List<FortunePayeeVo>> getPayeeList(@Valid FortunePayeeQuery query) {
        List<FortunePayeeEntity> list = fortunePayeeService.getList(query);
        List<FortunePayeeVo> result = list.stream().map(FortunePayeeVo::new).toList();
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "查询启用的交易对象")
    @GetMapping("/{bookId}/{billType}/getEnableList")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<List<FortunePayeeVo>> getEnablePayeeList(@PathVariable Long bookId, @PathVariable Integer billType) {
        List<FortunePayeeEntity> list = fortunePayeeService.getEnablePayeeList(bookId, billType);
        List<FortunePayeeVo> result = list.stream().map(FortunePayeeVo::new).toList();
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "新增交易对象")
    @PostMapping("/add")
    @PreAuthorize("@fortune.bookOwnerPermission(#addCommand.getBookId)")
    public ResponseDTO<Void> addPayee(@Valid @RequestBody FortunePayeeAddCommand addCommand) {
        fortunePayeeService.add(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "新增交易对象")
    @PutMapping("/modify")
    @PreAuthorize("@fortune.bookOwnerPermission(#modifyCommand.getBookId)")
    public ResponseDTO<Void> modifyPayee(@RequestBody FortunePayeeModifyCommand modifyCommand) {
        fortunePayeeService.modify(modifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "交易对象移入回收站")
    @PatchMapping("/{bookId}/{payeeId}/moveToRecycleBin")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> movePayeeToRecycleBin(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.moveToRecycleBin(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除交易对象")
    @DeleteMapping("/{bookId}/{payeeId}/remove")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> removePayee(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.remove(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "交易对象放回原处")
    @PatchMapping("/{bookId}/{payeeId}/putBack")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> putBackPayee(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.putBack(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "交易对象是否可支出")
    @PatchMapping("/{bookId}/{payeeId}/modifyCanExpense")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> modifyPayeeCanExpense(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.modifyCanExpense(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "交易对象是否可收入")
    @PatchMapping("/{bookId}/{payeeId}/modifyCanIncome")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> modifyPayeeCanInCome(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.modifyCanIncome(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "交易对象是否可收入")
    @PatchMapping("/{bookId}/{payeeId}/modifyEnable")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> modifyPayeeEnable(@PathVariable Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.modifyEnable(bookId, payeeId);
        return ResponseDTO.ok();
    }
}
