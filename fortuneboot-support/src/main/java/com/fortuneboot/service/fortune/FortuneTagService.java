package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.common.utils.tree.AbstractTreeNode;
import com.fortuneboot.domain.command.fortune.FortuneTagAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneTagModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.domain.query.fortune.FortuneTagQuery;
import com.fortuneboot.domain.vo.fortune.FortuneCategoryVo;
import com.fortuneboot.domain.vo.fortune.FortuneTagVo;
import com.fortuneboot.factory.fortune.FortuneTagFactory;
import com.fortuneboot.factory.fortune.model.FortuneTagModel;
import com.fortuneboot.repository.fortune.FortuneTagRelationRepository;
import com.fortuneboot.repository.fortune.FortuneTagRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 账本标签服务
 *
 * @author zhangchi118
 * @date 2024/12/11 16:13
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneTagService {

    private final FortuneTagRepository fortuneTagRepository;

    private final FortuneTagFactory fortuneTagFactory;

    private final FortuneTagRelationRepository fortuneTagRelationRepository;

    public PageDTO<FortuneTagVo> getPage(FortuneTagQuery query) {
        // 根据是否条件查询选择不同处理逻辑
        if (!query.conditionQuery()) {
            // 无查询条件时直接获取根节点
            IPage<FortuneTagEntity> page = fortuneTagRepository.page(
                    query.toPage(),
                    query.addQueryCondition().eq(FortuneTagEntity::getParentId, -1L)
            );

            // 转换实体为VO对象
            List<FortuneTagVo> records = page.getRecords()
                    .stream()
                    .map(FortuneTagVo::new)
                    .collect(Collectors.toList());

            // 递归填充子节点（优化点：保持单次数据库交互）
            this.fillChildrenWithCache(records);

            return new PageDTO<>(records, page.getTotal());
        } else {
            // 有查询条件时需要关联查找完整树结构
            // 获取所有符合条件的节点（包括非根节点）
            List<FortuneTagEntity> list = fortuneTagRepository.list(query.addQueryCondition(Boolean.TRUE));

            // 递归查找所有关联的根节点ID（优化点：批量查询代替逐级递归）
            Set<Long> rootIdSet = this.findRootIdsEfficiently(list);

            // 根据根节点ID进行分页查询
            IPage<FortuneTagEntity> result = fortuneTagRepository.page(
                    query.toPage(),
                    query.addQueryCondition().in(FortuneTagEntity::getTagId, rootIdSet)
            );

            // 构建树形结构（优化点：使用缓存优化子节点查询）
            List<FortuneTagVo> forest = this.buildForestWithCache(result.getRecords(), list);

            return new PageDTO<>(forest, result.getTotal());
        }
    }
    /**
     * 高效查找根节点ID集合（优化递归为迭代）
     * @param children 初始子节点列表
     * @return 关联的根节点ID集合
     */
    private Set<Long> findRootIdsEfficiently(List<FortuneTagEntity> children) {
        Set<Long> rootIds = new HashSet<>();
        Set<Long> pendingParentIds = new HashSet<>();

        // 初始处理：直接筛选根节点并收集待处理的父ID
        children.forEach(entity -> {
            if (entity.getParentId() == -1L) {
                rootIds.add(entity.getTagId());
            } else {
                pendingParentIds.add(entity.getParentId());
            }
        });

        // 迭代处理父节点直到没有新的父ID
        while (!pendingParentIds.isEmpty()) {
            // 批量查询父节点
            List<FortuneTagEntity> parents = fortuneTagRepository.getByIds(new ArrayList<>(pendingParentIds));
            pendingParentIds.clear();

            parents.forEach(parent -> {
                if (parent.getParentId() == -1L) {
                    rootIds.add(parent.getTagId());
                } else {
                    pendingParentIds.add(parent.getParentId());
                }
            });
        }

        return rootIds;
    }

    /**
     * 带缓存的树结构构建（减少数据库查询次数）
     * @param rootEntities 根节点实体列表
     * @param allEntities 所有相关实体（包含子节点）
     * @return 构建完成的树结构VO列表
     */
    private List<FortuneTagVo> buildForestWithCache(List<FortuneTagEntity> rootEntities, List<FortuneTagEntity> allEntities) {
        // 创建缓存映射：父ID -> 子节点列表
        Map<Long, List<FortuneTagEntity>> childrenCache = allEntities.stream()
                .collect(Collectors.groupingBy(FortuneTagEntity::getParentId));

        // 转换根节点为VO
        return rootEntities.stream()
                .map(root -> buildTree(new FortuneTagVo(root), childrenCache))
                .collect(Collectors.toList());
    }

    /**
     * 递归构建树结构（使用缓存数据）
     * @param nodeVo 当前节点VO
     * @param childrenCache 子节点缓存
     * @return 构建完成的节点VO
     */
    private FortuneTagVo buildTree(FortuneTagVo nodeVo, Map<Long, List<FortuneTagEntity>> childrenCache) {
        List<FortuneTagEntity> childrenEntities = childrenCache.getOrDefault(
                nodeVo.getTagId(),
                Collections.emptyList()
        );

        childrenEntities.forEach(childEntity -> {
            FortuneTagVo childVo = new FortuneTagVo(childEntity);
            nodeVo.addChild(childVo);
            buildTree(childVo, childrenCache);  // 递归构建子树
        });

        return nodeVo;
    }

    private void fillChildrenWithCache(List<FortuneTagVo> parentVos) {
        if (CollectionUtils.isEmpty(parentVos)) {
            return;
        }
        // 收集所有需要查询的父ID（包括所有层级的）
        Set<Long> allParentIds = new HashSet<>();
        Queue<AbstractTreeNode> queue = new LinkedList<>(parentVos);

        while (!queue.isEmpty()) {
            AbstractTreeNode current = queue.poll();
            allParentIds.add(current.getId());
            if (!CollectionUtils.isEmpty(current.getChildren())) {
                queue.addAll(current.getChildren());
            }
        }

        // 单次批量查询所有子节点
        Map<Long, List<FortuneTagEntity>> childrenMap = fortuneTagRepository.getByParentIds(new ArrayList<>(allParentIds));

        // 递归填充子节点（使用内存数据）
        this.fillChildrenFromCache(parentVos, childrenMap);
    }

    private void fillChildrenFromCache(List<FortuneTagVo> parentVos, Map<Long, List<FortuneTagEntity>> childrenMap) {
        for (FortuneTagVo parentVo : parentVos) {
            List<FortuneTagEntity> childrenEntities = childrenMap.getOrDefault(
                    parentVo.getTagId(),
                    Collections.emptyList()
            );

            List<FortuneTagVo> childrenVos = childrenEntities.stream()
                    // 过滤回收站条目
                    .filter(e -> !e.getRecycleBin())
                    .map(FortuneTagVo::new).toList();

            childrenVos.forEach(parentVo::addChild);
            // 递归填充子树
            fillChildrenFromCache(childrenVos, childrenMap);
            this.fillChildrenWithCache(childrenVos);
        }
    }

/*
    public PageDTO<FortuneTagVo> getPage(FortuneTagQuery query) {
        IPage<FortuneTagEntity> page = fortuneTagRepository.page(query.toPage(), query.addQueryCondition().eq(FortuneTagEntity::getParentId, -1L));
        List<FortuneTagVo> records = page.getRecords().stream().map(FortuneTagVo::new).toList();
        this.fillChildren(records);
        return new PageDTO<>(records, page.getTotal());
    }

    private void fillChildren(List<FortuneTagVo> fortuneTagVos) {
        if (CollectionUtils.isEmpty(fortuneTagVos)) {
            return;
        }
        List<Long> parentIds = fortuneTagVos.stream().map(FortuneTagVo::getTagId).toList();
        Map<Long, List<FortuneTagEntity>> map = fortuneTagRepository.getByParentIds(parentIds);
        List<FortuneTagVo> childrenVo = new ArrayList<>();
        for (FortuneTagVo tagVo : fortuneTagVos) {
            List<FortuneTagEntity> childrenEntity = map.get(tagVo.getTagId());
            if (CollectionUtils.isEmpty(childrenEntity)) {
                continue;
            }
            List<FortuneTagVo> list = childrenEntity.stream().filter(item -> !item.getRecycleBin()).map(FortuneTagVo::new).toList();
            list.forEach(tagVo::addChild);
            childrenVo.addAll(list);
        }
        this.fillChildren(childrenVo);
    }*/

    public List<FortuneTagEntity> getList(FortuneTagQuery query) {
        return fortuneTagRepository.list(query.addQueryCondition());
    }

    public IPage<FortuneTagEntity> getListPage(FortuneTagQuery query) {
        return fortuneTagRepository.page(query.toPage(), query.addQueryCondition());
    }


    public List<FortuneTagEntity> getEnableList(Long bookId, Integer billType) {
        BillTypeEnum billTypeEnum = BillTypeEnum.getByValue(billType);
        switch (billTypeEnum) {
            case EXPENSE, INCOME, TRANSFER -> {
                return fortuneTagRepository.getEnableTagList(bookId, billType);
            }
            case null -> {
                return fortuneTagRepository.getEnableTagList(bookId, null);
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }

    public FortuneTagModel add(FortuneTagAddCommand addCommand) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.create();
        fortuneTagModel.loadAddCommand(addCommand);
        fortuneTagModel.checkTagExist();
        fortuneTagModel.checkHeight();
        fortuneTagModel.insert();
        return fortuneTagModel;
    }

    public void modify(FortuneTagModifyCommand modifyCommand) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(modifyCommand.getTagId());
        fortuneTagModel.loadModifyCommand(modifyCommand);
        fortuneTagModel.checkTagExist();
        fortuneTagModel.checkBookId(modifyCommand.getBookId());
        fortuneTagModel.checkParentId(modifyCommand.getParentId());
        fortuneTagModel.updateById();
    }

    public void moveToRecycleBin(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setRecycleBin(Boolean.TRUE);
        fortuneTagModel.updateById();
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long bookId, Long tagId) {
        Boolean used = fortuneTagRelationRepository.existByTagId(tagId);
        if (used) {
            throw new ApiException(ErrorCode.Business.TAG_ALREADY_USED);
        }
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.deleteById();
        // 递归删除子级标签
        List<FortuneTagEntity> children = fortuneTagRepository.getByParentId(tagId);
        for (FortuneTagEntity child : children) {
            this.remove(bookId, child.getTagId());
        }
    }


    public void putBack(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.checkParentInRecycleBin();
        fortuneTagModel.setRecycleBin(Boolean.FALSE);
        fortuneTagModel.updateById();
    }

    public void canExpense(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setCanExpense(Boolean.TRUE);
        fortuneTagModel.updateById();
    }

    public void cannotExpense(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setCanExpense(Boolean.FALSE);
        fortuneTagModel.updateById();
    }

    public void canIncome(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setCanIncome(Boolean.TRUE);
        fortuneTagModel.updateById();
    }

    public void cannotIncome(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setCanIncome(Boolean.FALSE);
        fortuneTagModel.updateById();
    }

    public void canTransfer(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setCanTransfer(Boolean.TRUE);
        fortuneTagModel.updateById();
    }

    public void cannotTransfer(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setCanTransfer(Boolean.FALSE);
        fortuneTagModel.updateById();
    }

    public void enable(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setEnable(Boolean.TRUE);
        fortuneTagModel.updateById();
    }

    public void disable(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setEnable(Boolean.FALSE);
        fortuneTagModel.updateById();
    }
}
