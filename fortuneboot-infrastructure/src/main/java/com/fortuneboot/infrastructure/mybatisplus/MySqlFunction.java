package com.fortuneboot.infrastructure.mybatisplus;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import java.util.List;

/**
 * MySQL 函数的自定义实现
 *
 * @author valarchie
 */
public class MySqlFunction {

    private MySqlFunction() {
    }

    public static boolean findInSet(String target, String setString) {
        if (setString == null) {
            return false;
        }

        List<String> split = StrUtil.split(setString, ",");

        return CollUtil.contains(split, target);
    }

}
