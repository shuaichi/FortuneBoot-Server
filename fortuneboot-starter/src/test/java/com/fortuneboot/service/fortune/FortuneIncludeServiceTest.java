package com.fortuneboot.service.fortune;

import com.fortuneboot.common.enums.fortune.IncomeExpenseCalendarGranularityEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.domain.vo.fortune.include.IncomeExpenseCalendarItemVo;
import com.fortuneboot.domain.vo.fortune.include.IncomeExpenseCalendarQuery;
import com.fortuneboot.repository.fortune.FortuneBalanceSnapshotRepo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneBookRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import com.fortuneboot.repository.system.SysConfigRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 收支日历统计测试
 *
 * @author zhangchi118
 * @date 2026/8/13
 **/
class FortuneIncludeServiceTest {

    private final FortuneBillRepo fortuneBillRepo = mock(FortuneBillRepo.class);

    private final FortuneIncludeService fortuneIncludeService = new FortuneIncludeService(
            mock(FortuneBillService.class),
            mock(FortuneAccountService.class),
            fortuneBillRepo,
            mock(FortuneBookRepo.class),
            mock(FortuneFinanceOrderRepo.class),
            mock(FortuneBalanceSnapshotRepo.class),
            mock(SysConfigRepo.class));

    @Test
    @DisplayName("按日收支日历补齐自然月并保留双向金额和笔数")
    void getIncomeExpenseCalendar_dayGranularity_completesMonthSeries() {
        IncomeExpenseCalendarQuery query = new IncomeExpenseCalendarQuery();
        query.setBookId(1L);
        query.setGranularity(IncomeExpenseCalendarGranularityEnum.DAY.getValue());
        query.setYear(2024);
        query.setMonth(2);
        query.setAccountIds(List.of(10L));

        IncomeExpenseCalendarItemVo item = new IncomeExpenseCalendarItemVo();
        item.setPeriod("2024-02-10");
        item.setIncome(new BigDecimal("100.25"));
        item.setExpense(new BigDecimal("20.50"));
        item.setIncomeCount(1);
        item.setExpenseCount(2);
        when(fortuneBillRepo.getIncomeExpenseCalendar(any())).thenReturn(List.of(item));

        var result = fortuneIncludeService.getIncomeExpenseCalendar(query);

        assertThat(result.getGranularity()).isEqualTo(IncomeExpenseCalendarGranularityEnum.DAY.getValue());
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2024, 2, 29));
        assertThat(result.getItems()).hasSize(29);
        assertThat(result.getItems().getFirst()).extracting(IncomeExpenseCalendarItemVo::getPeriod,
                IncomeExpenseCalendarItemVo::getIncome, IncomeExpenseCalendarItemVo::getExpense,
                IncomeExpenseCalendarItemVo::getIncomeCount, IncomeExpenseCalendarItemVo::getExpenseCount)
                .containsExactly("2024-02-01", BigDecimal.ZERO, BigDecimal.ZERO, 0, 0);
        assertThat(result.getItems().get(9)).isSameAs(item);
        assertThat(query.getStartDate()).isNull();
        assertThat(query.getEndDate()).isNull();
        verify(fortuneBillRepo).getIncomeExpenseCalendar(any());
    }

    @Test
    @DisplayName("按月收支日历补齐全年十二个月")
    void getIncomeExpenseCalendar_monthGranularity_completesYearSeries() {
        IncomeExpenseCalendarQuery query = new IncomeExpenseCalendarQuery();
        query.setBookId(1L);
        query.setGranularity(IncomeExpenseCalendarGranularityEnum.MONTH.getValue());
        query.setYear(2026);
        when(fortuneBillRepo.getIncomeExpenseCalendar(any())).thenReturn(List.of());

        var result = fortuneIncludeService.getIncomeExpenseCalendar(query);

        assertThat(result.getItems()).hasSize(12);
        assertThat(result.getItems().getFirst().getPeriod()).isEqualTo("2026-01");
        assertThat(result.getItems().getLast().getPeriod()).isEqualTo("2026-12");
        assertThat(result.getItems()).allSatisfy(item -> {
            assertThat(item.getIncome()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(item.getExpense()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(item.getIncomeCount()).isZero();
            assertThat(item.getExpenseCount()).isZero();
        });
    }

    @Test
    @DisplayName("按年收支日历补齐历史年份并限制十年")
    void getIncomeExpenseCalendar_yearGranularity_completesYearsAndRejectsOversizeRange() {
        IncomeExpenseCalendarQuery query = new IncomeExpenseCalendarQuery();
        query.setBookId(1L);
        query.setGranularity(IncomeExpenseCalendarGranularityEnum.YEAR.getValue());
        query.setStartYear(2022);
        query.setEndYear(2024);
        when(fortuneBillRepo.getIncomeExpenseCalendar(any())).thenReturn(List.of());

        var result = fortuneIncludeService.getIncomeExpenseCalendar(query);

        assertThat(result.getItems()).extracting(IncomeExpenseCalendarItemVo::getPeriod)
                .containsExactly("2022", "2023", "2024");

        IncomeExpenseCalendarQuery oversizedQuery = new IncomeExpenseCalendarQuery();
        oversizedQuery.setBookId(1L);
        oversizedQuery.setGranularity(IncomeExpenseCalendarGranularityEnum.YEAR.getValue());
        oversizedQuery.setStartYear(2014);
        oversizedQuery.setEndYear(2024);

        assertThatThrownBy(() -> fortuneIncludeService.getIncomeExpenseCalendar(oversizedQuery))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isNotNull();
    }

    @Test
    @DisplayName("按日收支日历缺少月份时拒绝查询")
    void getIncomeExpenseCalendar_dayGranularityWithoutMonth_throwsApiException() {
        IncomeExpenseCalendarQuery query = new IncomeExpenseCalendarQuery();
        query.setBookId(1L);
        query.setGranularity(IncomeExpenseCalendarGranularityEnum.DAY.getValue());
        query.setYear(2026);

        assertThatThrownBy(() -> fortuneIncludeService.getIncomeExpenseCalendar(query))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isNotNull();
        verifyNoInteractions(fortuneBillRepo);
    }
}
