package com.fortuneboot.controller.fortune;

import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.domain.bo.fortune.BookTemplateBo;
import com.fortuneboot.domain.bo.fortune.CurrencyTemplateBo;
import com.fortuneboot.domain.command.fortune.FortuneGroupAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneGroupModifyCommand;
import com.fortuneboot.domain.command.fortune.FortuneUserGroupRelationAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneUserGroupRelationModifyCommand;
import com.fortuneboot.domain.common.vo.SelectOptionsVo;
import com.fortuneboot.domain.query.fortune.GroupQuery;
import com.fortuneboot.domain.vo.fortune.FortuneGroupVo;
import com.fortuneboot.domain.vo.fortune.FortuneUserGroupRelationVo;
import com.fortuneboot.service.fortune.FortuneGroupService;
import com.fortuneboot.service.fortune.FortuneUserGroupRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
public class FortuneGroupController {

    private final FortuneGroupService fortuneGroupService;

    private final FortuneUserGroupRelationService fortuneUserGroupRelationService;


    private final ApplicationScopeBo applicationScopeBo;

    @Operation(summary = "分页查询我的分组")
    @GetMapping("/getPage")
    public ResponseDTO<PageDTO<FortuneGroupVo>> getFortuneGroupPage(GroupQuery query) {
        PageDTO<FortuneGroupVo> pageDTO = fortuneGroupService.getFortuneGroupPage(query);
        return ResponseDTO.ok(pageDTO);
    }

    @Operation(summary = "通过分组id查看分组")
    @GetMapping("/getByUserId/{groupId}")
    @PreAuthorize("@fortune.groupVisitorPermission(#groupId)")
    public ResponseDTO<FortuneGroupVo> getFortuneGroupByUserId(@PathVariable Long groupId) {
        return ResponseDTO.ok(fortuneGroupService.getFortuneGroupByUserId(groupId));
    }

    @Operation(summary = "获取账本模板")
    @GetMapping("/getBookTemplate")
    public ResponseDTO<List<SelectOptionsVo>> getBookTemplate() {
        var bookTemplateBoList = applicationScopeBo.getBookTemplateBoList();
        var result = bookTemplateBoList.stream().map(item -> {
            SelectOptionsVo selectOptionsVo = new SelectOptionsVo();
            selectOptionsVo.setValue(item.getBookTemplateId());
            selectOptionsVo.setLabel(item.getBookTemplateName());
            return selectOptionsVo;
        }).toList();
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取货币模板")
    @GetMapping("/getCurrencyTemplate")
    public ResponseDTO<List<SelectOptionsVo>> getCurrencyTemplate() {
        var currencyTemplateBoList = applicationScopeBo.getCurrencyTemplateBoList();
        var result = currencyTemplateBoList.stream().map(item->{
            SelectOptionsVo selectOptionsVo = new SelectOptionsVo();
            selectOptionsVo.setValue(item.getCurrencyId());
            selectOptionsVo.setLabel(item.getCurrencyName());
            return selectOptionsVo;
        }).toList();
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "新增分组")
    @PostMapping("/add")
    public ResponseDTO<Void> addFortuneGroup(@RequestBody FortuneGroupAddCommand groupAddCommand) {
        fortuneGroupService.addFortuneGroup(groupAddCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改分组")
    @PutMapping("/modify")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupModifyCommand.groupId)")
    public ResponseDTO<Void> modifyFortuneGroup(@RequestBody FortuneGroupModifyCommand groupModifyCommand) {
        fortuneGroupService.modifyFortuneGroup(groupModifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除分组")
    @DeleteMapping("/remove/{groupId}")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> removeFortuneGroup(@PathVariable Long groupId) {
        fortuneGroupService.removeFortuneGroup(groupId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "邀请用户")
    @PostMapping("/inviteUser")
    @PreAuthorize("@fortune.groupOwnerPermission(#relationAddCommand.groupId)")
    public ResponseDTO<Void> inviteUser(@RequestBody FortuneUserGroupRelationAddCommand relationAddCommand) {
        fortuneUserGroupRelationService.addFortuneUserGroupRelation(relationAddCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改用户")
    @PutMapping("/inviteUser")
    @PreAuthorize("@fortune.groupOwnerPermission(#relationModifyCommand.groupId)")
    public ResponseDTO<Void> modifyUser(@RequestBody FortuneUserGroupRelationModifyCommand relationModifyCommand) {
        fortuneUserGroupRelationService.modifyFortuneUserGroupRelation(relationModifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/removeUser/{relationId}")
    @PreAuthorize("@fortune.groupOwnerPermissionByRelationId(#relationId)")
    public ResponseDTO<Void> removeUser(@PathVariable Long relationId) {
        fortuneUserGroupRelationService.removeFortuneUserGroupRelation(relationId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "查询分组用户")
    @GetMapping("/getUserGroupRelation/{groupId}")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<List<FortuneUserGroupRelationVo>> getUserGroupRelationByGroupId(@PathVariable Long groupId) {
        return ResponseDTO.ok(fortuneUserGroupRelationService.getUserGroupRelationByGroupId(groupId));
    }

    @Operation(summary = "设置为默认分组")
    @PutMapping("/setDefaultGroup/{groupId}")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> setDefaultGroup(@PathVariable Long groupId) {
        fortuneUserGroupRelationService.setDefaultGroup(groupId);
        return ResponseDTO.ok();
    }
}
