package com.fortuneboot.domain.query.system;

import cn.hutool.core.util.StrUtil;
import com.fortuneboot.common.core.page.AbstractPageQuery;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 当出现复用Query的情况，我们需要把泛型加到类本身，通过传入类型 来进行复用
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SearchUserQuery<T> extends AbstractPageQuery<T> {

    protected Long userId;
    protected String username;
    protected Integer status;
    protected String phoneNumber;

    @Override
    public QueryWrapper<T> addQueryCondition() {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();

        queryWrapper.like(StrUtil.isNotEmpty(username), "username", username)
            .like(StrUtil.isNotEmpty(phoneNumber), "u.phone_number", phoneNumber)
            .eq(userId != null, "u.user_id", userId)
            .eq(status != null, "u.status", status)
            .eq("u.deleted", 0);

        // 设置排序字段
        this.timeRangeColumn = "u.create_time";

        return queryWrapper;
    }
}
