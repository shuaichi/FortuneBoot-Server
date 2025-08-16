package com.fortuneboot.rest.fortune;

import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.domain.entity.fortune.FortuneFileEntity;
import com.fortuneboot.domain.vo.fortune.FortuneFileVo;
import com.fortuneboot.repository.fortune.FortuneFileRepo;
import com.fortuneboot.service.fortune.FortuneFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 账单附件
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/2/27 22:01
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/fortune/bill/file")
@Tag(name = "分类API", description = "账本分类的增删查改")
public class FortuneFileRest {

    private final FortuneFileService fortuneFileService;

    private final FortuneFileRepo fortuneFileRepo;

    @Operation(summary = "根据账单id查询文件")
    @GetMapping("/{billId}/getByBillId")
    @PreAuthorize("@fortune.billVisitorPermission(#billId)")
    public ResponseDTO<List<FortuneFileVo>> getByBillId(@PathVariable @Positive Long billId) {
        List<FortuneFileEntity> list = fortuneFileRepo.getByBillId(billId);
        List<FortuneFileVo> result = list.stream().map(FortuneFileVo::new).toList();
        return ResponseDTO.ok(result);
    }
}
