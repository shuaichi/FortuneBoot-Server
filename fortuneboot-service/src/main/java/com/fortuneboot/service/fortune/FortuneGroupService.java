package com.fortuneboot.service.fortune;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.fortune.RoleTypeEnum;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.command.fortune.FortuneGroupAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneGroupModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.entity.fortune.FortuneGroupEntity;
import com.fortuneboot.domain.entity.fortune.FortuneUserGroupRelationEntity;
import com.fortuneboot.domain.query.fortune.FortuneGroupQuery;
import com.fortuneboot.domain.vo.fortune.FortuneGroupVo;
import com.fortuneboot.factory.fortune.FortuneGroupFactory;
import com.fortuneboot.factory.fortune.FortuneUserGroupRelationFactory;
import com.fortuneboot.factory.fortune.model.FortuneGroupModel;
import com.fortuneboot.factory.fortune.model.FortuneUserGroupRelationModel;
import com.fortuneboot.infrastructure.user.AuthenticationUtils;
import com.fortuneboot.repository.fortune.FortuneBookRepository;
import com.fortuneboot.repository.fortune.FortuneGroupRepository;
import com.fortuneboot.repository.fortune.FortuneUserGroupRelationRepository;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final FortuneGroupRepository fortuneGroupRepository;

    /**
     * 分组工厂
     */
    private final FortuneGroupFactory fortuneGroupFactory;

    /**
     * 用户/分组关系repository
     */
    private final FortuneUserGroupRelationRepository fortuneUserGroupRelationRepository;

    /**
     * 用户/分组关系工厂
     */
    private final FortuneUserGroupRelationFactory fortuneUserGroupRelationFactory;

    /**
     * 账本Repository
     */
    private final FortuneBookRepository fortuneBookRepository;

    public FortuneGroupVo getByUserId(Long groupId) {
        FortuneGroupEntity fortuneGroupEntity = fortuneGroupRepository.getById(groupId);
        return BeanUtil.copyProperties(fortuneGroupEntity, FortuneGroupVo.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(FortuneGroupAddCommand groupAddCommand) {
        FortuneGroupModel fortuneGroupModel = fortuneGroupFactory.create();
        fortuneGroupModel.loadAddCommand(groupAddCommand);
        fortuneGroupModel.insert();
        // 设置权限
        FortuneUserGroupRelationModel relationModel = fortuneUserGroupRelationFactory.create();
        relationModel.setGroupId(fortuneGroupModel.getGroupId());
        relationModel.setUserId(AuthenticationUtils.getSystemLoginUser().getUserId());
        relationModel.setRoleType(RoleTypeEnum.OWNER.getValue());
        relationModel.insert();
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
        fortuneUserGroupRelationRepository.removeByGroupId(groupId);
    }

    public PageDTO<FortuneGroupVo> getFortuneGroupPage(FortuneGroupQuery query) {
        List<FortuneUserGroupRelationEntity> relationEntityList = fortuneUserGroupRelationRepository.getByUserId();
        if (CollectionUtils.isEmpty(relationEntityList)) {
            return new PageDTO<>(new ArrayList<>(), 0L);
        }
        List<Long> groupIdList = relationEntityList.stream().map(FortuneUserGroupRelationEntity::getGroupId).collect(Collectors.toList());
        LambdaQueryWrapper<FortuneGroupEntity> queryWrapper = query.addQueryCondition();
        queryWrapper.in(FortuneGroupEntity::getGroupId, groupIdList);
        Page<FortuneGroupEntity> page = fortuneGroupRepository.page(query.toPage(), queryWrapper);
        List<Long> bookIdList = page.getRecords().stream().map(FortuneGroupEntity::getDefaultBookId).toList();
        List<FortuneBookEntity> bookEntities = fortuneBookRepository.listByIds(bookIdList);
        Map<Long, FortuneBookEntity> idMapBook = bookEntities.stream().collect(Collectors.toMap(FortuneBookEntity::getBookId, Function.identity()));
        Map<Long, FortuneUserGroupRelationEntity> idMapRelation = relationEntityList.stream().collect(Collectors.toMap(FortuneUserGroupRelationEntity::getGroupId, Function.identity(), (k1, k2) -> k2));
        List<FortuneGroupVo> records = page.getRecords().stream().map(FortuneGroupVo::new).peek(item -> {
            if (ObjectUtil.isNotEmpty(item.getDefaultBookId())) {
                item.setDefaultBookName(idMapBook.get(item.getDefaultBookId()).getBookName());
            }
            item.setRoleTypeDesc(Objects.requireNonNull(RoleTypeEnum.getByValue(idMapRelation.get(item.getGroupId()).getRoleType())).getDescription());
        }).collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    public void setDefaultBook(Long groupId, Long bookId) {
        FortuneGroupModel fortuneGroupModel = fortuneGroupFactory.loadById(groupId);
        fortuneGroupModel.setDefaultBookId(bookId);
        fortuneGroupModel.updateById();
    }
}
