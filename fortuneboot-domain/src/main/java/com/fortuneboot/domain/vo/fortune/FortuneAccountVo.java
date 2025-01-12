package com.fortuneboot.domain.vo.fortune;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/10 23:04
 **/
@Data
public class FortuneAccountVo {

    public FortuneAccountVo(FortuneAccountEntity entity){
        if (ObjectUtil.isNotEmpty(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /**
     * id
     */
    private Long accountId;

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 用户名
     */
    private String accountName;

    /**
     * 余额
     */
    private BigDecimal balance;

    /**
     *账单日
     */
    private LocalDate billDay;

    /**
     *还款日
     */
    private LocalDate repayDay;

    /**
     *可支出
     */
    private Boolean canExpense;

    /**
     *可收入
     */
    private Boolean canIncome;

    /**
     *可转出
     */
    private Boolean canTransferOut;

    /**
     *可转入
     */
    private Boolean canTransferIn;

    /**
     *信用额度
     */
    private BigDecimal creditLimit;

    /**
     *币种
     */
    private String currencyCode;

    /**
     *是否启用
     */
    private Boolean enable;

    /**
     *是否计入净资产
     */
    private Boolean include;

    /**
     *利率
     */
    private BigDecimal apr;

    /**
     *期初余额
     */
    private BigDecimal initialBalance;

    /**
     * 账户类型
     * com.fortuneboot.common.enums.fortune.AccountTypeEnum
     */
    private Integer accountType;

    /**
     *分组id
     */
    private Long groupId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     *备注
     */
    private String remark;
}
