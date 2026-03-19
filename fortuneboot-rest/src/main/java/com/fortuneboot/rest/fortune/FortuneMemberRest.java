package com.fortuneboot.rest.fortune;

import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import com.fortuneboot.customize.accessLog.AccessLog;
import com.fortuneboot.domain.command.fortune.FortuneMemberAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneMemberModifyCommand;
import com.fortuneboot.domain.query.fortune.FortuneMemberQuery;
import com.fortuneboot.domain.vo.fortune.FortuneMemberVo;
import com.fortuneboot.service.fortune.FortuneMemberService;
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
 * @author zhangchi118
 * @date 2026/3/19 09:40
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/fortune/member")
@Tag(name = "成员API", description = "账单消费成员增删改查")
public class FortuneMemberRest {

    private final FortuneMemberService fortuneMemberService;

    @Operation(summary = "分页查询成员")
    @GetMapping("/getPage")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    public ResponseDTO<PageDTO<FortuneMemberVo>> getPage(@Valid FortuneMemberQuery query) {
        return ResponseDTO.ok(fortuneMemberService.getPage(query));
    }

    @Operation(summary = "查询启用的成员列表")
    @GetMapping("/{bookId}/getEnableList")
    @PreAuthorize("@fortune.bookVisitorPermission(#bookId)")
    public ResponseDTO<List<FortuneMemberVo>> getEnableList(@PathVariable Long bookId) {
        return ResponseDTO.ok(fortuneMemberService.getEnableList(bookId));
    }

    @Operation(summary = "新增成员")
    @PostMapping("/add")
    @AccessLog(title = "好记-成员管理", businessType = BusinessTypeEnum.ADD)
    @PreAuthorize("@fortune.bookActorPermission(#addCommand.getBookId())")
    public ResponseDTO<Void> add(@Valid @RequestBody FortuneMemberAddCommand addCommand) {
        fortuneMemberService.add(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改成员")
    @PutMapping("/modify")
    @AccessLog(title = "好记-成员管理", businessType = BusinessTypeEnum.MODIFY)
    @PreAuthorize("@fortune.bookActorPermission(#modifyCommand.getBookId())")
    public ResponseDTO<Void> modify(@RequestBody @Valid FortuneMemberModifyCommand modifyCommand) {
        fortuneMemberService.modify(modifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "成员移入回收站")
    @PatchMapping("/{bookId}/{memberId}/moveToRecycleBin")
    @AccessLog(title = "好记-成员管理", businessType = BusinessTypeEnum.MOVE_TO_RECYCLE_BIN)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> moveToRecycleBin(@PathVariable @Positive Long bookId, @PathVariable @Positive Long memberId) {
        fortuneMemberService.moveToRecycleBin(bookId, memberId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "成员移出回收站")
    @PatchMapping("/{bookId}/{memberId}/putBack")
    @AccessLog(title = "好记-成员管理", businessType = BusinessTypeEnum.PUT_BACK)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> putBack(@PathVariable @Positive Long bookId, @PathVariable @Positive Long memberId) {
        fortuneMemberService.putBack(bookId, memberId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除成员")
    @DeleteMapping("/{bookId}/{memberId}/remove")
    @AccessLog(title = "好记-成员管理", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> remove(@PathVariable @Positive Long bookId, @PathVariable @Positive Long memberId) {
        fortuneMemberService.remove(bookId, memberId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "启用成员")
    @PatchMapping("/{bookId}/{memberId}/enable")
    @AccessLog(title = "好记-成员管理", businessType = BusinessTypeEnum.ENABLE)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> enable(@PathVariable Long bookId, @PathVariable @Positive Long memberId) {
        fortuneMemberService.enable(bookId, memberId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "停用成员")
    @PatchMapping("/{bookId}/{memberId}/disable")
    @AccessLog(title = "好记-成员管理", businessType = BusinessTypeEnum.DISABLE)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> disable(@PathVariable Long bookId, @PathVariable @Positive Long memberId) {
        fortuneMemberService.disable(bookId, memberId);
        return ResponseDTO.ok();
    }
}
