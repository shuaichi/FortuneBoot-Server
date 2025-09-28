package com.fortuneboot.service.fortune;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.fortune.RoleTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneBookAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneGroupAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneGroupModifyCommand;
import com.fortuneboot.domain.command.fortune.FortuneUserGroupRelationAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.entity.fortune.FortuneGroupEntity;
import com.fortuneboot.domain.entity.fortune.FortuneUserGroupRelationEntity;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import com.fortuneboot.domain.query.fortune.FortuneGroupQuery;
import com.fortuneboot.domain.vo.fortune.FortuneGroupVo;
import com.fortuneboot.factory.fortune.factory.FortuneBookFactory;
import com.fortuneboot.factory.fortune.factory.FortuneGroupFactory;
import com.fortuneboot.factory.fortune.model.FortuneBookModel;
import com.fortuneboot.factory.fortune.model.FortuneGroupModel;
import com.fortuneboot.infrastructure.user.AuthenticationUtils;
import com.fortuneboot.repository.fortune.FortuneBookRepo;
import com.fortuneboot.repository.fortune.FortuneGroupRepo;
import com.fortuneboot.repository.fortune.FortuneUserGroupRelationRepo;
import com.fortuneboot.repository.system.SysUserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分组Service
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/9 17:23
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneGroupService {
    /**
     * 分组repository
     */
    private final FortuneGroupRepo fortuneGroupRepo;

    /**
     * 分组工厂
     */
    private final FortuneGroupFactory fortuneGroupFactory;

    /**
     * 用户/分组关系repository
     */
    private final FortuneUserGroupRelationRepo fortuneUserGroupRelationRepo;

    /**
     * 用户/分组关系工厂
     */
    private final FortuneUserGroupRelationService fortuneUserGroupRelationService;

    /**
     * 账本service
     */
    private final FortuneBookService fortuneBookService;
    /**
     * 账本repo
     */
    private final FortuneBookRepo fortuneBookRepo;

    /**
     * 用户repo
     */
    private final SysUserRepo sysUserRepo;
    private final FortuneBookFactory fortuneBookFactory;


    public FortuneGroupVo getByGroupId(Long groupId) {
        FortuneGroupEntity fortuneGroupEntity = fortuneGroupRepo.getById(groupId);
        FortuneGroupVo fortuneGroupVo = BeanUtil.copyProperties(fortuneGroupEntity, FortuneGroupVo.class);
        FortuneBookEntity bookEntity = fortuneBookService.getBookById(fortuneGroupEntity.getDefaultBookId());
        fortuneGroupVo.setDefaultBookName(bookEntity.getBookName());
        return fortuneGroupVo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(FortuneGroupAddCommand groupAddCommand, Long userId) {
        FortuneGroupModel fortuneGroupModel = fortuneGroupFactory.create();
        fortuneGroupModel.loadAddCommand(groupAddCommand);
        fortuneGroupModel.insert();
        // 设置权限
        userId = Objects.isNull(userId) ? AuthenticationUtils.getSystemLoginUser().getUserId() : userId;
        FortuneUserGroupRelationAddCommand fortuneUserGroupRelationAddCommand = new FortuneUserGroupRelationAddCommand();
        fortuneUserGroupRelationAddCommand.setGroupId(fortuneGroupModel.getGroupId());
        fortuneUserGroupRelationAddCommand.setUserId(userId);
        fortuneUserGroupRelationAddCommand.setRoleType(RoleTypeEnum.OWNER.getValue());
        fortuneUserGroupRelationService.add(fortuneUserGroupRelationAddCommand);
        // 新增账本
        FortuneBookAddCommand fortuneBookAddCommand = new FortuneBookAddCommand();
        fortuneBookAddCommand.setGroupId(fortuneGroupModel.getGroupId());
        fortuneBookAddCommand.setBookName(groupAddCommand.getGroupName() + "的默认账本");
        fortuneBookAddCommand.setDefaultCurrency(groupAddCommand.getDefaultCurrency());
        fortuneBookAddCommand.setBookTemplate(groupAddCommand.getBookTemplate());
        FortuneBookModel fortuneBookModel = fortuneBookService.add(fortuneBookAddCommand);
        // 更新默认账本id
        fortuneGroupModel.setDefaultBookId(fortuneBookModel.getBookId());
        fortuneGroupModel.updateById();
    }

    public void modify(FortuneGroupModifyCommand groupModifyCommand) {
        FortuneGroupModel fortuneGroupModel = fortuneGroupFactory.loadById(groupModifyCommand.getGroupId());
        fortuneGroupModel.loadModifyCommand(groupModifyCommand);
        fortuneGroupModel.updateById();
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long groupId) {
        FortuneGroupModel fortuneGroupModel = fortuneGroupFactory.loadById(groupId);
        fortuneGroupModel.deleteById();
        fortuneUserGroupRelationRepo.removeByGroupId(groupId);
        fortuneBookService.removeByGroupId(groupId);
    }

    public PageDTO<FortuneGroupVo> getFortuneGroupPage(FortuneGroupQuery query) {
        // 获取当前用户的组关系列表
        List<FortuneUserGroupRelationEntity> relationList = fortuneUserGroupRelationRepo.getByUserId();
        if (CollectionUtils.isEmpty(relationList)) {
            return PageDTO.empty();
        }

        // 提取用户有权限的组ID列表
        List<Long> groupIds = extractGroupIds(relationList);

        // 构建查询条件并执行分页查询
        LambdaQueryWrapper<FortuneGroupEntity> queryWrapper = buildQueryWrapper(query, groupIds);
        Page<FortuneGroupEntity> groupPage = fortuneGroupRepo.page(query.toPage(), queryWrapper);

        // 提前返回空结果
        if (CollectionUtils.isEmpty(groupPage.getRecords())) {
            return PageDTO.empty();
        }

        // 批量获取关联数据并构建缓存MAP
        Map<Long, FortuneBookEntity> bookMap = getBookMap(groupPage.getRecords());
        Map<Long, FortuneUserGroupRelationEntity> relationMap = buildRelationMap(relationList);

        // 转换VO对象
        List<FortuneGroupVo> voList = convertToVoList(groupPage.getRecords(), bookMap, relationMap);

        return new PageDTO<>(voList, groupPage.getTotal());
    }


    public List<FortuneGroupVo> getEnableGroupList() {
        // 获取当前用户的组关系列表
        List<FortuneUserGroupRelationEntity> relationList = fortuneUserGroupRelationRepo.getByUserId();
        if (CollectionUtils.isEmpty(relationList)) {
            return Collections.emptyList();
        }
        // 提取用户有权限的组ID列表
        List<Long> groupIds = extractGroupIds(relationList);
        List<FortuneGroupEntity> list = fortuneGroupRepo.getEnableByGroupIds(groupIds);

        // 提前返回空结果
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 批量获取关联数据并构建缓存MAP
        Map<Long, FortuneBookEntity> bookMap = getBookMap(list);
        Map<Long, FortuneUserGroupRelationEntity> relationMap = buildRelationMap(relationList);

        // 转换VO对象
        return convertToVoList(list, bookMap, relationMap);
    }

    // 提取组ID列表（带注释的独立方法）
    private List<Long> extractGroupIds(List<FortuneUserGroupRelationEntity> relations) {
        return relations.stream()
                .map(FortuneUserGroupRelationEntity::getGroupId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 构建查询条件（显式命名的查询条件构造）
    private LambdaQueryWrapper<FortuneGroupEntity> buildQueryWrapper(FortuneGroupQuery query, List<Long> groupIds) {
        LambdaQueryWrapper<FortuneGroupEntity> wrapper = query.addQueryCondition();
        return wrapper.in(FortuneGroupEntity::getGroupId, groupIds);
    }

    // 批量获取账本数据（带空值过滤）
    private Map<Long, FortuneBookEntity> getBookMap(List<FortuneGroupEntity> groups) {
        List<Long> bookIds = groups.stream()
                .map(FortuneGroupEntity::getDefaultBookId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return CollectionUtils.isEmpty(bookIds)
                ? Collections.emptyMap()
                : fortuneBookRepo.listByIds(bookIds).stream()
                .collect(Collectors.toMap(FortuneBookEntity::getBookId, Function.identity()));
    }

    // 构建关系映射表（显式处理重复键）
    private Map<Long, FortuneUserGroupRelationEntity> buildRelationMap(List<FortuneUserGroupRelationEntity> relations) {
        return relations.stream()
                .collect(Collectors.toMap(
                        FortuneUserGroupRelationEntity::getGroupId,
                        Function.identity(),
                        (existing, replacement) -> replacement));
    }

    // VO转换（使用独立方法明确转换逻辑）
    private List<FortuneGroupVo> convertToVoList(
            List<FortuneGroupEntity> groups,
            Map<Long, FortuneBookEntity> bookMap,
            Map<Long, FortuneUserGroupRelationEntity> relationMap) {

        return groups.stream()
                .map(entity -> {
                    FortuneGroupVo vo = new FortuneGroupVo(entity);
                    bindAdditionalInfo(vo, bookMap, relationMap);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    // 绑定附加信息（安全处理空值）
    private void bindAdditionalInfo(
            FortuneGroupVo vo,
            Map<Long, FortuneBookEntity> bookMap,
            Map<Long, FortuneUserGroupRelationEntity> relationMap) {

        // 处理账本名称
        Optional.ofNullable(vo.getDefaultBookId())
                .map(bookMap::get)
                .ifPresent(book -> vo.setDefaultBookName(book.getBookName()));

        // 处理角色类型描述
        Optional.ofNullable(relationMap.get(vo.getGroupId()))
                .map(FortuneUserGroupRelationEntity::getRoleType)
                .map(RoleTypeEnum::getByValue)
                .ifPresent(roleType -> vo.setRoleTypeDesc(roleType.getDescription()));
    }

    public void setDefaultBook(Long bookId) {
        FortuneBookModel fortuneBookModel = fortuneBookFactory.loadById(bookId);
        FortuneGroupModel fortuneGroupModel = fortuneGroupFactory.loadById(fortuneBookModel.getGroupId());
        fortuneGroupModel.checkDefaultBookDisable(fortuneBookModel);
        fortuneGroupModel.setDefaultBookId(bookId);
        fortuneGroupModel.updateById();
    }

    public void enable(Long groupId) {
        FortuneGroupModel fortuneGroupModel = fortuneGroupFactory.loadById(groupId);
        fortuneGroupModel.setEnable(Boolean.TRUE);
        fortuneGroupModel.updateById();
    }

    public void disable(Long groupId) {
        this.checkDefault(groupId);
        FortuneGroupModel fortuneGroupModel = fortuneGroupFactory.loadById(groupId);
        fortuneGroupModel.setEnable(Boolean.FALSE);
        fortuneGroupModel.updateById();
    }

    private void checkDefault(Long groupId) {
        List<FortuneUserGroupRelationEntity> defaultGroupList = fortuneUserGroupRelationRepo.getDefaultGroupByGroupId(groupId);
        if (CollectionUtils.isNotEmpty(defaultGroupList)) {
            List<Long> userIds = defaultGroupList.stream().map(FortuneUserGroupRelationEntity::getUserId).toList();
            List<SysUserEntity> users = sysUserRepo.getUsersByIds(userIds);
            List<String> nicknames = users.stream().map(SysUserEntity::getNickname).toList();
            throw new ApiException(ErrorCode.Business.GROUP_CANNOT_DISABLE_DEFAULT_GROUP, nicknames.toString());
        }
    }
}
