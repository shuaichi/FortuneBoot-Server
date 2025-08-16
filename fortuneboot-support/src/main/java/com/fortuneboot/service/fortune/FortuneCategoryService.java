package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.fortune.CategoryTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.common.utils.tree.AbstractTreeNode;
import com.fortuneboot.domain.command.fortune.FortuneCategoryAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneCategoryModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.domain.query.fortune.FortuneCategoryQuery;
import com.fortuneboot.domain.vo.fortune.FortuneCategoryVo;
import com.fortuneboot.factory.fortune.factory.FortuneCategoryFactory;
import com.fortuneboot.factory.fortune.model.FortuneCategoryModel;
import com.fortuneboot.repository.fortune.FortuneCategoryRelationRepository;
import com.fortuneboot.repository.fortune.FortuneCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhangchi118
 * @date 2025/1/10 18:33
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneCategoryService {

    private final FortuneCategoryRepository fortuneCategoryRepository;

    private final FortuneCategoryFactory fortuneCategoryFactory;

    private final FortuneCategoryRelationRepository fortuneCategoryRelationRepository;
    public PageDTO<FortuneCategoryVo> getPage(FortuneCategoryQuery query) {
        // 根据是否条件查询选择不同处理逻辑
        if (!query.conditionQuery()) {
            // 无查询条件时直接获取根节点
            // 优化点：复用分页参数，避免重复创建
            IPage<FortuneCategoryEntity> page = fortuneCategoryRepository.page(
                    query.toPage(),
                    query.addQueryCondition()
                            .eq(FortuneCategoryEntity::getParentId, -1L)  // 根节点条件
            );

            // 转换实体为VO对象
            List<FortuneCategoryVo> records = page.getRecords()
                    .stream()
                    .map(FortuneCategoryVo::new)
                    .collect(Collectors.toList());

            // 递归填充子节点
            this.fillChildrenWithCache(records);

            return new PageDTO<>(records, page.getTotal());
        } else {
            // 有查询条件时需要关联查找完整树结构
            // 获取所有符合条件的节点（包括非根节点）
            List<FortuneCategoryEntity> list = fortuneCategoryRepository.list(
                    query.addQueryCondition(Boolean.TRUE)
            );

            // 递归查找所有关联的根节点ID（优化点：批量查询代替逐级递归）
            Set<Long> rootIdSet = this.findRootIdsEfficiently(list);

            // 根据根节点ID进行分页查询
            Page<FortuneCategoryEntity> result = fortuneCategoryRepository.page(
                    query.toPage(),
                    query.addQueryCondition()
                            .in(FortuneCategoryEntity::getCategoryId, rootIdSet)
            );

            // 构建树形结构（优化点：使用缓存优化子节点查询）
            List<FortuneCategoryVo> forest = this.buildForestWithCache(
                    result.getRecords(),
                    list
            );

            return new PageDTO<>(forest, result.getTotal());
        }
    }

    /**
     * 高效查找根节点ID集合（优化递归为迭代）
     * @param children 初始子节点列表
     * @return 关联的根节点ID集合
     */
    private Set<Long> findRootIdsEfficiently(List<FortuneCategoryEntity> children) {
        Set<Long> rootIds = new HashSet<>();
        Set<Long> pendingParentIds = new HashSet<>();

        // 初始处理：直接筛选根节点并收集待处理的父ID
        children.forEach(entity -> {
            if (entity.getParentId() == -1L) {
                rootIds.add(entity.getCategoryId());
            } else {
                pendingParentIds.add(entity.getParentId());
            }
        });

        // 迭代处理父节点直到没有新的父ID
        while (!pendingParentIds.isEmpty()) {
            // 批量查询父节点
            List<FortuneCategoryEntity> parents = fortuneCategoryRepository.getByIds(
                    new ArrayList<>(pendingParentIds)
            );
            pendingParentIds.clear();

            parents.forEach(parent -> {
                if (parent.getParentId() == -1L) {
                    rootIds.add(parent.getCategoryId());
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
    private List<FortuneCategoryVo> buildForestWithCache(List<FortuneCategoryEntity> rootEntities, List<FortuneCategoryEntity> allEntities) {
        // 创建缓存映射：父ID -> 子节点列表
        Map<Long, List<FortuneCategoryEntity>> childrenCache = allEntities.stream()
                .collect(Collectors.groupingBy(FortuneCategoryEntity::getParentId));

        // 转换根节点为VO
        return rootEntities.stream()
                .map(root -> buildTree(new FortuneCategoryVo(root), childrenCache))
                .collect(Collectors.toList());
    }

    /**
     * 递归构建树结构（使用缓存数据）
     * @param nodeVo 当前节点VO
     * @param childrenCache 子节点缓存
     * @return 构建完成的节点VO
     */
    private FortuneCategoryVo buildTree(FortuneCategoryVo nodeVo, Map<Long, List<FortuneCategoryEntity>> childrenCache) {
        List<FortuneCategoryEntity> childrenEntities = childrenCache.getOrDefault(
                nodeVo.getCategoryId(),
                Collections.emptyList()
        );

        childrenEntities.forEach(childEntity -> {
            FortuneCategoryVo childVo = new FortuneCategoryVo(childEntity);
            nodeVo.addChild(childVo);
            buildTree(childVo, childrenCache);  // 递归构建子树
        });

        return nodeVo;
    }

    /**
     * 带缓存的子节点填充（单次数据库查询）
     * @param parentVos 父节点VO列表
     */
    private void fillChildrenWithCache(List<FortuneCategoryVo> parentVos) {
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
        Map<Long, List<FortuneCategoryEntity>> childrenMap = fortuneCategoryRepository.getByParentIds(new ArrayList<>(allParentIds));

        // 递归填充子节点（使用内存数据）
        this.fillChildrenFromCache(parentVos, childrenMap);
    }

    /**
     * 从缓存映射填充子节点
     * @param parentVos 当前层父节点VO列表
     * @param childrenMap 子节点缓存映射
     */
    private void fillChildrenFromCache(List<FortuneCategoryVo> parentVos, Map<Long, List<FortuneCategoryEntity>> childrenMap) {
        for (FortuneCategoryVo parentVo : parentVos) {
            List<FortuneCategoryEntity> childrenEntities = childrenMap.getOrDefault(
                    parentVo.getCategoryId(),
                    Collections.emptyList()
            );

            List<FortuneCategoryVo> childrenVos = childrenEntities.stream()
                    // 过滤回收站条目
                    .filter(e -> !e.getRecycleBin())
                    .map(FortuneCategoryVo::new)
                    .collect(Collectors.toList());

            childrenVos.forEach(parentVo::addChild);
            // 递归填充子树
            this.fillChildrenFromCache(childrenVos, childrenMap);
            this.fillChildrenWithCache(childrenVos);
        }
    }

    public List<FortuneCategoryEntity> getList(FortuneCategoryQuery query) {
        return fortuneCategoryRepository.list(query.addQueryCondition());
    }

    public IPage<FortuneCategoryEntity> getListPageApi(FortuneCategoryQuery query) {
        return fortuneCategoryRepository.page(query.toPage(), query.addQueryCondition());
    }

    public List<FortuneCategoryEntity> getEnableCategoryList(Long bookId, Integer billType) {
        CategoryTypeEnum categoryTypeEnum = CategoryTypeEnum.getByValue(billType);
        switch (categoryTypeEnum) {
            case INCOME, EXPENSE -> {
                return fortuneCategoryRepository.getEnableCategoryList(bookId, billType);
            }
            case null -> {
                return fortuneCategoryRepository.getEnableCategoryList(bookId, null);
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }

    public FortuneCategoryModel add(FortuneCategoryAddCommand addCommand) {
        FortuneCategoryModel fortuneCategoryModel = fortuneCategoryFactory.create();
        fortuneCategoryModel.loadAddCommand(addCommand);
        fortuneCategoryModel.checkHeight();
        fortuneCategoryModel.insert();
        return fortuneCategoryModel;
    }

    public void modify(FortuneCategoryModifyCommand modifyCommand) {
        FortuneCategoryModel fortuneCategoryModel = fortuneCategoryFactory.loadById(modifyCommand.getCategoryId());
        fortuneCategoryModel.loadModifyCommand(modifyCommand);
        fortuneCategoryModel.checkBookId(modifyCommand.getBookId());
        fortuneCategoryModel.checkParentId(modifyCommand.getParentId());
        fortuneCategoryModel.updateById();
    }

    public void moveToRecycleBin(Long bookId, Long categoryId) {
        FortuneCategoryModel fortuneCategoryModel = fortuneCategoryFactory.loadById(categoryId);
        fortuneCategoryModel.checkBookId(bookId);
        fortuneCategoryModel.setRecycleBin(Boolean.TRUE);
        fortuneCategoryModel.updateById();
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long bookId, Long categoryId) {
        Boolean used = fortuneCategoryRelationRepository.existByCategoryId(categoryId);
        if (used) {
            throw new ApiException(ErrorCode.Business.CATEGORY_ALREADY_USED);
        }
        FortuneCategoryModel fortuneCategoryModel = fortuneCategoryFactory.loadById(categoryId);
        fortuneCategoryModel.checkBookId(bookId);
        fortuneCategoryModel.deleteById();
        List<FortuneCategoryEntity> children = fortuneCategoryRepository.getByParentId(categoryId);
        for (FortuneCategoryEntity child : children) {
            this.remove(bookId, child.getCategoryId());
        }
    }

    public void putBack(Long bookId, Long categoryId) {
        FortuneCategoryModel fortuneCategoryModel = fortuneCategoryFactory.loadById(categoryId);
        fortuneCategoryModel.checkBookId(bookId);
        fortuneCategoryModel.checkParentInRecycleBin();
        fortuneCategoryModel.setRecycleBin(Boolean.FALSE);
        fortuneCategoryModel.updateById();
    }

    public void enable(Long bookId, Long categoryId) {
        FortuneCategoryModel fortuneCategoryModel = fortuneCategoryFactory.loadById(categoryId);
        fortuneCategoryModel.checkBookId(bookId);
        fortuneCategoryModel.setEnable(Boolean.TRUE);
        fortuneCategoryModel.updateById();
    }

    public void disable(Long bookId, Long categoryId) {
        FortuneCategoryModel fortuneCategoryModel = fortuneCategoryFactory.loadById(categoryId);
        fortuneCategoryModel.checkBookId(bookId);
        fortuneCategoryModel.setEnable(Boolean.FALSE);
        fortuneCategoryModel.updateById();
    }
}
