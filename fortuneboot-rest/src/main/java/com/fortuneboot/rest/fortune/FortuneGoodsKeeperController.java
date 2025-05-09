package com.fortuneboot.rest.fortune;

import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import com.fortuneboot.customize.accessLog.AccessLog;
import com.fortuneboot.domain.command.fortune.FortuneGoodsKeeperAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneGoodsKeeperModifyCommand;
import com.fortuneboot.domain.query.fortune.FortuneGoodsKeeperQuery;
import com.fortuneboot.domain.vo.fortune.FortuneGoodsKeeperVo;
import com.fortuneboot.service.fortune.FortuneGoodsKeeperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 归物
 *
 * @author zhangchi118
 * @date 2025/5/6 11:01
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/fortune/goods/keeper")
@Tag(name = "归物", description = "归物相关的增删查改")
public class FortuneGoodsKeeperController {

    private final FortuneGoodsKeeperService fortuneGoodsKeeperService;

    @Operation(summary = "新增物品")
    @GetMapping("/getPage")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    public ResponseDTO<PageDTO<FortuneGoodsKeeperVo>> getPage(FortuneGoodsKeeperQuery query) {
        return ResponseDTO.ok(fortuneGoodsKeeperService.getPage(query));
    }

    @Operation(summary = "新增物品")
    @PostMapping("/add")
    @AccessLog(title = "好记-归物", businessType = BusinessTypeEnum.ADD)
    @PreAuthorize("@fortune.bookActorPermission(#addCommand.getBookId())")
    public ResponseDTO<Void> add(@RequestPart("data") @Valid FortuneGoodsKeeperAddCommand addCommand,
                                 @RequestPart(name = "files", required = false) List<MultipartFile> fileList) {
        addCommand.setFileList(fileList);
        fortuneGoodsKeeperService.add(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改物品")
    @PutMapping("/modify")
    @AccessLog(title = "好记-归物", businessType = BusinessTypeEnum.MODIFY)
    @PreAuthorize("@fortune.bookActorPermission(#command.getBookId())")
    public ResponseDTO<Void> modify(@RequestPart("data") @Valid FortuneGoodsKeeperModifyCommand command,
                                    @RequestPart(name = "files", required = false) List<MultipartFile> fileList) {
        command.setFileList(fileList);
        fortuneGoodsKeeperService.modify(command);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除物品")
    @DeleteMapping("/{bookId}/{keeperId}/remove")
    @AccessLog(title = "好记-归物", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Void> remove(@PathVariable("bookId") Long bookId,@PathVariable Long keeperId) {
        fortuneGoodsKeeperService.remove( bookId,keeperId);
        return ResponseDTO.ok();
    }
}
