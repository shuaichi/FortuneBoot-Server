package com.fortuneboot.rest.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import com.fortuneboot.common.utils.tree.TreeUtil;
import com.fortuneboot.customize.accessLog.AccessLog;
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

    @Operation(summary = "分页查询标签")
    @GetMapping("/getPage")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    public ResponseDTO<PageDTO<FortuneTagVo>> getPage(@Valid FortuneTagQuery query) {
        return ResponseDTO.ok(fortuneTagService.getPage(query));
    }

    @Operation(summary = "查询标签")
    @GetMapping("/getList")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    public ResponseDTO<List<FortuneTagVo>> getList(@Valid FortuneTagQuery query) {
        List<FortuneTagEntity> list = fortuneTagService.getList(query);
        List<FortuneTagVo> result = list.stream().map(FortuneTagVo::new).toList();
        List<FortuneTagVo> treeNodes = TreeUtil.buildForest(result, FortuneTagVo.class);
        return ResponseDTO.ok(treeNodes);
    }

    @Operation(summary = "平铺查询标签")
    @GetMapping("/getListPage")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    public ResponseDTO<PageDTO<FortuneTagVo>> getListPage(@Valid FortuneTagQuery query) {
        IPage<FortuneTagEntity> page = fortuneTagService.getListPage(query);
        List<FortuneTagVo> records = page.getRecords().stream().map(FortuneTagVo::new).toList();
        return ResponseDTO.ok(new PageDTO<>(records, page.getTotal()));
    }

    @Operation(summary = "查询启用的标签")
    @GetMapping("/{bookId}/getEnableList")
    @PreAuthorize("@fortune.bookVisitorPermission(#bookId)")
    public ResponseDTO<List<FortuneTagVo>> getEnableList(@PathVariable Long bookId, @RequestParam(required = false) Integer billType) {
        List<FortuneTagEntity> list = fortuneTagService.getEnableList(bookId, billType);
        List<FortuneTagVo> result = list.stream().map(FortuneTagVo::new).toList();
        List<FortuneTagVo> treeNodes = TreeUtil.buildForest(result, FortuneTagVo.class);
        return ResponseDTO.ok(treeNodes);
    }

    @Operation(summary = "新增标签")
    @PostMapping("/add")
    @AccessLog(title = "好记-标签管理", businessType = BusinessTypeEnum.ADD)
    @PreAuthorize("@fortune.bookActorPermission(#addCommand.getBookId)")
    public ResponseDTO<Void> add(@Valid @RequestBody FortuneTagAddCommand addCommand) {
        fortuneTagService.add(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改标签")
    @PutMapping("/modify")
    @AccessLog(title = "好记-标签管理", businessType = BusinessTypeEnum.MODIFY)
    @PreAuthorize("@fortune.bookActorPermission(#modifyCommand.getBookId)")
    public ResponseDTO<Void> modify(@Valid @RequestBody FortuneTagModifyCommand modifyCommand) {
        fortuneTagService.modify(modifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "标签移入回收站")
    @PatchMapping("/{bookId}/{tagId}/moveToRecycleBin")
    @AccessLog(title = "好记-标签管理", businessType = BusinessTypeEnum.MOVE_TO_RECYCLE_BIN)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> moveTagToRecycleBin(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.moveToRecycleBin(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{bookId}/{tagId}/remove")
    @AccessLog(title = "好记-标签管理", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> remove(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.remove(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "标签移出回收站")
    @PatchMapping("/{bookId}/{tagId}/putBack")
    @AccessLog(title = "好记-标签管理", businessType = BusinessTypeEnum.PUT_BACK)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> putBack(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.putBack(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "标签可支出")
    @PatchMapping("/{bookId}/{tagId}/canExpense")
    @AccessLog(title = "好记-标签管理", businessType = BusinessTypeEnum.CAN_EXPENSE)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> canExpense(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.canExpense(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "标签不可支出")
    @PatchMapping("/{bookId}/{tagId}/cannotExpense")
    @AccessLog(title = "好记-标签管理", businessType = BusinessTypeEnum.CAN_NOT_EXPENSE)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> cannotExpense(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.cannotExpense(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "标签可收入")
    @PatchMapping("/{bookId}/{tagId}/canIncome")
    @AccessLog(title = "好记-标签管理", businessType = BusinessTypeEnum.CAN_INCOME)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> canIncome(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.canIncome(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "标签不可收入")
    @PatchMapping("/{bookId}/{tagId}/cannotIncome")
    @AccessLog(title = "好记-标签管理", businessType = BusinessTypeEnum.CAN_NOT_INCOME)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> cannotIncome(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.cannotIncome(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "标签可转账")
    @PatchMapping("/{bookId}/{tagId}/canTransfer")
    @AccessLog(title = "好记-标签管理", businessType = BusinessTypeEnum.CAN_TRANSFER)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> canTransfer(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.canTransfer(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "标签不可转账")
    @PatchMapping("/{bookId}/{tagId}/cannotTransfer")
    @AccessLog(title = "好记-标签管理", businessType = BusinessTypeEnum.CAN_NOT_TRANSFER)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> cannotTransfer(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.cannotTransfer(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "标签启用")
    @PatchMapping("/{bookId}/{tagId}/enable")
    @AccessLog(title = "好记-标签管理", businessType = BusinessTypeEnum.ENABLE)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> enable(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.enable(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "标签停用")
    @PatchMapping("/{bookId}/{tagId}/disable")
    @AccessLog(title = "好记-标签管理", businessType = BusinessTypeEnum.DISABLE)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> disable(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.disable(bookId, tagId);
        return ResponseDTO.ok();
    }
}
