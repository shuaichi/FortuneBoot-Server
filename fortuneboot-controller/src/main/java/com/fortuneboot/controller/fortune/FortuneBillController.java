package com.fortuneboot.controller.fortune;

import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import com.fortuneboot.customize.accessLog.AccessLog;
import com.fortuneboot.domain.bo.fortune.FortuneBillBo;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillModifyCommand;
import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillVo;
import com.fortuneboot.service.fortune.FortuneBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 22:39
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/fortune/bill")
@Tag(name = "账单API", description = "账单相关的增删查改")
public class FortuneBillController {

    private final FortuneBillService fortuneBillService;

    @Operation(summary = "分页查询账单")
    @GetMapping("/getPage")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    public ResponseDTO<PageDTO<FortuneBillVo>> getPage(@Valid FortuneBillQuery query) {
        PageDTO<FortuneBillBo> page = fortuneBillService.getPage(query);
        List<FortuneBillVo> records = page.getRows().stream().map(FortuneBillVo::new).toList();
        return ResponseDTO.ok(new PageDTO<>(records, page.getTotal()));
    }

    @Operation(summary = "新增账单")
    @PostMapping("/add")
    @AccessLog(title = "好记-账单管理", businessType = BusinessTypeEnum.ADD)
    @PreAuthorize("@fortune.bookActorPermission(#addCommand.getBookId())")
    public ResponseDTO<Void> add(@RequestPart("data") @Valid FortuneBillAddCommand addCommand,
                                 @RequestPart(name = "files",required = false) List<MultipartFile> fileList) {
        addCommand.setFileList(fileList);
        fortuneBillService.add(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改账单")
    @PutMapping("/modify")
    @AccessLog(title = "好记-账单管理", businessType = BusinessTypeEnum.MODIFY)
    @PreAuthorize("@fortune.bookActorPermission(#modifyCommand.getBookId())")
    public ResponseDTO<Void> modify(@RequestPart("data") @Valid FortuneBillModifyCommand modifyCommand ,
                                    @RequestPart(name = "files",required = false) List<MultipartFile> fileList) {
        modifyCommand.setFileList(fileList);
        fortuneBillService.modify(modifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除账单")
    @DeleteMapping("/{bookId}/{billId}/remove")
    @AccessLog(title = "好记-账单管理", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> remove(@PathVariable @Positive Long bookId, @PathVariable @Positive Long billId) {
        fortuneBillService.remove(bookId, billId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "确认账单")
    @PatchMapping("/{bookId}/{billId}/confirm")
    @AccessLog(title = "好记-账单管理-确认账单", businessType = BusinessTypeEnum.OTHER)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> confirm(@PathVariable @Positive Long bookId, @PathVariable @Positive Long billId) {
        fortuneBillService.confirm(bookId, billId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "取消确认账单")
    @PatchMapping("/{bookId}/{billId}/unConfirm")
    @AccessLog(title = "好记-账单管理-取消确认账单", businessType = BusinessTypeEnum.OTHER)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> unConfirm(@PathVariable @Positive Long bookId, @PathVariable @Positive Long billId) {
        fortuneBillService.unConfirm(bookId, billId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "统计账单")
    @PatchMapping("/{bookId}/{billId}/include")
    @AccessLog(title = "好记-账单管理", businessType = BusinessTypeEnum.INCLUDE)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> include(@PathVariable @Positive Long bookId, @PathVariable @Positive Long billId) {
        fortuneBillService.include(bookId, billId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "取消统计账单")
    @PatchMapping("/{bookId}/{billId}/exclude")
    @AccessLog(title = "好记-账单管理", businessType = BusinessTypeEnum.EXCLUDE)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> exclude(@PathVariable @Positive Long bookId, @PathVariable @Positive Long billId) {
        fortuneBillService.exclude(bookId, billId);
        return ResponseDTO.ok();
    }
}
