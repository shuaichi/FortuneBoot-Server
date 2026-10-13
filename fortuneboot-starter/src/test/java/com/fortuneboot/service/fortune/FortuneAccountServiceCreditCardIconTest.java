package com.fortuneboot.service.fortune;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fortuneboot.common.enums.fortune.AccountTypeEnum;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.vo.fortune.include.CreditCardVo;
import com.fortuneboot.factory.fortune.factory.FortuneAccountFactory;
import com.fortuneboot.factory.fortune.factory.FortuneGroupFactory;
import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 信用卡看板图标测试
 * <p>
 * 看板卡片展示账户自定义图标；未自定义时返回空值由前端按账户类型回退默认图标。
 */
class FortuneAccountServiceCreditCardIconTest {

    private final FortuneAccountRepo fortuneAccountRepo = mock(FortuneAccountRepo.class);

    private final FortuneAccountService fortuneAccountService = new FortuneAccountService(
        fortuneAccountRepo,
        mock(FortuneAccountFactory.class),
        mock(FortuneBillRepo.class),
        mock(FortuneBillService.class),
        mock(FortuneGroupFactory.class),
        mock(ApplicationScopeBo.class));

    @Test
    @DisplayName("信用卡看板应返回账户自定义图标")
    void getCreditCardOverview_returnsAccountIcon() {
        FortuneAccountEntity creditCard = new FortuneAccountEntity();
        creditCard.setAccountId(1L);
        creditCard.setAccountName("招行信用卡");
        creditCard.setAccountType(AccountTypeEnum.CREDIT.getValue());
        creditCard.setInclude(Boolean.TRUE);
        creditCard.setIcon("ep:credit-card");
        creditCard.setCreditLimit(new BigDecimal("10000"));
        creditCard.setBalance(new BigDecimal("-3000"));
        when(fortuneAccountRepo.getEnableAccountList(1L)).thenReturn(List.of(creditCard));

        List<CreditCardVo> result = fortuneAccountService.getCreditCardOverview(1L);

        assertThat(result).hasSize(1);
        CreditCardVo vo = result.get(0);
        assertThat(vo.getAccountName()).isEqualTo("招行信用卡");
        assertThat(vo.getIcon()).isEqualTo("ep:credit-card");
        assertThat(vo.getCreditLimit()).isEqualByComparingTo("10000");
        assertThat(vo.getUsedAmount()).isEqualByComparingTo("3000");
        assertThat(vo.getAvailable()).isEqualByComparingTo("7000");
    }

    @Test
    @DisplayName("非信用卡账户不进入看板，未自定义图标原样返回空值")
    void getCreditCardOverview_filtersNonCreditAndKeepsNullIcon() {
        FortuneAccountEntity currentAccount = new FortuneAccountEntity();
        currentAccount.setAccountId(2L);
        currentAccount.setAccountName("现金");
        currentAccount.setAccountType(AccountTypeEnum.CURRENT.getValue());
        currentAccount.setInclude(Boolean.TRUE);

        FortuneAccountEntity creditCardWithoutIcon = new FortuneAccountEntity();
        creditCardWithoutIcon.setAccountId(1L);
        creditCardWithoutIcon.setAccountName("信用账户");
        creditCardWithoutIcon.setAccountType(AccountTypeEnum.CREDIT.getValue());
        creditCardWithoutIcon.setInclude(Boolean.TRUE);
        creditCardWithoutIcon.setCreditLimit(BigDecimal.ZERO);
        creditCardWithoutIcon.setBalance(BigDecimal.ZERO);
        when(fortuneAccountRepo.getEnableAccountList(1L))
            .thenReturn(List.of(currentAccount, creditCardWithoutIcon));

        List<CreditCardVo> result = fortuneAccountService.getCreditCardOverview(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountId()).isEqualTo(1L);
        assertThat(result.get(0).getIcon()).isNull();
    }
}
