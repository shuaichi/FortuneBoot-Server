package com.fortuneboot.common.core.page;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Max;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/16 00:24
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractLambdaPageQuery <T> extends AbstractLambdaQuery<T> {

    /**
     * 最大分页页数
     */
    public static final int MAX_PAGE_NUM = 200;
    /**
     * 单页最大大小
     */
    public static final int MAX_PAGE_SIZE = 500;
    /**
     * 默认分页页数
     */
    public static final int DEFAULT_PAGE_NUM = 1;
    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 如果需要排序，需要重写getOrderField和getTimeRangeField两个方法
     * @return
     */
    @Override
    protected SFunction<T, ?> getOrderField() {
        return null;
    }

    @Override
    protected SFunction<T, Date> getTimeRangeField() {
        return null;
    }

    @Max(MAX_PAGE_NUM)
    protected Integer pageNum;

    @Max(MAX_PAGE_SIZE)
    protected Integer pageSize;

    public Page<T> toPage() {
        pageNum = ObjectUtil.defaultIfNull(pageNum, DEFAULT_PAGE_NUM);
        pageSize = ObjectUtil.defaultIfNull(pageSize, DEFAULT_PAGE_SIZE);
        return new Page<>(pageNum, pageSize);
    }
}
