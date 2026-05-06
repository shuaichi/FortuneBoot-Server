package com.fortuneboot.strategy.bill;

import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillExtraAddCommand;
import com.fortuneboot.factory.fortune.model.FortuneAccountModel;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.factory.fortune.model.FortuneBookModel;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 账单策略上下文
 *
 * @author zhangchi118
 * @date 2025/8/25 21:26
 **/
@Data
public class BillStrategyContext {

    /**
     * 手续费类型
     */
    private static final Integer EXTRA_TYPE_FEE = 1;

    /**
     * 优惠类型
     */
    private static final Integer EXTRA_TYPE_DISCOUNT = 2;

    /**
     * 账户方向：转出账户(from)
     */
    private static final Integer ACCOUNT_SIDE_FROM = 1;

    /**
     * 账户方向：转入账户(to)
     */
    private static final Integer ACCOUNT_SIDE_TO = 2;

    private FortuneBillAddCommand command;

    /**
     * 账单
     */
    private FortuneBillModel billModel;

    /**
     * 账本
     */
    private FortuneBookModel bookModel;

    /**
     * 原账户
     */
    private FortuneAccountModel fromAccount;

    /**
     * 目的账户
     */
    private FortuneAccountModel toAccount;

    // ========== from 侧汇总 ==========

    /**
     * 获取转出账户(from)侧的手续费合计
     */
    public BigDecimal getFromTotalFee() {
        return sumExtraAmount(EXTRA_TYPE_FEE, ACCOUNT_SIDE_FROM);
    }

    /**
     * 获取转出账户(from)侧的优惠合计
     */
    public BigDecimal getFromTotalDiscount() {
        return sumExtraAmount(EXTRA_TYPE_DISCOUNT, ACCOUNT_SIDE_FROM);
    }

    // ========== to 侧汇总 ==========

    /**
     * 获取转入账户(to)侧的手续费合计
     */
    public BigDecimal getToTotalFee() {
        return sumExtraAmount(EXTRA_TYPE_FEE, ACCOUNT_SIDE_TO);
    }

    /**
     * 获取转入账户(to)侧的优惠合计
     */
    public BigDecimal getToTotalDiscount() {
        return sumExtraAmount(EXTRA_TYPE_DISCOUNT, ACCOUNT_SIDE_TO);
    }

    // ========== 兼容：不区分方向的全量汇总（支出场景只有 from） ==========

    /**
     * 获取手续费合计（不区分账户方向，支出场景使用）
     */
    public BigDecimal getTotalFee() {
        return sumExtraAmount(EXTRA_TYPE_FEE, null);
    }

    /**
     * 获取优惠合计（不区分账户方向，支出场景使用）
     */
    public BigDecimal getTotalDiscount() {
        return sumExtraAmount(EXTRA_TYPE_DISCOUNT, null);
    }

    // ========== 内部方法 ==========

    /**
     * 汇总附加费用中指定类型和方向的金额
     *
     * @param extraType   附加类型：1-手续费，2-优惠
     * @param accountSide 账户方向：1-from，2-to；null 表示不区分方向
     * @return 总金额，永不为 null
     */
    private BigDecimal sumExtraAmount(Integer extraType, Integer accountSide) {
        if (Objects.isNull(command) || CollectionUtils.isEmpty(command.getExtras())) {
            return BigDecimal.ZERO;
        }
        List<FortuneBillExtraAddCommand> extras = command.getExtras();
        return extras.stream()
                .filter(Objects::nonNull)
                .filter(e -> Objects.equals(extraType, e.getExtraType()))
                .filter(e -> Objects.isNull(accountSide) || Objects.equals(accountSide, e.getAccountSide()))
                .map(FortuneBillExtraAddCommand::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}