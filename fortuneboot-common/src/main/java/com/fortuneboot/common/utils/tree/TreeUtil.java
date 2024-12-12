package com.fortuneboot.common.utils.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 树形结构工具类
 *
 * @author zhangchi118
 * @date 2024/12/11 17:38
 **/
public class TreeUtil {
    public static <T extends AbstractTreeNode> List<T> buildForest(List<? extends AbstractTreeNode> dataList, Class<T> clazz) {
        Map<Long, AbstractTreeNode> nodeMap = new HashMap<>();
        List<T> roots = new ArrayList<>();
        // 转成map
        for (AbstractTreeNode item : dataList) {
            nodeMap.put(item.getId(), item);
        }
        // 构建树结构
        for (AbstractTreeNode node : nodeMap.values()) {
            if (node.getParentId() == null) {
                // 将根节点添加到根列表中
                if (clazz.isInstance(node)) {
                    roots.add(clazz.cast(node));
                }
            } else {
                AbstractTreeNode parentNode = nodeMap.get(node.getParentId());
                if (parentNode != null) {
                    parentNode.addChild(node);
                }
            }
        }
        return roots;
    }
}
