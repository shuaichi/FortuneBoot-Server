package com.fortuneboot.controller.fortune;

import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.utils.tree.TreeUtil;
import com.fortuneboot.domain.command.fortune.FortuneTagAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneTagModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.domain.query.fortune.FortuneTagQuery;
import com.fortuneboot.domain.vo.fortune.FortuneTagVo;
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
 * 账本分类
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/2/12 23:50
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/fortune/tag")
@Tag(name = "标签API", description = "账本标签的增删查改")
public class FortuneTagController {

    private final FortuneTagService fortuneTagService;

    @Operation(summary = "查询标签")
    @GetMapping("/getList")
    @PreAuthorize("@fortune.bookOwnerPermission(#query.getBookId())")
    public ResponseDTO<List<FortuneTagVo>> getTagList(@Valid FortuneTagQuery query) {
        List<FortuneTagEntity> list = fortuneTagService.getList(query);
        List<FortuneTagVo> result = list.stream().map(FortuneTagVo::new).toList();
        List<FortuneTagVo> treeNodes = TreeUtil.buildForest(result, FortuneTagVo.class);
        return ResponseDTO.ok(treeNodes);
    }

    @Operation(summary = "查询启用的标签")
    @GetMapping("/{bookId}/{billType}/getEnableList")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<List<FortuneTagVo>> getEnableTagList(@PathVariable Long bookId, @PathVariable Integer billType) {
        List<FortuneTagEntity> list = fortuneTagService.getEnableTagList(bookId, billType);
        List<FortuneTagVo> result = list.stream().map(FortuneTagVo::new).toList();
        List<FortuneTagVo> treeNodes = TreeUtil.buildForest(result, FortuneTagVo.class);
        return ResponseDTO.ok(treeNodes);
    }

    @Operation(summary = "新增标签")
    @PostMapping("/add")
    @PreAuthorize("@fortune.bookOwnerPermission(#addCommand.getBookId)")
    public ResponseDTO<Void> addTag(@Valid @RequestBody FortuneTagAddCommand addCommand) {
        fortuneTagService.add(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改标签")
    @PutMapping("/modify")
    @PreAuthorize("@fortune.bookOwnerPermission(#modifyCommand.getBookId)")
    public ResponseDTO<Void> modifyTag(@Valid @RequestBody FortuneTagModifyCommand modifyCommand) {
        fortuneTagService.modify(modifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "标签移入回收站")
    @PatchMapping("/{bookId}/{tagId}/moveToRecycleBin")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> moveTagToRecycleBin(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.moveToRecycleBin(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{bookId}/{tagId}/remove")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> removeTag(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.remove(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "标签放回原处")
    @PatchMapping("/{bookId}/{tagId}/putBack")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> putBackTag(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.putBack(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改是否可支出")
    @PatchMapping("/{bookId}/{tagId}/modifyCanExpense")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> modifyTagCanExpense(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.modifyCanExpense(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改是否可收入")
    @PatchMapping("/{bookId}/{tagId}/modifyCanIncome")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> modifyTagCanIncome(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.modifyCanIncome(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改是否可转账")
    @PatchMapping("/{bookId}/{tagId}/modifyCanTransfer")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> modifyTagCanTransfer(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.modifyCanTransfer(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改是否启用")
    @PatchMapping("/{bookId}/{tagId}/modifyEnable")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> modifyTagEnable(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.modifyEnable(bookId, tagId);
        return ResponseDTO.ok();
    }
}
