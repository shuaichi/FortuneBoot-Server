package com.fortuneboot.controller.fortune;

import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.utils.tree.TreeUtil;
import com.fortuneboot.domain.command.fortune.*;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.domain.query.fortune.FortuneCategoryQuery;
import com.fortuneboot.domain.query.fortune.FortunePayeeQuery;
import com.fortuneboot.domain.query.fortune.FortuneTagQuery;
import com.fortuneboot.domain.vo.fortune.FortuneCategoryVo;
import com.fortuneboot.domain.vo.fortune.FortunePayeeVo;
import com.fortuneboot.domain.vo.fortune.FortuneTagVo;
import com.fortuneboot.service.fortune.FortuneCategoryService;
import com.fortuneboot.service.fortune.FortunePayeeService;
import com.fortuneboot.service.fortune.FortuneTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账本配置API
 *
 * @author zhangchi118
 * @date 2024/12/11 16:01
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/fortune/book/config")
@Tag(name = "账本配置API", description = "账本配置相关的增删查改")
public class FortuneBookConfigController {

    private final FortuneTagService fortuneTagService;

    private final FortunePayeeService fortunePayeeService;

    private final FortuneCategoryService fortuneCategoryService;

    @Operation(summary = "查询标签")
    @GetMapping("/tag/getList")
    @PreAuthorize("@fortune.bookOwnerPermission(#query.getBookId())")
    public ResponseDTO<List<FortuneTagVo>> getTagList(@Valid @RequestBody FortuneTagQuery query) {
        List<FortuneTagEntity> list = fortuneTagService.getList(query);
        List<FortuneTagVo> result = list.stream().map(FortuneTagVo::new).toList();
        List<FortuneTagVo> treeNodes = TreeUtil.buildForest(result, FortuneTagVo.class);
        return ResponseDTO.ok(treeNodes);
    }

    @Operation(summary = "新增标签")
    @PostMapping("/tag/add")
    @PreAuthorize("@fortune.bookOwnerPermission(#addCommand.getBookId)")
    public ResponseDTO<Void> addTag(@Valid @RequestBody FortuneTagAddCommand addCommand) {
        fortuneTagService.add(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改标签")
    @PutMapping("/tag/modify")
    @PreAuthorize("@fortune.bookOwnerPermission(#modifyCommand.getBookId)")
    public ResponseDTO<Void> modifyTag(@Valid @RequestBody FortuneTagModifyCommand modifyCommand) {
        fortuneTagService.modify(modifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "标签移入回收站")
    @PatchMapping("/tag/moveToRecycleBin/{bookId}/{tagId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> moveTagToRecycleBin(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.moveToRecycleBin(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/tag/remove/{bookId}/{tagId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> removeTag(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.remove(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "标签放回原处")
    @PatchMapping("/tag/putBack/{bookId}/{tagId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> putBackTag(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.putBack(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改是否可支出")
    @PatchMapping("/tag/modifyCanExpense/{bookId}/{tagId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> modifyTagCanExpense(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.modifyCanExpense(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改是否可收入")
    @PatchMapping("/tag/modifyCanIncome/{bookId}/{tagId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> modifyTagCanIncome(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.modifyCanIncome(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改是否可转账")
    @PatchMapping("/tag/modifyCanTransfer/{bookId}/{tagId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> modifyTagCanTransfer(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.modifyCanTransfer(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改是否启用")
    @PatchMapping("/tag/modifyEnable/{bookId}/{tagId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> modifyTagEnable(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.modifyEnable(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "查询交易对象")
    @GetMapping("/payee/getList")
    @PreAuthorize("@fortune.bookOwnerPermission(#query.getBookId())")
    public ResponseDTO<List<FortunePayeeVo>> getPayeeList(@Valid @RequestBody FortunePayeeQuery query) {
        List<FortunePayeeEntity> list = fortunePayeeService.getList(query);
        List<FortunePayeeVo> result = list.stream().map(FortunePayeeVo::new).toList();
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "新增交易对象")
    @PostMapping("/payee/add")
    @PreAuthorize("@fortune.bookOwnerPermission(#addCommand.getBookId)")
    public ResponseDTO<Void> addPayee(@Valid @RequestBody FortunePayeeAddCommand addCommand) {
        fortunePayeeService.add(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "新增交易对象")
    @PutMapping("/payee/modify")
    @PreAuthorize("@fortune.bookOwnerPermission(#modifyCommand.getBookId)")
    public ResponseDTO<Void> modifyPayee(@RequestBody FortunePayeeModifyCommand modifyCommand) {
        fortunePayeeService.modify(modifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "交易对象移入回收站")
    @PatchMapping("/payee/moveToRecycleBin/{bookId}/{payeeId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> movePayeeToRecycleBin(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.moveToRecycleBin(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除交易对象")
    @DeleteMapping("/payee/remove/{bookId}/{payeeId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> removePayee(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.remove(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "交易对象放回原处")
    @PatchMapping("/payee/putBack/{bookId}/{payeeId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> putBackPayee(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.putBack(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "交易对象是否可支出")
    @PatchMapping("/payee/modifyCanExpense/{bookId}/{payeeId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> modifyPayeeCanExpense(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.modifyCanExpense(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "交易对象是否可收入")
    @PatchMapping("/payee/modifyCanIncome/{bookId}/{payeeId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> modifyPayeeCanInCome(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.modifyCanIncome(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "交易对象是否可收入")
    @PatchMapping("/payee/modifyEnable/{bookId}/{payeeId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> modifyPayeeEnable(@PathVariable @Positive Long bookId, @PathVariable @Positive Long payeeId) {
        fortunePayeeService.modifyEnable(bookId, payeeId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "查询分类")
    @GetMapping("/category/getList")
    @PreAuthorize("@fortune.bookOwnerPermission(#query.getBookId())")
    public ResponseDTO<List<FortuneCategoryVo>> getCategoryList(@Valid @RequestBody FortuneCategoryQuery query) {
        List<FortuneCategoryEntity> list = fortuneCategoryService.getList(query);
        List<FortuneCategoryVo> result = list.stream().map(FortuneCategoryVo::new).toList();
        List<FortuneCategoryVo> treeNodes = TreeUtil.buildForest(result, FortuneCategoryVo.class);
        return ResponseDTO.ok(treeNodes);
    }

    @Operation(summary = "新增分类")
    @PostMapping("/category/add")
    @PreAuthorize("@fortune.bookOwnerPermission(#addCommand.getBookId)")
    public ResponseDTO<Void> addCategory(@Valid @RequestBody FortuneCategoryAddCommand addCommand) {
        fortuneCategoryService.add(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改分类")
    @PutMapping("/category/modify")
    @PreAuthorize("@fortune.bookOwnerPermission(#modifyCommand.getBookId)")
    public ResponseDTO<Void> modifyCategory(@Valid @RequestBody FortuneCategoryModifyCommand modifyCommand) {
        fortuneCategoryService.modify(modifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "分类移入回收站")
    @PatchMapping("/category/moveToRecycleBin/{bookId}/{categoryId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> moveCategoryToRecycleBin(@PathVariable @Positive Long bookId, @PathVariable @Positive Long categoryId) {
        fortuneCategoryService.moveToRecycleBin(bookId, categoryId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "分类标签")
    @DeleteMapping("/category/remove/{bookId}/{categoryId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> removeCategory(@PathVariable @Positive Long bookId, @PathVariable @Positive Long categoryId) {
        fortuneCategoryService.remove(bookId, categoryId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "分类放回原处")
    @PatchMapping("/category/putBack/{bookId}/{categoryId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> putBackCategory(@PathVariable @Positive Long bookId, @PathVariable @Positive Long categoryId) {
        fortuneCategoryService.putBack(bookId, categoryId);
        return ResponseDTO.ok();
    }
}
