package com.fortuneboot.domain.common.dto;

import cn.hutool.core.lang.tree.Tree;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author valarchie
 */
@Data
@NoArgsConstructor
public class TreeSelectedDTO {

    private List<Long> checkedKeys;
    private List<Tree<Long>> menus;

}
