package com.fortuneboot.service.fortune;

import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBalanceSnapshotEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import com.fortuneboot.repository.fortune.FortuneBalanceSnapshotRepo;
import com.fortuneboot.repository.fortune.FortuneBookRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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

    private final FortuneBookRepo fortuneBookRepo;
    private final FortuneAccountRepo fortuneAccountRepo;
    private final FortuneBalanceSnapshotRepo fortuneBalanceSnapshotRepo;
    private final FortuneAccountService fortuneAccountService;

    @Transactional(rollbackFor = Exception.class)
    public void generateMonthEndSnapshot() {
        generateSnapshot(LocalDate.now().minusDays(1));
    }

    @Transactional(rollbackFor = Exception.class)
    public void generateSnapshot(LocalDate snapshotDate) {
        fortuneBalanceSnapshotRepo.removeBySnapshotDate(snapshotDate);
        List<FortuneBookEntity> books = fortuneBookRepo.list();
        Map<Long, FortuneBookEntity> bookMap = books.stream()
                .filter(book -> book.getBookId() != null)
                .collect(Collectors.toMap(FortuneBookEntity::getBookId, book -> book, (left, right) -> left));
        List<FortuneAccountEntity> accounts = fortuneAccountRepo.list().stream()
                .filter(account -> Boolean.TRUE.equals(account.getEnable()))
                .filter(account -> Boolean.TRUE.equals(account.getInclude()))
                .filter(account -> Boolean.FALSE.equals(account.getRecycleBin()))
                .toList();
        List<FortuneBalanceSnapshotEntity> snapshots = new ArrayList<>();
        for (Map.Entry<Long, List<FortuneAccountEntity>> entry : accounts.stream().collect(Collectors.groupingBy(FortuneAccountEntity::getGroupId)).entrySet()) {
            Long groupId = entry.getKey();
            BigDecimal totalAssets = BigDecimal.ZERO;
            BigDecimal totalLiabilities = BigDecimal.ZERO;
            List<FortuneBalanceSnapshotEntity> groupSnapshots = new ArrayList<>();
            for (FortuneAccountEntity account : entry.getValue()) {
                BigDecimal converted = fortuneAccountService.convertToGroupCurrency(groupId, account);
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
                snapshot.setBalance(Objects.requireNonNullElse(account.getBalance(), BigDecimal.ZERO));
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
        if (!snapshots.isEmpty()) {
            fortuneBalanceSnapshotRepo.saveBatch(snapshots);
        }
        log.info("账户余额快照生成完成，日期: {}，数量: {}", snapshotDate, snapshots.size());
    }

    private Long resolveBookId(Map<Long, FortuneBookEntity> bookMap, Long groupId) {
        return bookMap.values().stream()
                .filter(book -> Objects.equals(book.getGroupId(), groupId))
                .map(FortuneBookEntity::getBookId)
                .findFirst()
                .orElse(null);
    }
}
