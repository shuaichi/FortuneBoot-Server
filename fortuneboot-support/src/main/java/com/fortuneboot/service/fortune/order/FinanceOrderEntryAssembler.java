package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderBillActionEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderLedgerVersionEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderReconciliationStatusEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBillExtraEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryRelationEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.entity.fortune.FortuneMemberEntity;
import com.fortuneboot.domain.entity.fortune.FortuneMemberRelationEntity;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.domain.entity.fortune.FortuneTagRelationEntity;
import com.fortuneboot.domain.vo.fortune.FortuneMemberVo;
import com.fortuneboot.domain.vo.fortune.FortuneTagVo;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillExtraVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderCategoryAmountVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderEntryVo;
import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import com.fortuneboot.repository.fortune.FortuneBillExtraRepo;
import com.fortuneboot.repository.fortune.FortuneBookRepo;
import com.fortuneboot.repository.fortune.FortuneCategoryRelationRepo;
import com.fortuneboot.repository.fortune.FortuneCategoryRepo;
import com.fortuneboot.repository.fortune.FortuneFileRepo;
import com.fortuneboot.repository.fortune.FortuneMemberRelationRepo;
import com.fortuneboot.repository.fortune.FortuneMemberRepo;
import com.fortuneboot.repository.fortune.FortunePayeeRepo;
import com.fortuneboot.repository.fortune.FortuneTagRelationRepo;
import com.fortuneboot.repository.fortune.FortuneTagRepo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 单据流水装配器。
 * <p>负责流水实体到VO的转换、流水级动作权限计算和业务字段批量填充。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
@RequiredArgsConstructor
public class FinanceOrderEntryAssembler {

    private final FortuneBookRepo bookRepo;
    private final FortunePayeeRepo payeeRepo;
    private final FortuneAccountRepo accountRepo;
    private final FortuneCategoryRepo categoryRepo;
    private final FortuneCategoryRelationRepo categoryRelationRepo;
    private final FortuneTagRepo tagRepo;
    private final FortuneTagRelationRepo tagRelationRepo;
    private final FortuneMemberRepo memberRepo;
    private final FortuneMemberRelationRepo memberRelationRepo;
    private final FortuneFileRepo fileRepo;
    private final FortuneBillExtraRepo billExtraRepo;

    public FinanceOrderEntryVo build(FortuneFinanceOrderEntity order, FortuneBillEntity bill, boolean actor) {
        FinanceOrderEntryVo vo = new FinanceOrderEntryVo();
        vo.setBillId(bill.getBillId());
        vo.setBookId(bill.getBookId());
        vo.setTitle(bill.getTitle());
        vo.setTradeTime(bill.getTradeTime());
        vo.setAccountId(bill.getAccountId());
        vo.setOrderId(bill.getOrderId());
        vo.setAmount(FinanceOrderAmountRule.format(bill.getAmount()));
        vo.setConvertedAmount(FinanceOrderAmountRule.format(bill.getConvertedAmount()));
        vo.setPayeeId(bill.getPayeeId());
        vo.setBillType(bill.getBillType());
        vo.setToAccountId(bill.getToAccountId());
        vo.setConfirm(bill.getConfirm());
        vo.setInclude(bill.getInclude());
        vo.setRemark(bill.getRemark());
        vo.setOrderComponent(bill.getOrderComponent());
        vo.setOrderLedgerVersion(bill.getOrderLedgerVersion() == null
                ? FinanceOrderLedgerVersionEnum.LEGACY.getValue() : bill.getOrderLedgerVersion());
        vo.setRecycleBin(Boolean.TRUE.equals(bill.getRecycleBin()));
        vo.setAllowedActions(allowedActions(order, bill, actor));
        if (!FinanceOrderReconciliationStatusEnum.isReadyValue(order.getReconciliationStatus())) {
            vo.setReadOnlyReason(StringUtils.defaultIfBlank(order.getReconciliationNote(),
                    FinanceOrderReconciliationStatusEnum.REVIEW_REQUIRED.getValue()));
        }
        return vo;
    }

    public void fillBusinessFields(Long bookId, List<FinanceOrderEntryVo> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        List<Long> billIds = rows.stream().map(FinanceOrderEntryVo::getBillId).toList();
        Map<Long, List<FortuneCategoryRelationEntity>> categoryRelations =
                categoryRelationRepo.getByBillIdList(billIds);
        Map<Long, List<FortuneTagRelationEntity>> tagRelations = tagRelationRepo.getByBillIdList(billIds);
        Map<Long, List<FortuneMemberRelationEntity>> memberRelations = memberRelationRepo.getByBillIdList(billIds);
        Map<Long, List<FortuneBillExtraEntity>> extras = billExtraRepo.getByBillIdList(billIds);
        Set<Long> hasFileBillIds = fileRepo.findBillIdsWithFiles(billIds);

        Set<Long> categoryIds = categoryRelations.values().stream().flatMap(List::stream)
                .map(FortuneCategoryRelationEntity::getCategoryId).collect(Collectors.toSet());
        Set<Long> tagIds = tagRelations.values().stream().flatMap(List::stream)
                .map(FortuneTagRelationEntity::getTagId).collect(Collectors.toSet());
        Set<Long> memberIds = memberRelations.values().stream().flatMap(List::stream)
                .map(FortuneMemberRelationEntity::getMemberId).collect(Collectors.toSet());
        Set<Long> accountIds = rows.stream()
                .flatMap(row -> Stream.of(row.getAccountId(), row.getToAccountId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> payeeIds = rows.stream().map(FinanceOrderEntryVo::getPayeeId)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, FortuneCategoryEntity> categories = categoryRepo.getByIds(new ArrayList<>(categoryIds)).stream()
                .collect(Collectors.toMap(FortuneCategoryEntity::getCategoryId, Function.identity()));
        Map<Long, FortuneTagEntity> tags = tagRepo.getByIds(new ArrayList<>(tagIds)).stream()
                .collect(Collectors.toMap(FortuneTagEntity::getTagId, Function.identity()));
        Map<Long, FortuneMemberEntity> members = memberRepo.getByIds(new ArrayList<>(memberIds)).stream()
                .collect(Collectors.toMap(FortuneMemberEntity::getMemberId, Function.identity()));
        Map<Long, FortuneAccountEntity> accounts = accountRepo.getByIds(new ArrayList<>(accountIds)).stream()
                .collect(Collectors.toMap(FortuneAccountEntity::getAccountId, Function.identity()));
        Map<Long, String> payeeNames = payeeRepo.getByIdList(new ArrayList<>(payeeIds)).stream()
                .collect(Collectors.toMap(FortunePayeeEntity::getPayeeId, FortunePayeeEntity::getPayeeName));
        Map<Long, FortuneBookEntity> books = bookRepo.listByIds(List.of(bookId)).stream()
                .collect(Collectors.toMap(FortuneBookEntity::getBookId, Function.identity()));

        for (FinanceOrderEntryVo row : rows) {
            FortuneBookEntity book = books.get(bookId);
            if (book != null) {
                row.setBookName(book.getBookName());
            }
            FortuneAccountEntity account = accounts.get(row.getAccountId());
            if (account != null) {
                row.setAccountName(account.getAccountName());
                row.setCurrencyCode(account.getCurrencyCode());
            }
            FortuneAccountEntity toAccount = accounts.get(row.getToAccountId());
            if (toAccount != null) {
                row.setToAccountName(toAccount.getAccountName());
                row.setToCurrencyCode(toAccount.getCurrencyCode());
            }
            if (row.getPayeeId() != null) {
                row.setPayeeName(payeeNames.get(row.getPayeeId()));
            }
            row.setHasFile(hasFileBillIds.contains(row.getBillId()));
            row.setCategoryAmountPair(categoryAmounts(categoryRelations.get(row.getBillId()), categories));
            row.setTagList(relationsOf(tagRelations.get(row.getBillId()),
                    relation -> tags.get(relation.getTagId()) == null ? null
                            : new FortuneTagVo(tags.get(relation.getTagId()))));
            row.setMemberList(relationsOf(memberRelations.get(row.getBillId()),
                    relation -> members.get(relation.getMemberId()) == null ? null
                            : new FortuneMemberVo(members.get(relation.getMemberId()))));
            row.setExtras(extras.get(row.getBillId()) == null ? List.of()
                    : extras.get(row.getBillId()).stream().map(FortuneBillExtraVo::new).toList());
        }
    }

    private String[] allowedActions(FortuneFinanceOrderEntity order, FortuneBillEntity bill, boolean actor) {
        if (!actor || !FinanceOrderReconciliationStatusEnum.isReadyValue(order.getReconciliationStatus())) {
            return new String[0];
        }
        if (!Objects.equals(order.getStatus(), FinanceOrderStatusEnum.USING.getValue())) {
            return new String[0];
        }
        List<FinanceOrderBillActionEnum> actions = new ArrayList<>();
        if (Boolean.TRUE.equals(bill.getRecycleBin())) {
            actions.add(FinanceOrderBillActionEnum.RESTORE);
        } else {
            actions.add(FinanceOrderBillActionEnum.EDIT);
            actions.add(Boolean.TRUE.equals(bill.getConfirm())
                    ? FinanceOrderBillActionEnum.UNCONFIRM : FinanceOrderBillActionEnum.CONFIRM);
            actions.add(FinanceOrderBillActionEnum.VOID);
        }
        actions.add(FinanceOrderBillActionEnum.COPY);
        return actions.stream().map(FinanceOrderBillActionEnum::name).toArray(String[]::new);
    }

    private List<FinanceOrderCategoryAmountVo> categoryAmounts(List<FortuneCategoryRelationEntity> relations,
                                                               Map<Long, FortuneCategoryEntity> categories) {
        if (CollectionUtils.isEmpty(relations)) {
            return List.of();
        }
        return relations.stream().map(relation -> {
            FortuneCategoryEntity category = categories.get(relation.getCategoryId());
            FinanceOrderCategoryAmountVo vo = new FinanceOrderCategoryAmountVo();
            vo.setCategoryId(relation.getCategoryId());
            vo.setCategoryName(category == null ? null : category.getCategoryName());
            vo.setAmount(FinanceOrderAmountRule.format(relation.getAmount()));
            return vo;
        }).toList();
    }

    private <R, V> List<V> relationsOf(List<R> relations, Function<R, V> mapper) {
        if (CollectionUtils.isEmpty(relations)) {
            return List.of();
        }
        return relations.stream().map(mapper).filter(Objects::nonNull).toList();
    }
}
