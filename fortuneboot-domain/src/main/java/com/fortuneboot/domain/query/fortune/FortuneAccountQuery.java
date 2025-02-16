package com.fortuneboot.domain.query.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fortuneboot.common.core.page.AbstractLambdaPageQuery;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 账户查询
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/10 23:05
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneAccountQuery extends AbstractLambdaPageQuery<FortuneAccountEntity> {

    /**
     * 分组id
     */
    @NotNull
    @Positive
    private Long groupId;

    /**
     * 账户类型
     */
    @Positive
    private Long accountType;

    /**
     * 账户名称
     */
    private String accountName;

    /**
     * 余额下限
     */
    private BigDecimal lowerBalance;

    /**
     * 余额上限
     */
    private BigDecimal limitBalance;

    /**
     * 币种
     */
    private String currencyCode;

    /**
     * 是否计入净资产
     */
    private Boolean include;

    /**
     * 可支出
     */
    private Boolean canExpense;

    /**
     * 可收入
     */
    private Boolean canIncome;

    /**
     * 可转出
     */
    private Boolean canTransferOut;

    /**
     * 可转入
     */
    private Boolean canTransferIn;

    /**
     * 回收站
     */
    private Boolean recycleBin;


    @Override
    public LambdaQueryWrapper<FortuneAccountEntity> addQueryCondition() {
        LambdaQueryWrapper<FortuneAccountEntity> lambdaQueryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneAccountEntity.class);
        lambdaQueryWrapper.eq(FortuneAccountEntity::getGroupId,groupId)
                .eq(FortuneAccountEntity::getRecycleBin,recycleBin)
                .eq(Objects.nonNull(accountType),FortuneAccountEntity::getAccountType, accountType)
                .like(StringUtils.isNotBlank(accountName), FortuneAccountEntity::getAccountName, accountName)
                .ge(Objects.nonNull(lowerBalance), FortuneAccountEntity::getBalance, lowerBalance)
                .le(Objects.nonNull(limitBalance), FortuneAccountEntity::getBalance, limitBalance)
                .eq(StringUtils.isNotBlank(currencyCode), FortuneAccountEntity::getCurrencyCode, currencyCode)
                .eq(Objects.nonNull(include), FortuneAccountEntity::getInclude, include)
                .eq(Objects.nonNull(canExpense), FortuneAccountEntity::getCanExpense, canExpense)
                .eq(Objects.nonNull(canIncome), FortuneAccountEntity::getCanIncome, canIncome)
                .eq(Objects.nonNull(canTransferOut), FortuneAccountEntity::getCanTransferOut, canTransferOut)
                .eq(Objects.nonNull(canTransferIn), FortuneAccountEntity::getCanTransferIn, canTransferIn);
        return lambdaQueryWrapper;
    }
}
