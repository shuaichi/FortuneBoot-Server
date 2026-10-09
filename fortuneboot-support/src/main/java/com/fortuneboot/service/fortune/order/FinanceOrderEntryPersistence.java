package com.fortuneboot.service.fortune.order;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneBillExtraAddCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.dto.fortune.CategoryAmountDTO;
import com.fortuneboot.domain.entity.fortune.*;
import com.fortuneboot.repository.fortune.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FinanceOrderEntryPersistence {

    /**
     * 关联关系批量入库大小。
     */
    private static final int RELATION_BATCH_SIZE = 1000;

    /**
     * 附件批量入库大小，避免单批过大。
     */
    private static final int FILE_BATCH_SIZE = 50;


    private final FortuneBillRepo fortuneBillRepo;
    private final FortuneCategoryRelationRepo categoryRelationRepo;
    private final FortuneTagRelationRepo tagRelationRepo;
    private final FortuneMemberRelationRepo memberRelationRepo;
    private final FortuneFileRepo fileRepo;
    private final FortuneBillExtraRepo billExtraRepo;

    @Transactional(rollbackFor = Exception.class)
    public FortuneBillEntity add(FinanceOrderEntryCommand command, List<MultipartFile> files) {
        FortuneBillEntity bill = new FortuneBillEntity();
        BeanUtil.copyProperties(command, bill, "billId", "fileList", "extras", "requestId", "expectedVersion");
        bill.setOrderLedgerVersion(1);
        bill.setDeleted(false);
        bill.setRecycleBin(false);
        fortuneBillRepo.save(bill);
        replaceRelations(bill, command, files);
        return bill;
    }

    @Transactional(rollbackFor = Exception.class)
    public void modify(FortuneBillEntity bill, FinanceOrderEntryCommand command, List<MultipartFile> files) {
        BeanUtil.copyProperties(command, bill, "billId", "bookId", "orderId", "billType",
                "orderComponent", "orderLedgerVersion", "deleted", "recycleBin",
                "fileList", "extras", "requestId", "expectedVersion");
        fortuneBillRepo.updateById(bill);
        categoryRelationRepo.phyRemoveByBillId(bill.getBillId());
        tagRelationRepo.phyRemoveByBillId(bill.getBillId());
        memberRelationRepo.phyRemoveByBillId(bill.getBillId());
        fileRepo.phyRemoveByBillId(bill.getBillId());
        billExtraRepo.phyRemoveByBillId(bill.getBillId());
        replaceRelations(bill, command, files);
    }

    public void updateState(FortuneBillEntity bill) {
        fortuneBillRepo.updateById(bill);
    }

    private void replaceRelations(FortuneBillEntity bill, FinanceOrderEntryCommand command,
                                  List<MultipartFile> files) {
        if (CollectionUtils.isNotEmpty(command.getCategoryAmountPair())) {
            List<FortuneCategoryRelationEntity> relations = command.getCategoryAmountPair().stream()
                    .map(category -> relation(bill.getBillId(), category))
                    .toList();
            categoryRelationRepo.saveBatch(relations, RELATION_BATCH_SIZE);
        }
        if (CollectionUtils.isNotEmpty(command.getTagIdList())) {
            List<FortuneTagRelationEntity> relations = command.getTagIdList().stream()
                    .map(tagId -> tagRelation(bill.getBillId(), tagId))
                    .toList();
            tagRelationRepo.saveBatch(relations, RELATION_BATCH_SIZE);
        }
        if (CollectionUtils.isNotEmpty(command.getMemberIdList())) {
            List<FortuneMemberRelationEntity> relations = command.getMemberIdList().stream()
                    .map(memberId -> memberRelation(bill.getBillId(), memberId))
                    .toList();
            memberRelationRepo.saveBatch(relations, RELATION_BATCH_SIZE);
        }
        if (CollectionUtils.isNotEmpty(command.getExtras())) {
            List<FortuneBillExtraEntity> relations = command.getExtras().stream()
                    .map(extra -> extra(bill.getBillId(), extra))
                    .toList();
            billExtraRepo.saveBatch(relations, RELATION_BATCH_SIZE);
        }
        if (CollectionUtils.isNotEmpty(files)) {
            List<FortuneFileEntity> relations = files.stream()
                    .map(file -> file(bill.getBillId(), file))
                    .toList();
            fileRepo.saveBatch(relations, FILE_BATCH_SIZE);
        }
    }

    private FortuneCategoryRelationEntity relation(Long billId, CategoryAmountDTO category) {
        FortuneCategoryRelationEntity relation = new FortuneCategoryRelationEntity();
        relation.setBillId(billId);
        relation.setCategoryId(category.getCategoryId());
        relation.setAmount(category.getAmount());
        relation.setDeleted(false);
        return relation;
    }

    private FortuneTagRelationEntity tagRelation(Long billId, Long tagId) {
        FortuneTagRelationEntity relation = new FortuneTagRelationEntity();
        relation.setBillId(billId);
        relation.setTagId(tagId);
        relation.setDeleted(false);
        return relation;
    }

    private FortuneMemberRelationEntity memberRelation(Long billId, Long memberId) {
        FortuneMemberRelationEntity relation = new FortuneMemberRelationEntity();
        relation.setBillId(billId);
        relation.setMemberId(memberId);
        relation.setDeleted(false);
        return relation;
    }

    private FortuneBillExtraEntity extra(Long billId, FortuneBillExtraAddCommand command) {
        FortuneBillExtraEntity extra = new FortuneBillExtraEntity();
        extra.setBillId(billId);
        extra.setExtraType(command.getExtraType());
        extra.setAmount(command.getAmount());
        extra.setAccountSide(command.getAccountSide());
        extra.setCategoryId(command.getCategoryId());
        extra.setRemark(command.getRemark());
        extra.setDeleted(false);
        return extra;
    }

    private FortuneFileEntity file(Long billId, MultipartFile multipartFile) {
        FortuneFileEntity file = new FortuneFileEntity();
        file.setBillId(billId);
        file.setContentType(multipartFile.getContentType());
        file.setOriginalName(multipartFile.getOriginalFilename());
        try {
            file.setFileData(multipartFile.getBytes());
            file.setSize((long) file.getFileData().length);
        } catch (IOException exception) {
            throw new ApiException(exception, ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "附件读取失败");
        }
        file.setDeleted(false);
        return file;
    }
}
