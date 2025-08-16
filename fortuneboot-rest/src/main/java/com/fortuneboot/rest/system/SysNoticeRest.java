package com.fortuneboot.rest.system;

import com.fortuneboot.common.core.base.BaseController;
import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.customize.accessLog.AccessLog;
import com.fortuneboot.domain.common.command.BulkOperationCommand;
import com.fortuneboot.service.system.NoticeApplicationService;
import com.fortuneboot.domain.command.system.NoticeAddCommand;
import com.fortuneboot.domain.command.system.NoticeUpdateCommand;
import com.fortuneboot.domain.dto.NoticeDTO;
import com.fortuneboot.domain.query.system.NoticeQuery;
import com.fortuneboot.infrastructure.annotations.unrepeatable.Unrepeatable;
import com.fortuneboot.infrastructure.annotations.unrepeatable.Unrepeatable.CheckType;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import com.baomidou.dynamic.datasource.annotation.DS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公告 信息操作处理
 *
 * @author valarchie
 */
@Tag(name = "公告API", description = "公告相关的增删查改")
@RestController
@RequestMapping("/system/notices")
@Validated
@RequiredArgsConstructor
public class SysNoticeRest extends BaseController {

    private final NoticeApplicationService noticeApplicationService;

    /**
     * 获取通知公告列表
     */
    @Operation(summary = "公告列表")
    @PreAuthorize("@permission.has('system:notice:list')")
    @GetMapping
    public ResponseDTO<PageDTO<NoticeDTO>> list(NoticeQuery query) {
        PageDTO<NoticeDTO> pageDTO = noticeApplicationService.getNoticeList(query);
        return ResponseDTO.ok(pageDTO);
    }

    /**
     * 获取通知公告列表
     * 从从库获取数据 例子 仅供参考
     */
    @Operation(summary = "公告列表（从数据库从库获取）", description = "演示主从库的例子")
    @DS("slave")
    @PreAuthorize("@permission.has('system:notice:list')")
    @GetMapping("/database/slave")
    public ResponseDTO<PageDTO<NoticeDTO>> listFromSlave(NoticeQuery query) {
        PageDTO<NoticeDTO> pageDTO = noticeApplicationService.getNoticeList(query);
        return ResponseDTO.ok(pageDTO);
    }

    /**
     * 根据通知公告编号获取详细信息
     */
    @Operation(summary = "公告详情")
    @PreAuthorize("@permission.has('system:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public ResponseDTO<NoticeDTO> getInfo(@PathVariable @NotNull @Positive Long noticeId) {
        return ResponseDTO.ok(noticeApplicationService.getNoticeInfo(noticeId));
    }

    /**
     * 新增通知公告
     */
    @Operation(summary = "添加公告")
    @Unrepeatable(interval = 60, checkType = CheckType.SYSTEM_USER)
    @PreAuthorize("@permission.has('system:notice:add')")
    @AccessLog(title = "通知公告", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@RequestBody NoticeAddCommand addCommand) {
        noticeApplicationService.addNotice(addCommand);
        return ResponseDTO.ok();
    }

    /**
     * 修改通知公告
     */
    @Operation(summary = "修改公告")
    @PreAuthorize("@permission.has('system:notice:edit')")
    @AccessLog(title = "通知公告", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{noticeId}")
    public ResponseDTO<Void> edit(@PathVariable Long noticeId, @RequestBody NoticeUpdateCommand updateCommand) {
        updateCommand.setNoticeId(noticeId);
        noticeApplicationService.updateNotice(updateCommand);
        return ResponseDTO.ok();
    }

    /**
     * 删除通知公告
     */
    @Operation(summary = "删除公告")
    @PreAuthorize("@permission.has('system:notice:remove')")
    @AccessLog(title = "通知公告", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping
    public ResponseDTO<Void> remove(@RequestParam List<Integer> noticeIds) {
        noticeApplicationService.deleteNotice(new BulkOperationCommand<>(noticeIds));
        return ResponseDTO.ok();
    }


}
