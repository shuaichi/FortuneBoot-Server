package com.fortuneboot.fortune;

import com.fortuneboot.common.core.base.BaseEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneGoodsKeeperEntity;
import com.fortuneboot.integrationTest.IntegrationTestApplication;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneGoodsKeeperRepo;
import com.fortuneboot.service.fortune.FortuneGoodsKeeperService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 归物关联账单「自动叠加成本」逻辑测试（支出加 / 收入减，忽略回收站与已删除）。
 *
 * @author fortuneboot
 * @date 2026/10/23
 **/
@Slf4j
@SpringBootTest(classes = IntegrationTestApplication.class)
@ActiveProfiles({"basic", "test"})
public class FortuneGoodsKeeperCostTest {

    @Resource
    private FortuneGoodsKeeperService fortuneGoodsKeeperService;
    @Resource
    private FortuneGoodsKeeperRepo fortuneGoodsKeeperRepo;
    @Resource
    private FortuneBillRepo fortuneBillRepo;

    private FortuneGoodsKeeperEntity newKeeper() {
        FortuneGoodsKeeperEntity keeper = new FortuneGoodsKeeperEntity();
        keeper.setGoodsName("成本测试归物");
        keeper.setBookId(1L);
        keeper.setCategoryId(1L);
        keeper.setPrice(new BigDecimal("500"));
        keeper.setPurchaseDate(LocalDate.now());
        keeper.setDeleted(false);
        return keeper;
    }

    private FortuneBillEntity newBill(int billType, String amount, Long keeperId,
                                      boolean deleted, boolean recycleBin) {
        FortuneBillEntity bill = new FortuneBillEntity();
        bill.setBookId(1L);
        bill.setBillType(billType);
        bill.setAmount(new BigDecimal(amount));
        bill.setGoodsKeeperId(keeperId);
        bill.setDeleted(deleted);
        bill.setRecycleBin(recycleBin);
        return bill;
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void testRecalculateAccumulatedCost_expenseMinusIncome() {
        FortuneGoodsKeeperEntity keeper = newKeeper();
        fortuneGoodsKeeperRepo.save(keeper);
        Long keeperId = keeper.getGoodsKeeperId();

        // 支出 100，收入 30，回收站 50（应忽略），已删除 20（应忽略）
        fortuneBillRepo.save(newBill(1, "100", keeperId, false, false));
        fortuneBillRepo.save(newBill(2, "30", keeperId, false, false));
        fortuneBillRepo.save(newBill(1, "50", keeperId, false, true));
        fortuneBillRepo.save(newBill(1, "20", keeperId, true, false));

        fortuneGoodsKeeperService.recalculateAccumulatedCost(keeperId);

        FortuneGoodsKeeperEntity reloaded = fortuneGoodsKeeperRepo.getById(keeperId);
        // 100 - 30 = 70
        assertThat(reloaded.getAccumulatedCost()).isEqualByComparingTo("70");
        log.info("[testRecalculateAccumulatedCost] accumulatedCost = {}", reloaded.getAccumulatedCost());
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void testRecalculateAccumulatedCost_noLinkedBill_isZero() {
        FortuneGoodsKeeperEntity keeper = newKeeper();
        fortuneGoodsKeeperRepo.save(keeper);
        Long keeperId = keeper.getGoodsKeeperId();

        fortuneGoodsKeeperService.recalculateAccumulatedCost(keeperId);

        FortuneGoodsKeeperEntity reloaded = fortuneGoodsKeeperRepo.getById(keeperId);
        assertThat(reloaded.getAccumulatedCost()).isEqualByComparingTo("0");
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void testRecalculateAccumulatedCost_incomeExceedsExpense_negative() {
        FortuneGoodsKeeperEntity keeper = newKeeper();
        fortuneGoodsKeeperRepo.save(keeper);
        Long keeperId = keeper.getGoodsKeeperId();

        // 支出 40，收入 100 -> 净成本 -60
        fortuneBillRepo.save(newBill(1, "40", keeperId, false, false));
        fortuneBillRepo.save(newBill(2, "100", keeperId, false, false));

        fortuneGoodsKeeperService.recalculateAccumulatedCost(keeperId);

        FortuneGoodsKeeperEntity reloaded = fortuneGoodsKeeperRepo.getById(keeperId);
        assertThat(reloaded.getAccumulatedCost()).isEqualByComparingTo("-60");
        log.info("[testRecalculateAccumulatedCost_negative] accumulatedCost = {}", reloaded.getAccumulatedCost());
    }
}
