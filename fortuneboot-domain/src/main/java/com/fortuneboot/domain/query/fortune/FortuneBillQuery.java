package com.fortuneboot.domain.query.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fortuneboot.common.core.page.AbstractLambdaPageQuery;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 23:04
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneBillQuery extends AbstractLambdaPageQuery<FortuneBillEntity> {

    /**
     * 账本id
     */
    @NotNull(message = "账本id不能为空")
    @Positive
    private String bookId;

    /**
     * 账本类型
     */
    private Integer bookType;

    /**
     * 账户id
     */
    private Integer accountId;

    /**
     * 账单类型
     */
    private Integer billType;

    /**
     * 标题
     */
    private String title;

    /**
     * 交易开始时间
     */
    private String tradeTimeStartTime;

    /**
     * 交易结束时间
     */
    private String tradeTimeEndTime;

    /**
     * 最小金额
     */
    private Integer amountMin;

    /**
     * 最大金额
     */
    private Integer amountMax;

    /**
     * 分类id
     */
    private List<Integer> categoryIds;

    /**
     * 标签id
     */
    private List<Integer> tagIds;

    /**
     * 交易对象
     */
    private Integer payeeId;

    /**
     * 是否确认
     */
    private Boolean confirm;

    /**
     * 是否统计
     */
    private Boolean include;

    /**
     * 是否有文件
     */
    private Boolean fileInclude;

    /**
     * 备注
     */
    private String remark;

    @Override
    public LambdaQueryWrapper<FortuneBillEntity> addQueryCondition() {
        LambdaQueryWrapper<FortuneBillEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBillEntity.class);
        queryWrapper.eq(FortuneBillEntity::getBookId, bookId)
        ;
        return queryWrapper;
    }
}
