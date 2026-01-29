package com.fortuneboot.common.core.page;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fortuneboot.common.utils.time.DatePickUtil;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/15 23:47
 **/
@Data
public abstract class AbstractLambdaQuery<T> {

    protected String orderDirection;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date beginTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date endTime;

    private static final String ASC = "ascending";
    private static final String DESC = "descending";

    protected abstract SFunction<T, ?> getOrderField();

    protected abstract SFunction<T, Date> getTimeRangeField();

    /**
     * 生成query conditions
     *
     * @return 添加条件后的QueryWrapper
     */
    public LambdaQueryWrapper<T> toQueryWrapper() {
        LambdaQueryWrapper<T> queryWrapper = addQueryCondition();
        addSortCondition(queryWrapper);
        addTimeCondition(queryWrapper);
        return queryWrapper;
    }

    public abstract LambdaQueryWrapper<T> addQueryCondition();

    public void addSortCondition(LambdaQueryWrapper<T> queryWrapper) {
        if (ObjectUtil.isEmpty(queryWrapper) || ObjectUtil.isEmpty(this.getOrderField())) {
            return;
        }
        Boolean sortDirection = this.convertSortDirection();
        if (ObjectUtil.isNotEmpty(sortDirection)) {
            // 使用 if-else 语句来判断使用升序还是降序
            if (sortDirection) {
                queryWrapper.orderByAsc(this.getOrderField());
            } else {
                queryWrapper.orderByDesc(this.getOrderField());
            }
        }
    }

    public void addTimeCondition(LambdaQueryWrapper<T> queryWrapper) {
        if (ObjectUtil.isNotEmpty(queryWrapper) && ObjectUtil.isNotEmpty(this.getTimeRangeField())) {
            queryWrapper.ge(ObjectUtil.isNotEmpty(beginTime), this.getTimeRangeField(), DatePickUtil.getBeginOfTheDay(beginTime))
                    .le(ObjectUtil.isNotEmpty(endTime), this.getTimeRangeField(), DatePickUtil.getEndOfTheDay(endTime));
        }
    }

    /**
     * 获取前端传来的排序方向  转换成MyBatisPlus所需的排序参数 boolean=isAsc
     *
     * @return 排序顺序， null为无排序
     */
    public Boolean convertSortDirection() {
        Boolean isAsc = null;
        if (StrUtil.isEmpty(this.orderDirection)) {
            return isAsc;
        }

        if (ASC.equals(this.orderDirection)) {
            isAsc = true;
        }
        if (DESC.equals(this.orderDirection)) {
            isAsc = false;
        }
        return isAsc;
    }
}
