package com.fortuneboot.common.utils.tree;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangchi118
 * @date 2024/12/11 20:18
 **/
@Getter
public abstract class AbstractTreeNode implements TreeNodeData{

    private final List<AbstractTreeNode> children;

    public AbstractTreeNode() {
        this.children = new ArrayList<>();
    }

    public void addChild(AbstractTreeNode child) {
        this.children.add(child);
    }

}
