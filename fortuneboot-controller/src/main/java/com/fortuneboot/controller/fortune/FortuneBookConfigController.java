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

    @Operation(summary = "查询账本标签")
    @GetMapping("/tag/getList")
    @PreAuthorize("@fortune.bookOwnerPermission(#query.getBookId())")
    public ResponseDTO<List<FortuneTagVo>> getTagList(FortuneTagQuery query) {
        List<FortuneTagEntity> list = fortuneTagService.getTagList(query);
        List<FortuneTagVo> result = list.stream().map(FortuneTagVo::new).toList();
        List<FortuneTagVo> treeNodes = TreeUtil.buildForest(result, FortuneTagVo.class);
        return ResponseDTO.ok(treeNodes);
    }

    @Operation(summary = "新增账本标签")
    @PostMapping("/tag/add")
    @PreAuthorize("@fortune.bookOwnerPermission(#addCommand.getBookId)")
    public ResponseDTO<Void> addTag(@Valid @RequestBody FortuneTagAddCommand addCommand) {
        fortuneTagService.addTag(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改账本标签")
    @PutMapping("/tag/modify")
    @PreAuthorize("@fortune.bookOwnerPermission(#modifyCommand.getBookId)")
    public ResponseDTO<Void> modifyTag(@Valid @RequestBody FortuneTagModifyCommand modifyCommand) {
        fortuneTagService.modifyTag(modifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "账本标签移入回收站")
    @PutMapping("/tag/moveToRecycleBin/{bookId}/{tagId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> moveToRecycleBin(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.moveToRecycleBin(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除账本标签")
    @PutMapping("/tag/delete/{bookId}/{tagId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> deleteTag(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.deleteTag(bookId, tagId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "放回原处")
    @PutMapping("/tag/putBack/{bookId}/{tagId}")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> putBack(@PathVariable @Positive Long bookId, @PathVariable @Positive Long tagId) {
        fortuneTagService.putBack(bookId,tagId);
        return ResponseDTO.ok();
    }
}
