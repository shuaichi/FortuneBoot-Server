package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillExtraAddCommand;
import com.fortuneboot.domain.dto.fortune.CategoryAmountDTO;
import com.fortuneboot.domain.entity.fortune.*;
import com.fortuneboot.repository.fortune.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangchi118
 */
@Component
@RequiredArgsConstructor
class FortuneBillImportRelationBatch {

    private final FortuneCategoryRelationRepo fortuneCategoryRelationRepo;
    private final FortuneTagRelationRepo fortuneTagRelationRepo;
    private final FortuneMemberRelationRepo fortuneMemberRelationRepo;
    private final FortuneBillExtraRepo fortuneBillExtraRepo;

    Relations create() {
        return new Relations();
    }

    void collect(Relations relations, FortuneBillAddCommand command, Long billId) {
        if (CollectionUtils.isNotEmpty(command.getCategoryAmountPair())) {
            for (CategoryAmountDTO category : command.getCategoryAmountPair()) {
                FortuneCategoryRelationEntity relation = new FortuneCategoryRelationEntity();
                relation.setBillId(billId);
                relation.setCategoryId(category.getCategoryId());
                relation.setAmount(category.getAmount());
                relations.categoryRelations.add(relation);
            }
        }
        if (CollectionUtils.isNotEmpty(command.getTagIdList())) {
            for (Long tagId : command.getTagIdList()) {
                FortuneTagRelationEntity relation = new FortuneTagRelationEntity();
                relation.setBillId(billId);
                relation.setTagId(tagId);
                relations.tagRelations.add(relation);
            }
        }
        if (CollectionUtils.isNotEmpty(command.getMemberIdList())) {
            for (Long memberId : command.getMemberIdList()) {
                FortuneMemberRelationEntity relation = new FortuneMemberRelationEntity();
                relation.setBillId(billId);
                relation.setMemberId(memberId);
                relations.memberRelations.add(relation);
            }
        }
        if (CollectionUtils.isNotEmpty(command.getExtras())) {
            for (FortuneBillExtraAddCommand extraCommand : command.getExtras()) {
                FortuneBillExtraEntity extra = new FortuneBillExtraEntity();
                extra.setBillId(billId);
                extra.setExtraType(extraCommand.getExtraType());
                extra.setAmount(extraCommand.getAmount());
                extra.setAccountSide(extraCommand.getAccountSide());
                extra.setCategoryId(extraCommand.getCategoryId());
                extra.setRemark(extraCommand.getRemark());
                relations.extras.add(extra);
            }
        }
    }

    void save(Relations relations) {
        if (CollectionUtils.isNotEmpty(relations.categoryRelations)) {
            fortuneCategoryRelationRepo.saveBatch(relations.categoryRelations, 1000);
        }
        if (CollectionUtils.isNotEmpty(relations.tagRelations)) {
            fortuneTagRelationRepo.saveBatch(relations.tagRelations, 1000);
        }
        if (CollectionUtils.isNotEmpty(relations.memberRelations)) {
            fortuneMemberRelationRepo.saveBatch(relations.memberRelations, 1000);
        }
        if (CollectionUtils.isNotEmpty(relations.extras)) {
            fortuneBillExtraRepo.saveBatch(relations.extras, 1000);
        }
    }

    static class Relations {
        private final List<FortuneCategoryRelationEntity> categoryRelations = new ArrayList<>();
        private final List<FortuneTagRelationEntity> tagRelations = new ArrayList<>();
        private final List<FortuneMemberRelationEntity> memberRelations = new ArrayList<>();
        private final List<FortuneBillExtraEntity> extras = new ArrayList<>();
    }
}
