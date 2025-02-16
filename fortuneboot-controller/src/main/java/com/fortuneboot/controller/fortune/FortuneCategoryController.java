package com.fortuneboot.controller.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.utils.tree.TreeUtil;
import com.fortuneboot.domain.command.fortune.FortuneCategoryAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneCategoryModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.domain.query.fortune.FortuneCategoryQuery;
import com.fortuneboot.domain.vo.fortune.FortuneCategoryVo;
import com.fortuneboot.service.fortune.FortuneCategoryService;
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
@RequestMapping("/fortune/category")
@Tag(name = "分类API", description = "账本分类的增删查改")
public class FortuneCategoryController {

    private final FortuneCategoryService fortuneCategoryService;

    @Operation(summary = "分页查询分类")
    @GetMapping("/getPage")
    @PreAuthorize("@fortune.bookOwnerPermission(#query.getBookId())")
    public ResponseDTO<PageDTO<FortuneCategoryVo>> getPage(@Valid FortuneCategoryQuery query) {
        return ResponseDTO.ok(fortuneCategoryService.getPage(query));
    }

    @Operation(summary = "查询分类")
    @GetMapping("/getList")
    @PreAuthorize("@fortune.bookOwnerPermission(#query.getBookId())")
    public ResponseDTO<List<FortuneCategoryVo>> getList(@Valid FortuneCategoryQuery query) {
        List<FortuneCategoryEntity> list = fortuneCategoryService.getList(query);
        List<FortuneCategoryVo> result = list.stream().map(FortuneCategoryVo::new).toList();
        List<FortuneCategoryVo> treeNodes = TreeUtil.buildForest(result, FortuneCategoryVo.class);
        return ResponseDTO.ok(treeNodes);
    }

    @Operation(summary = "分页查询分类（平铺）")
    @GetMapping("/getListPage")
    @PreAuthorize("@fortune.bookOwnerPermission(#query.getBookId())")
    public ResponseDTO<PageDTO<FortuneCategoryVo>> getListPageApi(@Valid FortuneCategoryQuery query) {
        IPage<FortuneCategoryEntity> page = fortuneCategoryService.getListPageApi(query);
        List<FortuneCategoryVo> records = page.getRecords().stream().map(FortuneCategoryVo::new).toList();
        return ResponseDTO.ok(new PageDTO<>(records, page.getTotal()));
    }

    @Operation(summary = "查询启用的分类")
    @GetMapping("/{bookId}/{billType}/getEnableList")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<List<FortuneCategoryVo>> getEnableCategoryList(@PathVariable Long bookId, @PathVariable Integer billType) {
        List<FortuneCategoryEntity> list = fortuneCategoryService.getEnableCategoryList(bookId, billType);
        List<FortuneCategoryVo> result = list.stream().map(FortuneCategoryVo::new).toList();
        List<FortuneCategoryVo> treeNodes = TreeUtil.buildForest(result, FortuneCategoryVo.class);
        return ResponseDTO.ok(treeNodes);
    }

    @Operation(summary = "新增分类")
    @PostMapping("/add")
    @PreAuthorize("@fortune.bookOwnerPermission(#addCommand.getBookId)")
    public ResponseDTO<Void> add(@Valid @RequestBody FortuneCategoryAddCommand addCommand) {
        fortuneCategoryService.add(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改分类")
    @PutMapping("/modify")
    @PreAuthorize("@fortune.bookOwnerPermission(#modifyCommand.getBookId)")
    public ResponseDTO<Void> modify(@Valid @RequestBody FortuneCategoryModifyCommand modifyCommand) {
        fortuneCategoryService.modify(modifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "分类移入回收站")
    @PatchMapping("/{bookId}/{categoryId}/moveToRecycleBin")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> moveToRecycleBin(@PathVariable @Positive Long bookId, @PathVariable @Positive Long categoryId) {
        fortuneCategoryService.moveToRecycleBin(bookId, categoryId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "分类标签")
    @DeleteMapping("/{bookId}/{categoryId}/remove")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> remove(@PathVariable @Positive Long bookId, @PathVariable @Positive Long categoryId) {
        fortuneCategoryService.remove(bookId, categoryId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "分类移出回收站")
    @PatchMapping("/{bookId}/{categoryId}/putBack")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> putBack(@PathVariable @Positive Long bookId, @PathVariable @Positive Long categoryId) {
        fortuneCategoryService.putBack(bookId, categoryId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "启用分类")
    @PatchMapping("/{bookId}/{categoryId}/enable")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> enable(@PathVariable @Positive Long bookId, @PathVariable @Positive Long categoryId) {
        fortuneCategoryService.enable(bookId, categoryId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "启用分类")
    @PatchMapping("/{bookId}/{categoryId}/disable")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> disable(@PathVariable @Positive Long bookId, @PathVariable @Positive Long categoryId) {
        fortuneCategoryService.disable(bookId, categoryId);
        return ResponseDTO.ok();
    }
}
