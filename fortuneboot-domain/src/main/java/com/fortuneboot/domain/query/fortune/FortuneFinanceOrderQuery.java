package com.fortuneboot.domain.query.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fortuneboot.common.core.page.AbstractLambdaPageQuery;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 *
 * @author zhangchi118
 * @date 2025/8/25 20:40
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneFinanceOrderQuery extends AbstractLambdaPageQuery<FortuneFinanceOrderEntity> {

    @NotBlank(message = "账本id不能为空")
    private Long bookId;

    /**
     * 标题
     */
    private String title;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    @Override
    public LambdaQueryWrapper<FortuneFinanceOrderEntity> addQueryCondition() {
        LambdaQueryWrapper<FortuneFinanceOrderEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneFinanceOrderEntity.class);
        queryWrapper.eq(FortuneFinanceOrderEntity::getBookId, bookId)
                .like(StringUtils.isNotBlank(title), FortuneFinanceOrderEntity::getTitle, title)
                .eq(Objects.nonNull(type), FortuneFinanceOrderEntity::getType, type)
                .eq(Objects.nonNull(status), FortuneFinanceOrderEntity::getStatus, status)
                .like(StringUtils.isNotBlank(remark), FortuneFinanceOrderEntity::getRemark, remark);
        return queryWrapper;
    }
}
