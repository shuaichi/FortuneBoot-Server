package com.fortuneboot.service.fortune.recurringBill;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.domain.command.fortune.FortuneRecurringBillRuleAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneRecurringBillRuleModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillLogEntity;
import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillRuleEntity;
import com.fortuneboot.domain.query.fortune.FortuneRecurringBillRuleQuery;
import com.fortuneboot.factory.fortune.factory.FortuneRecurringBillRuleFactory;
import com.fortuneboot.factory.fortune.model.FortuneRecurringBillRuleModel;
import com.fortuneboot.repository.fortune.FortuneRecurringBillLogRepository;
import com.fortuneboot.repository.fortune.FortuneRecurringBillRuleRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 周期记账服务
 *
 * @author zhangchi118
 * @date 2025/6/30 20:49
 **/
@Slf4j
@Service
@AllArgsConstructor
public class FortuneRecurringBillService {

    private final FortuneRecurringBillRuleFactory fortuneRecurringBillRuleFactory;
    private final FortuneRecurringBillRuleRepository fortuneRecurringBillRuleRepository;
    private final FortuneRecurringBillLogRepository fortuneRecurringBillLogRepository;

    private final FortuneRecurringBillScheduleService scheduleService;
    private final FortuneRecurringBillRecoveryService recoveryService;

    public IPage<FortuneRecurringBillRuleEntity> getRulePage(FortuneRecurringBillRuleQuery query) {
        return fortuneRecurringBillRuleRepository.page(query.toPage(), query.addQueryCondition());
    }

    public List<FortuneRecurringBillLogEntity> getLogByRuleId(Long bookId, Long ruleId) {
        FortuneRecurringBillRuleModel ruleModel = fortuneRecurringBillRuleFactory.loadById(ruleId);
        ruleModel.checkBookId(bookId);
        return fortuneRecurringBillLogRepository.getByRuleId(ruleId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addNewRule(FortuneRecurringBillRuleAddCommand addCommand) {
        FortuneRecurringBillRuleModel rule = fortuneRecurringBillRuleFactory.create();
        rule.loadAddCommand(addCommand);
        rule.checkCronValid();
        rule.insert();
        scheduleService.scheduleJob(rule);
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifyRule(FortuneRecurringBillRuleModifyCommand modifyCommand) {
        FortuneRecurringBillRuleModel rule = fortuneRecurringBillRuleFactory.loadById(modifyCommand.getRuleId());
        rule.loadModifyCommand(modifyCommand);
        rule.checkCronValid();
        rule.checkBookId(modifyCommand.getBookId());
        rule.updateById();
        scheduleService.scheduleJob(rule);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeRule(Long bookId, Long ruleId) {
        FortuneRecurringBillRuleModel rule = fortuneRecurringBillRuleFactory.loadById(ruleId);
        rule.checkBookId(bookId);
        rule.deleteById();
        scheduleService.deleteJob(ruleId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void enableRule(Long bookId, Long ruleId) {
        FortuneRecurringBillRuleModel rule = fortuneRecurringBillRuleFactory.loadById(ruleId);
        rule.checkBookId(bookId);
        rule.setEnable(Boolean.TRUE);
        rule.updateById();
        scheduleService.scheduleJob(rule);
    }

    @Transactional(rollbackFor = Exception.class)
    public void disableRule(Long bookId, Long ruleId) {
        FortuneRecurringBillRuleModel rule = fortuneRecurringBillRuleFactory.loadById(ruleId);
        rule.checkBookId(bookId);
        rule.setEnable(Boolean.FALSE);
        rule.updateById();
        scheduleService.deleteJob(ruleId);
    }

    /**
     * 应用启动时初始化
     */
    @PostConstruct
    @Transactional(rollbackFor = Exception.class)
    public void initRecurringBills() {
        log.info("开始初始化周期记账任务...");

        List<FortuneRecurringBillRuleModel> rules = fortuneRecurringBillRuleFactory.loadAllEnable();

        for (FortuneRecurringBillRuleModel rule : rules) {
            // 执行补偿逻辑
            recoveryService.performRecovery(rule);
            updateRecoveryCheckTime(rule.getRuleId(), LocalDateTime.now());

            // 创建定时任务
            scheduleService.scheduleJob(rule);
        }

        log.info("周期记账任务初始化完成，共加载{}个任务", rules.size());
    }

    private void updateRecoveryCheckTime(Long ruleId, LocalDateTime checkTime) {
        FortuneRecurringBillRuleModel rule = fortuneRecurringBillRuleFactory.loadById(ruleId);
        rule.setLastRecoveryCheck(checkTime);
        rule.updateById();
    }

    public Boolean checkCronExpression(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }
}