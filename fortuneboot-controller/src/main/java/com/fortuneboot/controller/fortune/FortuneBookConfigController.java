package com.fortuneboot.controller.fortune;

import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.utils.tree.TreeUtil;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.domain.query.fortune.FortuneTagQuery;
import com.fortuneboot.domain.vo.fortune.FortuneTagVo;
import com.fortuneboot.service.fortune.FortuneTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping("/getPage")
    @PreAuthorize("@fortune.bookOwnerPermission(#query.getBookId())")
    public ResponseDTO<List<FortuneTagVo>> getTagList(FortuneTagQuery query) {
        List<FortuneTagEntity> list = fortuneTagService.getTagList(query);
        List<FortuneTagVo> result = list.stream().map(FortuneTagVo::new).toList();
        List<FortuneTagVo> treeNodes = TreeUtil.buildForest(result, FortuneTagVo.class);
        return ResponseDTO.ok(treeNodes);
    }

    // public ResponseDTO<>


}
