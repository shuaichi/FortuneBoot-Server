package com.fortuneboot.rest.fortune;

import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import com.fortuneboot.common.enums.fortune.RoleTypeEnum;
import com.fortuneboot.customize.accessLog.AccessLog;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.domain.bo.fortune.tenplate.BookTemplateBo;
import com.fortuneboot.domain.bo.fortune.tenplate.CurrencyTemplateBo;
import com.fortuneboot.domain.command.fortune.*;
import com.fortuneboot.domain.common.vo.SelectOptionsVo;
import com.fortuneboot.domain.query.fortune.FortuneGroupQuery;
import com.fortuneboot.domain.vo.fortune.FortuneGroupVo;
import com.fortuneboot.domain.vo.fortune.FortuneUserGroupRelationVo;
import com.fortuneboot.service.fortune.FortuneGroupService;
import com.fortuneboot.service.fortune.FortuneUserGroupRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 分组Controller
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/9 17:12
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/fortune/group")
@Tag(name = "分组API", description = "分组相关的增删查改")
public class FortuneGroupRest {

    private final FortuneGroupService fortuneGroupService;

    private final FortuneUserGroupRelationService fortuneUserGroupRelationService;

    private final ApplicationScopeBo applicationScopeBo;

    @GetMapping("/getPage")
    @Operation(summary = "分页查询我的分组")
    public ResponseDTO<PageDTO<FortuneGroupVo>> getFortuneGroupPage(@Valid FortuneGroupQuery query) {
        PageDTO<FortuneGroupVo> pageDTO = fortuneGroupService.getFortuneGroupPage(query);
        return ResponseDTO.ok(pageDTO);
    }

    @Operation(summary = "查询我的启用分组")
    @GetMapping("/getEnableList")
    public ResponseDTO<List<FortuneGroupVo>> getEnableGroupList() {
        List<FortuneGroupVo> result = fortuneGroupService.getEnableGroupList();
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "通过分组id查看分组")
    @GetMapping("/{groupId}/getByGroupId")
    @PreAuthorize("@fortune.groupVisitorPermission(#groupId)")
    public ResponseDTO<FortuneGroupVo> getByGroupId(@PathVariable @Positive Long groupId) {
        return ResponseDTO.ok(fortuneGroupService.getByGroupId(groupId));
    }

    @Operation(summary = "获取账本模板")
    @GetMapping("/getBookTemplate")
    public ResponseDTO<List<SelectOptionsVo>> getBookTemplate() {
        List<BookTemplateBo> bookTemplateBoList = applicationScopeBo.getBookTemplateBoList();
        List<SelectOptionsVo> result = bookTemplateBoList.stream().map(item -> {
            SelectOptionsVo selectOptionsVo = new SelectOptionsVo();
            selectOptionsVo.setValue(item.getBookTemplateId());
            selectOptionsVo.setLabel(item.getBookTemplateName());
            return selectOptionsVo;
        }).toList();
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "通过id获取账本模板备注")
    @GetMapping("/{id}/getBookTemplateRemarkById")
    public ResponseDTO<String> getBookTemplateRemarkById(@PathVariable @Positive Long id ) {
        List<BookTemplateBo> bookTemplateBoList = applicationScopeBo.getBookTemplateBoList();
        String remark = bookTemplateBoList.stream()
                .filter(item -> Objects.equals(item.getBookTemplateId(), id))
                .findAny()
                .map(BookTemplateBo::getRemark)
                .orElse(null);
        return ResponseDTO.ok(remark);
    }

    @Operation(summary = "获取货币模板")
    @GetMapping("/getCurrencyTemplate")
    public ResponseDTO<List<SelectOptionsVo>> getCurrencyTemplate() {
        List<CurrencyTemplateBo> currencyTemplateBoList = applicationScopeBo.getCurrencyTemplateBoList();
        List<SelectOptionsVo> result = currencyTemplateBoList.stream().map(item -> {
            SelectOptionsVo selectOptionsVo = new SelectOptionsVo();
            selectOptionsVo.setValue(item.getCurrencyId());
            selectOptionsVo.setLabel(item.getCurrencyName());
            return selectOptionsVo;
        }).toList();
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "查询用户的默认分组")
    @GetMapping("/getDefaultGroupId")
    public ResponseDTO<Long> getDefaultGroupId() {
        return ResponseDTO.ok(fortuneUserGroupRelationService.getDefaultGroupId());
    }

    @Operation(summary = "新增分组")
    @PostMapping("/add")
    @AccessLog(title = "好记-分组管理", businessType = BusinessTypeEnum.ADD)
    public ResponseDTO<Void> add(@Valid @RequestBody FortuneGroupAddCommand groupAddCommand) {
        fortuneGroupService.add(groupAddCommand, null);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改分组")
    @PutMapping("/modify")
    @AccessLog(title = "好记-分组管理", businessType = BusinessTypeEnum.MODIFY)
    @PreAuthorize("@fortune.groupActorPermission(#groupModifyCommand.groupId)")
    public ResponseDTO<Void> modify(@Valid @RequestBody FortuneGroupModifyCommand groupModifyCommand) {
        fortuneGroupService.modify(groupModifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除分组")
    @DeleteMapping("/{groupId}/remove")
    @AccessLog(title = "好记-分组管理", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> remove(@PathVariable @NotNull(message = "分组id不能为空") @Positive Long groupId) {
        fortuneGroupService.remove(groupId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "查询分组角色枚举")
    @GetMapping("/getRoleTypes")
    public ResponseDTO<Map<Integer, String>> getRoleTypes() {
        return ResponseDTO.ok(Arrays.stream(RoleTypeEnum.values()).collect(Collectors.toMap(RoleTypeEnum::getValue, RoleTypeEnum::getDescription)));
    }

    @Operation(summary = "邀请用户")
    @PostMapping("/inviteUser")
    @AccessLog(title = "好记-分组管理-邀请用户", businessType = BusinessTypeEnum.GRANT)
    @PreAuthorize("@fortune.groupOwnerPermission(#inviteCommand.getGroupId())")
    public ResponseDTO<Void> inviteUser(@RequestBody @Valid FortuneUserGroupRelationInviteCommand inviteCommand) {
        fortuneUserGroupRelationService.inviteUser(inviteCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{relationId}/removeGroupUser")
    @AccessLog(title = "好记-分组管理-邀请用户", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@fortune.groupOwnerPermissionByRelationId(#relationId)")
    public ResponseDTO<Void> removeGroupUser(@PathVariable @NotNull(message = "被删除的分组用户id不能为空") @Positive Long relationId) {
        fortuneUserGroupRelationService.removeGroupUser(relationId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "查询分组用户")
    @GetMapping("/{groupId}/getGroupUser")
    @PreAuthorize("@fortune.groupVisitorPermission(#groupId)")
    public ResponseDTO<List<FortuneUserGroupRelationVo>> getGroupUser(@PathVariable @NotNull(message = "分组id不能为空") @Positive Long groupId) {
        return ResponseDTO.ok(fortuneUserGroupRelationService.getGroupUser(groupId));
    }

    @Operation(summary = "设置为默认分组")
    @PatchMapping("/{groupId}/setDefaultGroup")
    @AccessLog(title = "好记-分组管理-设置默认分组", businessType = BusinessTypeEnum.SET_DEFAULT)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> setDefaultGroup(@PathVariable @NotNull(message = "分组id不能为空") @Positive Long groupId) {
        fortuneUserGroupRelationService.setDefaultGroup(groupId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "启用分组")
    @PatchMapping("/{groupId}/enable")
    @AccessLog(title = "好记-分组管理", businessType = BusinessTypeEnum.ENABLE)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> enable(@PathVariable @NotNull(message = "分组id不能为空") @Positive Long groupId) {
        fortuneGroupService.enable(groupId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "停用分组")
    @PatchMapping("/{groupId}/disable")
    @AccessLog(title = "好记-分组管理", businessType = BusinessTypeEnum.DISABLE)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> disable(@PathVariable @NotNull(message = "分组id不能为空") @Positive Long groupId) {
        fortuneGroupService.disable(groupId);
        return ResponseDTO.ok();
    }
}
