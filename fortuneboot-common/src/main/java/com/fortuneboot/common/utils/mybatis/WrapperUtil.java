package com.fortuneboot.common.utils.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fortuneboot.common.core.base.BaseEntity;
import com.fortuneboot.common.enums.common.DeleteEnum;

/**
 * Mybatis Wrapper Util
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/10 22:58
 **/
public class WrapperUtil {

    public WrapperUtil() {}

    public static <T extends BaseEntity<?>> QueryWrapper<T> getQueryWrapper(Class<T> clazz) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.setEntityClass(clazz);
        queryWrapper.eq("deleted",DeleteEnum.VALID.getValue());
        return queryWrapper;
    }

    public static <T extends BaseEntity<?>> QueryWrapper<T> getQueryWrapper(Class<T> clazz,Boolean deleted) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.setEntityClass(clazz);
        queryWrapper.eq(deleted,"deleted",DeleteEnum.VALID.getValue());
        return queryWrapper;
    }

    public static <T extends BaseEntity<?>> LambdaQueryWrapper<T> getLambdaQueryWrapper(Class<T> clazz) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.setEntityClass(clazz);
        return queryWrapper.lambda().eq(BaseEntity::getDeleted, DeleteEnum.VALID.getValue());
    }

    public static <T extends BaseEntity<?>> UpdateWrapper<T> getUpdateWrapper(Class<T> clazz) {
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setEntityClass(clazz);
        updateWrapper.eq("deleted",DeleteEnum.VALID.getValue());
        return updateWrapper;
    }

    public static <T extends BaseEntity<?>> LambdaUpdateWrapper<T> getLambdaUpdateWrapper(Class<T> clazz) {
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setEntityClass(clazz);
        return updateWrapper.lambda().eq(BaseEntity::getDeleted, DeleteEnum.VALID.getValue());
    }
}
