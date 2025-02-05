package com.fortuneboot.common.utils.tree;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;

import java.util.*;

/**
 * 树形结构工具类
 *
 * @author zhangchi118
 * @date 2024/12/11 17:38
 **/
public class TreeUtil {

    private static final Long ROOT_PARENT_ID = -1L;

    public static <T extends AbstractTreeNode> List<T> buildForest(List<? extends AbstractTreeNode> dataList, Class<T> clazz) {
        // 参数校验
        if (Objects.isNull(clazz)) {
            throw new IllegalArgumentException("clazz must not be null");
        }
        if (Objects.isNull(dataList)) {
            return Collections.emptyList();
        }
        Map<Long, AbstractTreeNode> nodeMap = new HashMap<>();
        List<T> roots = new ArrayList<>();
        // 构建节点映射并检查重复ID
        for (AbstractTreeNode item : dataList) {
            if (nodeMap.containsKey(item.getId())) {
                throw new ApiException(ErrorCode.Business.COMMON_TREE_DUPLICATE_NODE_ID, item.getId());
            }
            nodeMap.put(item.getId(), item);
        }
        // 构建树结构
        for (AbstractTreeNode node : dataList) {
            Long parentId = node.getParentId();
            if (Objects.equals(parentId, ROOT_PARENT_ID)) {
                // 根节点类型检查
                if (clazz.isInstance(node)) {
                    roots.add(clazz.cast(node));
                } else {
                    // 可根据需求抛出异常或记录日志
                    throw new IllegalArgumentException("Root node " + node.getId() + " is not of type " + clazz.getSimpleName());
                }
            } else {
                AbstractTreeNode parentNode = nodeMap.get(parentId);
                if (Objects.nonNull(parentNode)) {
                    parentNode.addChild(node);
                } else {
                    throw new ApiException(ErrorCode.Business.COMMON_TREE_PARENT_NOT_EXIST, node.getId(), parentId);
                }
            }
        }
        return roots;
    }
}
