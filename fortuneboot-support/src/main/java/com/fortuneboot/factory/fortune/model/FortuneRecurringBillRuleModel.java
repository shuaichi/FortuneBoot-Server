package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.common.utils.jackson.JacksonUtil;
import com.fortuneboot.domain.command.fortune.FortuneRecurringBillRuleAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneRecurringBillRuleModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillRuleEntity;
import com.fortuneboot.repository.fortune.FortuneRecurringBillRuleRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.quartz.CronExpression;

import java.time.LocalDate;
import java.util.Objects;

/**
 * @author zhangchi118
 * @date 2025/7/3 17:02
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneRecurringBillRuleModel extends FortuneRecurringBillRuleEntity {

    private final FortuneRecurringBillRuleRepository fortuneRecurringBillRuleRepository;

    public FortuneRecurringBillRuleModel(FortuneRecurringBillRuleRepository repository) {
        this.fortuneRecurringBillRuleRepository = repository;
    }

    public FortuneRecurringBillRuleModel(FortuneRecurringBillRuleEntity entity, FortuneRecurringBillRuleRepository repository) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneRecurringBillRuleRepository = repository;
    }

    public void loadAddCommand(FortuneRecurringBillRuleAddCommand command) {
        if (Objects.nonNull(command)) {
            BeanUtil.copyProperties(command, this, "ruleId");
            // 将billRequest设置为json字符串
            this.setBillRequest(JacksonUtil.to(command.getBillRequest()));
        }
    }

    public void loadModifyCommand(FortuneRecurringBillRuleModifyCommand command) {
        if (Objects.isNull(command)) {
            return;
        }
        this.loadAddCommand(command);
    }

    public void checkCronValid() {
        if (!CronExpression.isValidExpression(this.getCronExpression())) {
            throw new ApiException(ErrorCode.Business.RECURRING_BILL_CRON_ILLEGAL, this.getCronExpression());
        }
    }

    public void checkBookId(Long bookId) {
        if (!Objects.equals(bookId, this.getBookId())) {
            throw new ApiException(ErrorCode.Business.RECURRING_BILL_BOOK_NOT_MATCH);
        }
    }

    public Boolean checkOverExecutions() {
        return Objects.nonNull(this.getMaxExecutions()) && this.getExecutedCount() >= this.getMaxExecutions();
    }


    public boolean checkOverEndDate(LocalDate localDate) {
        return Objects.nonNull(this.getEndDate()) && localDate.isAfter(this.getEndDate());
    }

    /**
     * 检查是否在开始日期之前
     */
    public boolean checkBeforeStartDate(LocalDate localDate) {
        return Objects.nonNull(this.getStartDate()) && localDate.isBefore(this.getStartDate());
    }

    /**
     * 检查当前是否可以执行
     */
    public boolean canExecuteNow() {
        LocalDate today = LocalDate.now();
        return !checkBeforeStartDate(today) && !checkOverEndDate(today);
    }
}
