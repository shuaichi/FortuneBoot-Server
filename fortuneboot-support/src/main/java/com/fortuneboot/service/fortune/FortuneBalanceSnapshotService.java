package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBalanceSnapshotEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBillExtraEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import com.fortuneboot.repository.fortune.FortuneBalanceSnapshotRepo;
import com.fortuneboot.repository.fortune.FortuneBillExtraRepo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneBookRepo;
import com.fortuneboot.service.fortune.snapshot.AccountBalanceChange;
import com.fortuneboot.service.fortune.snapshot.FortuneBillBalanceDeltaCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 账户余额快照服务
 *
 * @author zhangchi118
 * @date 2026/8/4 20:47
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneBalanceSnapshotService {

    private static final int BILL_PAGE_SIZE = 1000;
    private static final int SNAPSHOT_BATCH_SIZE = 1000;
    private static final int REBUILD_YEARS = 5;

    private final FortuneBookRepo fortuneBookRepo;
    private final FortuneAccountRepo fortuneAccountRepo;
    private final FortuneBalanceSnapshotRepo fortuneBalanceSnapshotRepo;
    private final FortuneAccountService fortuneAccountService;
    private final FortuneBillRepo fortuneBillRepo;
    private final FortuneBillExtraRepo fortuneBillExtraRepo;
    private final FortuneBillBalanceDeltaCalculator balanceDeltaCalculator;

    @Transactional(rollbackFor = Exception.class)
    public void generateMonthEndSnapshot() {
        rebuildDailySnapshots();
    }

    @Transactional(rollbackFor = Exception.class)
    public void generateSnapshot(LocalDate snapshotDate) {
        rebuildDailySnapshots();
    }

    @Transactional(rollbackFor = Exception.class)
    public void rebuildDailySnapshots() {
        rebuildDailySnapshots(LocalDate.now());
    }

    @Transactional(rollbackFor = Exception.class)
    public void rebuildDailySnapshots(LocalDate anchorDate) {
        List<FortuneAccountEntity> accounts = listSnapshotAccounts();
        if (accounts.isEmpty()) {
            fortuneBalanceSnapshotRepo.removeAllSnapshots();
            log.info("账户余额每日快照重算完成：无可纳入快照的账户，已清空旧快照，锚点日期: {}", anchorDate);
            return;
        }

        LocalDate windowStartDate = anchorDate.minusYears(REBUILD_YEARS);
        ChangeLoadResult changeLoadResult = loadAllChanges(anchorDate, windowStartDate);
        LocalDate startDate = maxDate(Objects.requireNonNullElse(changeLoadResult.startDate(), anchorDate), windowStartDate);
        Map<LocalDate, Map<Long, BigDecimal>> dailyDeltaMap = changeLoadResult.dailyDeltaMap();
        Map<Long, BigDecimal> balanceMap = buildAnchorBalanceMap(accounts, changeLoadResult.futureDeltaMap());
        Map<Long, FortuneBookEntity> bookMap = fortuneBookRepo.list().stream()
                .filter(book -> book.getBookId() != null)
                .collect(Collectors.toMap(FortuneBookEntity::getBookId, book -> book, (left, right) -> left));

        fortuneBalanceSnapshotRepo.removeAllSnapshots();
        long snapshotCount = 0L;
        for (LocalDate date = anchorDate; !date.isBefore(startDate); date = date.minusDays(1)) {
            List<FortuneBalanceSnapshotEntity> snapshots = buildSnapshots(date, accounts, balanceMap, bookMap);
            if (!snapshots.isEmpty()) {
                fortuneBalanceSnapshotRepo.saveBatch(snapshots, SNAPSHOT_BATCH_SIZE);
                snapshotCount += snapshots.size();
            }
            Map<Long, BigDecimal> dailyDelta = dailyDeltaMap.getOrDefault(date, Map.of());
            for (Map.Entry<Long, BigDecimal> entry : dailyDelta.entrySet()) {
                balanceMap.computeIfPresent(entry.getKey(), (accountId, balance) -> balance.subtract(entry.getValue()));
            }
        }

        log.info("账户余额每日快照重算完成，起始日期: {}，锚点日期: {}，账户数: {}，账单数: {}，快照数: {}",
                startDate, anchorDate, accounts.size(), changeLoadResult.billCount(), snapshotCount);
    }

    private List<FortuneAccountEntity> listSnapshotAccounts() {
        return fortuneAccountRepo.list().stream()
                .filter(account -> Boolean.TRUE.equals(account.getEnable()))
                .filter(account -> Boolean.TRUE.equals(account.getInclude()))
                .filter(account -> Boolean.FALSE.equals(account.getRecycleBin()))
                .toList();
    }

    private ChangeLoadResult loadAllChanges(LocalDate anchorDate, LocalDate windowStartDate) {
        Map<LocalDate, Map<Long, BigDecimal>> dailyDeltaMap = new HashMap<>();
        Map<Long, BigDecimal> futureDeltaMap = new HashMap<>();
        LocalDate startDate = null;
        long billCount = 0L;
        long pageNo = 1L;
        while (true) {
            Page<FortuneBillEntity> page = fortuneBillRepo.page(new Page<>(pageNo, BILL_PAGE_SIZE), confirmedBillWrapper(windowStartDate));
            List<FortuneBillEntity> bills = page.getRecords();
            if (bills.isEmpty()) {
                break;
            }
            Map<Long, List<FortuneBillExtraEntity>> extraMap = loadBillExtras(bills);
            for (AccountBalanceChange change : calculateChanges(bills, extraMap)) {
                if (change.tradeDate().isAfter(anchorDate)) {
                    futureDeltaMap.merge(change.accountId(), change.amount(), BigDecimal::add);
                    continue;
                }
                dailyDeltaMap.computeIfAbsent(change.tradeDate(), date -> new HashMap<>())
                        .merge(change.accountId(), change.amount(), BigDecimal::add);
                if (startDate == null || change.tradeDate().isBefore(startDate)) {
                    startDate = change.tradeDate();
                }
            }
            billCount += bills.size();
            if (pageNo >= page.getPages()) {
                break;
            }
            pageNo++;
        }
        return new ChangeLoadResult(dailyDeltaMap, futureDeltaMap, startDate, billCount);
    }

    private LambdaQueryWrapper<FortuneBillEntity> confirmedBillWrapper(LocalDate windowStartDate) {
        LambdaQueryWrapper<FortuneBillEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBillEntity.class);
        wrapper.eq(FortuneBillEntity::getConfirm, Boolean.TRUE)
                .eq(FortuneBillEntity::getRecycleBin, Boolean.FALSE)
                .ge(FortuneBillEntity::getTradeTime, LocalDateTime.of(windowStartDate, LocalTime.MIN))
                .orderByAsc(FortuneBillEntity::getTradeTime)
                .orderByAsc(FortuneBillEntity::getBillId);
        return wrapper;
    }

    private Map<Long, List<FortuneBillExtraEntity>> loadBillExtras(List<FortuneBillEntity> bills) {
        List<Long> billIds = bills.stream()
                .map(FortuneBillEntity::getBillId)
                .filter(Objects::nonNull)
                .toList();
        return fortuneBillExtraRepo.getByBillIdList(billIds);
    }

    private List<AccountBalanceChange> calculateChanges(List<FortuneBillEntity> bills, Map<Long, List<FortuneBillExtraEntity>> extraMap) {
        List<AccountBalanceChange> changes = new ArrayList<>();
        for (FortuneBillEntity bill : bills) {
            List<FortuneBillExtraEntity> extras = extraMap.getOrDefault(bill.getBillId(), List.of());
            changes.addAll(balanceDeltaCalculator.calculate(bill, extras));
        }
        return changes;
    }

    private Map<Long, BigDecimal> buildAnchorBalanceMap(List<FortuneAccountEntity> accounts,
                                                         Map<Long, BigDecimal> futureDeltaMap) {
        Map<Long, BigDecimal> balanceMap = new HashMap<>();
        for (FortuneAccountEntity account : accounts) {
            balanceMap.put(account.getAccountId(), Objects.requireNonNullElse(account.getBalance(), BigDecimal.ZERO));
        }
        for (Map.Entry<Long, BigDecimal> entry : futureDeltaMap.entrySet()) {
            balanceMap.computeIfPresent(entry.getKey(), (accountId, balance) -> balance.subtract(entry.getValue()));
        }
        return balanceMap;
    }

    private List<FortuneBalanceSnapshotEntity> buildSnapshots(LocalDate snapshotDate,
                                                              List<FortuneAccountEntity> accounts,
                                                              Map<Long, BigDecimal> balanceMap,
                                                              Map<Long, FortuneBookEntity> bookMap) {
        List<FortuneBalanceSnapshotEntity> snapshots = new ArrayList<>();
        for (Map.Entry<Long, List<FortuneAccountEntity>> entry : accounts.stream().collect(Collectors.groupingBy(FortuneAccountEntity::getGroupId)).entrySet()) {
            Long groupId = entry.getKey();
            BigDecimal totalAssets = BigDecimal.ZERO;
            BigDecimal totalLiabilities = BigDecimal.ZERO;
            List<FortuneBalanceSnapshotEntity> groupSnapshots = new ArrayList<>();
            for (FortuneAccountEntity account : entry.getValue()) {
                if (!accountExistsOn(account, snapshotDate)) {
                    continue;
                }
                BigDecimal balance = balanceMap.getOrDefault(account.getAccountId(), BigDecimal.ZERO);
                BigDecimal converted = fortuneAccountService.convertToGroupCurrency(groupId, account, balance);
                if (converted.compareTo(BigDecimal.ZERO) >= 0) {
                    totalAssets = totalAssets.add(converted);
                } else {
                    totalLiabilities = totalLiabilities.add(converted.abs());
                }
                FortuneBalanceSnapshotEntity snapshot = new FortuneBalanceSnapshotEntity();
                snapshot.setGroupId(groupId);
                snapshot.setBookId(resolveBookId(bookMap, groupId));
                snapshot.setAccountId(account.getAccountId());
                snapshot.setSnapshotDate(snapshotDate);
                snapshot.setCurrencyCode(account.getCurrencyCode());
                snapshot.setBalance(balance);
                snapshot.setConvertedBalance(converted);
                groupSnapshots.add(snapshot);
            }
            BigDecimal netAssets = totalAssets.subtract(totalLiabilities);
            for (FortuneBalanceSnapshotEntity snapshot : groupSnapshots) {
                snapshot.setTotalAssets(totalAssets);
                snapshot.setTotalLiabilities(totalLiabilities);
                snapshot.setNetAssets(netAssets);
            }
            snapshots.addAll(groupSnapshots);
        }
        return snapshots;
    }

    private boolean accountExistsOn(FortuneAccountEntity account, LocalDate snapshotDate) {
        return account.getCreateTime() == null || !account.getCreateTime().toLocalDate().isAfter(snapshotDate);
    }

    private LocalDate maxDate(LocalDate left, LocalDate right) {
        return left.isAfter(right) ? left : right;
    }

    private record ChangeLoadResult(Map<LocalDate, Map<Long, BigDecimal>> dailyDeltaMap,
                                    Map<Long, BigDecimal> futureDeltaMap,
                                    LocalDate startDate,
                                    long billCount) {
    }

    private Long resolveBookId(Map<Long, FortuneBookEntity> bookMap, Long groupId) {
        return bookMap.values().stream()
                .filter(book -> Objects.equals(book.getGroupId(), groupId))
                .map(FortuneBookEntity::getBookId)
                .findFirst()
                .orElse(null);
    }
}
