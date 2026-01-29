package com.fortuneboot.domain.query.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fortuneboot.common.core.base.BaseEntity;
import com.fortuneboot.common.core.page.AbstractLambdaPageQuery;
import com.fortuneboot.common.enums.common.DeleteEnum;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
    private Long bookId;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private String tradeTimeStartTime;

    /**
     * 交易结束时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private String tradeTimeEndTime;

    /**
     * 最小金额
     */
    private BigDecimal amountMin;

    /**
     * 最大金额
     */
    private BigDecimal amountMax;

    /**
     * 单据ID
     */
    private Long orderId;

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
     * TODO 待开发
     */
    private Boolean fileInclude;

    /**
     * 备注
     */
    private String remark;

    /**
     * 排序字段
     */
    private String orderField;

    @Override
    public void addSortCondition(LambdaQueryWrapper<FortuneBillEntity> queryWrapper) {
        // 获取前端传来的排序方向
        Boolean sortDirection = this.convertSortDirection();
        // 默认为倒序（如果 sortDirection 为 null，则 isAsc 为 false）
        boolean isAsc = sortDirection != null && sortDirection;

        // 获取排序字段，默认为 tradeTime（在 getOrderField 中定义）
        SFunction<FortuneBillEntity, ?> field = this.getOrderField();

        if (Objects.nonNull(field)) {
            queryWrapper.orderBy(Boolean.TRUE, isAsc, field);
        }

        // 固定添加 createTime 倒序
        queryWrapper.orderByDesc(FortuneBillEntity::getCreateTime);
    }

    @Override
    protected SFunction<FortuneBillEntity, ?> getOrderField() {
        if ("amount".equals(orderField)) {
            return FortuneBillEntity::getAmount;
        } else if ("tradeTime".equals(orderField)) {
            return FortuneBillEntity::getTradeTime;
        }
        return FortuneBillEntity::getTradeTime;
    }

    @Override
    public LambdaQueryWrapper<FortuneBillEntity> addQueryCondition() {
        QueryWrapper<FortuneBillEntity> queryWrapper = WrapperUtil.getQueryWrapper(FortuneBillEntity.class, Boolean.FALSE);
        queryWrapper.eq(Objects.nonNull(bookId), "bill.book_id", bookId)
                .eq(Objects.nonNull(billType), "bill.bill_type", billType)
                .and(Objects.nonNull(accountId), wrapper -> wrapper
                        .eq("bill.account_id", accountId)
                        .or()
                        .eq("bill.to_account_id", accountId)
                )
                .like(StringUtils.isNotBlank(title), "bill.title", title)
                .ge(StringUtils.isNotBlank(tradeTimeStartTime), "bill.trade_time", tradeTimeStartTime + " 00:00:00")
                .le(StringUtils.isNotBlank(tradeTimeEndTime), "bill.trade_time", tradeTimeEndTime + " 23:59:59")
                .ge(Objects.nonNull(amountMin), "bill.amount", amountMin)
                .le(Objects.nonNull(amountMax), "bill.amount", amountMax)
                .in(CollectionUtils.isNotEmpty(categoryIds), "fcr.category_id", categoryIds)
                .in(CollectionUtils.isNotEmpty(tagIds), "ftr.tag_id", tagIds)
                .eq(Objects.nonNull(payeeId), "bill.payee_id", payeeId)
                .eq(Objects.nonNull(confirm), "bill.confirm", confirm)
                .eq(Objects.nonNull(include), "bill.include", include)
                .eq(Objects.nonNull(orderId), "bill.order_id", orderId)
                .like(StringUtils.isNotBlank(remark), "bill.remark", remark)
                .eq("bill.deleted", DeleteEnum.VALID.getValue());

        // 根据ID进行分组
        LambdaQueryWrapper<FortuneBillEntity> lambda = queryWrapper.lambda();
        lambda.groupBy(FortuneBillEntity::getBillId);
        return lambda;
    }
}
